package edu.dyds.movies.data

import edu.dyds.movies.data.external.RemoteMovie
import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.test.LocalDataSourceSpy
import edu.dyds.movies.test.TMDBMoviesExternalSourceFake
import edu.dyds.movies.test.movie
import edu.dyds.movies.test.remoteMovie
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class MoviesRepositoryImplTest {

    private data class TestBed(
        val repository: MoviesRepositoryImpl,
        val local: LocalDataSourceSpy,
        val tmdb: TMDBMoviesExternalSourceFake,
    )

    private fun createTestBed(
        localPopularMovies: List<Movie>? = null,
        remotePopularMovies: List<RemoteMovie> = emptyList(),
        remoteMovieByTitleProvider: (String) -> RemoteMovie = { remoteMovie(0, it) },
        shouldThrowOnLocalPopular: Boolean = false,
        shouldThrowOnLocalByTitle: Boolean = false,
        shouldThrowOnRemotePopular: Boolean = false,
        shouldThrowOnRemoteByTitle: Boolean = false,
    ): TestBed {
        val local = LocalDataSourceSpy(
            cachedPopularMovies = localPopularMovies,
            shouldThrowOnGetPopular = shouldThrowOnLocalPopular,
            shouldThrowOnGetByTitle = shouldThrowOnLocalByTitle,
        )
        val tmdb = TMDBMoviesExternalSourceFake(
            popularMoviesProvider = { remotePopularMovies },
            movieByTitleProvider = remoteMovieByTitleProvider,
            shouldThrowOnPopular = shouldThrowOnRemotePopular,
            shouldThrowOnByTitle = shouldThrowOnRemoteByTitle,
        )
        return TestBed(MoviesRepositoryImpl(local, tmdb, tmdb), local, tmdb)
    }

    @Test
    fun `get popular movies should return cached movies and skip remote`() = runTest {
        val cachedMovies = listOf(movie(id = 1), movie(id = 2))
        val bed = createTestBed(localPopularMovies = cachedMovies)

        val result = bed.repository.getPopularMovies()

        assertEquals(cachedMovies, result)
        assertEquals(0, bed.tmdb.getPopularMoviesCalls)
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get popular movies should return empty list from local cache and skip remote`() = runTest {
        val bed = createTestBed(localPopularMovies = emptyList())

        val result = bed.repository.getPopularMovies()

        assertEquals(emptyList(), result)
        assertEquals(0, bed.tmdb.getPopularMoviesCalls)
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get popular movies should fetch remote map to domain and cache when local is null`() = runTest {
        val remoteMovies = listOf(remoteMovie(1), remoteMovie(2))
        val bed = createTestBed(localPopularMovies = null, remotePopularMovies = remoteMovies)

        val result = bed.repository.getPopularMovies()

        assertEquals(remoteMovies.map { it.toDomainMovie() }, result)
        assertEquals(1, bed.tmdb.getPopularMoviesCalls)
        assertEquals(1, bed.local.savePopularMoviesCalls)
        assertEquals(result, bed.local.lastSavedPopularMovies)
    }

    @Test
    fun `get popular movies should return and cache empty list when remote has no movies`() = runTest {
        val bed = createTestBed(localPopularMovies = null, remotePopularMovies = emptyList())

        val result = bed.repository.getPopularMovies()

        assertEquals(emptyList(), result)
        assertEquals(1, bed.tmdb.getPopularMoviesCalls)
        assertEquals(1, bed.local.savePopularMoviesCalls)
        assertEquals(emptyList(), bed.local.lastSavedPopularMovies)
    }

    @Test
    fun `get popular movies should propagate local exception and never call remote`() = runTest {
        val bed = createTestBed(shouldThrowOnLocalPopular = true)

        val error = assertFailsWith<IllegalStateException> { bed.repository.getPopularMovies() }

        assertTrue(error.message?.contains("local getPopular") == true)
        assertEquals(0, bed.tmdb.getPopularMoviesCalls)
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get popular movies should propagate remote exception and not cache anything`() = runTest {
        val bed = createTestBed(localPopularMovies = null, shouldThrowOnRemotePopular = true)

        assertFailsWith<IllegalStateException> { bed.repository.getPopularMovies() }
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get movie by title should return local movie when cache already has that title`() = runTest {
        val cachedMovie = movie(id = 42, title = "Expected movie")
        val bed = createTestBed(localPopularMovies = listOf(cachedMovie))

        val result = bed.repository.getMovieByTitle("Expected movie")

        assertEquals(cachedMovie, result)
        assertEquals(0, bed.tmdb.getMovieByTitleCalls)
        assertEquals(1, bed.local.getMovieByTitleCalls)
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get movie by title should fetch remote and always merge into non-null popular cache`() = runTest {
        val cachedMovie = movie(id = 1, title = "Cached Movie")
        val bed = createTestBed(localPopularMovies = listOf(cachedMovie))
        val targetTitle = "New Movie"

        val result = bed.repository.getMovieByTitle(targetTitle)

        assertNotNull(result)
        assertEquals(targetTitle, result.title)
        assertEquals(1, bed.tmdb.getMovieByTitleCalls)
        assertEquals(1, bed.local.getMovieByTitleCalls)
        assertEquals(1, bed.local.savePopularMoviesCalls)
        assertEquals(listOf("Cached Movie", targetTitle), bed.local.lastSavedPopularMovies?.map { it.title })
    }

    @Test
    fun `get movie by title should cache remote movie when popular cache is empty list`() = runTest {
        val bed = createTestBed(localPopularMovies = emptyList())
        val targetTitle = "Empty Cache Movie"

        val result = bed.repository.getMovieByTitle(targetTitle)

        assertNotNull(result)
        assertEquals(targetTitle, result.title)
        assertEquals(1, bed.tmdb.getMovieByTitleCalls)
        assertEquals(1, bed.local.getMovieByTitleCalls)
        assertEquals(1, bed.local.savePopularMoviesCalls)
        assertEquals(listOf(targetTitle), bed.local.lastSavedPopularMovies?.map { it.title })
    }

    @Test
    fun `get movie by title should not save when popular cache is null`() = runTest {
        val bed = createTestBed(localPopularMovies = null)
        val targetTitle = "Null Cache Movie"

        val result = bed.repository.getMovieByTitle(targetTitle)

        assertNotNull(result)
        assertEquals(1, bed.tmdb.getMovieByTitleCalls)
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get movie by title should map remote fields correctly including null backdrop`() = runTest {
        val remoteDetail = RemoteMovie(
            id = 999,
            title = "Test Movie",
            overview = "Test overview",
            releaseDate = "2026-05-11",
            posterPath = "/poster_test.jpg",
            backdropPath = null,
            originalTitle = "Titulo Original",
            originalLanguage = "ES",
            popularity = 42.5,
            voteAverage = 9.5,
        )
        val bed = createTestBed(
            localPopularMovies = null,
            remoteMovieByTitleProvider = { remoteDetail },
        )

        val result = bed.repository.getMovieByTitle("Test Movie")

        assertEquals(
            Movie(
                id = 999,
                title = "Test Movie",
                overview = "Test overview",
                releaseDate = "2026-05-11",
                poster = "https://image.tmdb.org/t/p/w185/poster_test.jpg",
                backdrop = null,
                originalTitle = "Titulo Original",
                originalLanguage = "ES",
                popularity = 42.5,
                voteAverage = 9.5,
            ),
            result,
        )
        assertEquals(1, bed.tmdb.getMovieByTitleCalls)
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get movie by title should return cached popular movie and skip remote when title exists`() = runTest {
        val cachedMovies = listOf(movie(id = 1, title = "Movie 1"), movie(id = 99, title = "Movie 99"))
        val bed = createTestBed(localPopularMovies = cachedMovies)

        val result = bed.repository.getMovieByTitle("Movie 99")

        assertNotNull(result)
        assertEquals("Movie 99", result.title)
        assertEquals(cachedMovies[1], result)
        assertEquals(0, bed.tmdb.getMovieByTitleCalls)
        assertEquals(1, bed.local.getMovieByTitleCalls)
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get movie by title should propagate local exception and never call remote`() = runTest {
        val bed = createTestBed(shouldThrowOnLocalByTitle = true)

        assertFailsWith<IllegalStateException> { bed.repository.getMovieByTitle("Any Movie") }
        assertEquals(0, bed.tmdb.getMovieByTitleCalls)
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get movie by title should propagate remote exception and not save cache`() = runTest {
        val bed = createTestBed(localPopularMovies = null, shouldThrowOnRemoteByTitle = true)

        assertFailsWith<IllegalStateException> { bed.repository.getMovieByTitle("Any Movie") }
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get movie by title should propagate not found exception when remote returns empty results`() = runTest {
        val local = LocalDataSourceSpy(cachedPopularMovies = null)
        val tmdb = TMDBMoviesExternalSourceFake(shouldReturnEmptyResultsOnTitle = true)
        val repository = MoviesRepositoryImpl(local, tmdb, tmdb)

        assertFailsWith<IllegalArgumentException> { repository.getMovieByTitle("Nonexistent Movie") }
        assertEquals(0, local.savePopularMoviesCalls)
    }
}