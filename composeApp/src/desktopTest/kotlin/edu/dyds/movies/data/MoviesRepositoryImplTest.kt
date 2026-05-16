package edu.dyds.movies.data

import edu.dyds.movies.data.external.RemoteMovie
import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.test.LocalDataSourceSpy
import edu.dyds.movies.test.RemoteDataSourceFake
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
        val remote: RemoteDataSourceFake,
    )

    private fun createTestBed(
        localPopularMovies: List<Movie>? = null,
        localDetailsCache: Map<Int, Movie> = emptyMap(),
        remotePopularMovies: List<RemoteMovie> = emptyList(),
        remoteMovieDetailsProvider: (Int) -> RemoteMovie = { remoteMovie(it) },
        shouldThrowOnLocalPopular: Boolean = false,
        shouldThrowOnLocalDetails: Boolean = false,
        shouldThrowOnRemotePopular: Boolean = false,
        shouldThrowOnRemoteDetails: Boolean = false,
    ): TestBed {
        val local = LocalDataSourceSpy(
            cachedPopularMovies = localPopularMovies,
            shouldThrowOnGetPopular = shouldThrowOnLocalPopular,
            shouldThrowOnGetDetails = shouldThrowOnLocalDetails,
            detailsCache = localDetailsCache,
        )
        val remote = RemoteDataSourceFake(
            popularMoviesProvider = { remotePopularMovies },
            movieDetailsProvider = remoteMovieDetailsProvider,
            shouldThrowOnPopular = shouldThrowOnRemotePopular,
            shouldThrowOnDetails = shouldThrowOnRemoteDetails,
        )
        return TestBed(MoviesRepositoryImpl(local, remote), local, remote)
    }

    @Test
    fun `get popular movies should return cached movies and skip remote`() = runTest {
        val cachedMovies = listOf(movie(id = 1), movie(id = 2))
        val bed = createTestBed(localPopularMovies = cachedMovies)

        val result = bed.repository.getPopularMovies()

        assertEquals(cachedMovies, result)
        assertEquals(0, bed.remote.getPopularMoviesCalls)
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get popular movies should return empty list from local cache and skip remote`() = runTest {
        val bed = createTestBed(localPopularMovies = emptyList())

        val result = bed.repository.getPopularMovies()

        assertEquals(emptyList(), result)
        assertEquals(0, bed.remote.getPopularMoviesCalls)
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get popular movies should fetch remote map to domain and cache when local is null`() = runTest {
        val remoteMovies = listOf(remoteMovie(1), remoteMovie(2))
        val bed = createTestBed(localPopularMovies = null, remotePopularMovies = remoteMovies)

        val result = bed.repository.getPopularMovies()

        assertEquals(remoteMovies.map { it.toDomainMovie() }, result)
        assertEquals(1, bed.remote.getPopularMoviesCalls)
        assertEquals(1, bed.local.savePopularMoviesCalls)
        assertEquals(result, bed.local.lastSavedPopularMovies)
    }

    @Test
    fun `get popular movies should return and cache empty list when remote has no movies`() = runTest {
        val bed = createTestBed(localPopularMovies = null, remotePopularMovies = emptyList())

        val result = bed.repository.getPopularMovies()

        assertEquals(emptyList(), result)
        assertEquals(1, bed.remote.getPopularMoviesCalls)
        assertEquals(1, bed.local.savePopularMoviesCalls)
        assertEquals(emptyList(), bed.local.lastSavedPopularMovies)
    }

    @Test
    fun `get popular movies should propagate local exception and never call remote`() = runTest {
        val bed = createTestBed(shouldThrowOnLocalPopular = true)

        val error = assertFailsWith<IllegalStateException> { bed.repository.getPopularMovies() }

        assertTrue(error.message?.contains("local getPopular") == true)
        assertEquals(0, bed.remote.getPopularMoviesCalls)
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get popular movies should propagate remote exception and not cache anything`() = runTest {
        val bed = createTestBed(localPopularMovies = null, shouldThrowOnRemotePopular = true)

        assertFailsWith<IllegalStateException> { bed.repository.getPopularMovies() }
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get movie details should return local movie when cache already has that id`() = runTest {
        val cachedMovie = movie(id = 42)
        val bed = createTestBed(localDetailsCache = mapOf(42 to cachedMovie))

        val result = bed.repository.getMovieDetails(42)

        assertEquals(cachedMovie, result)
        assertEquals(0, bed.remote.getMovieDetailsCalls)
        assertEquals(0, bed.local.getPopularMoviesCalls)
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get movie details should fetch remote and append to existing popular cache`() = runTest {
        val cachedMovie = movie(id = 1)
        val bed = createTestBed(localPopularMovies = listOf(cachedMovie))

        val result = bed.repository.getMovieDetails(99)

        assertNotNull(result)
        assertEquals(99, result.id)
        assertEquals(1, bed.remote.getMovieDetailsCalls)
        assertEquals(1, bed.local.getPopularMoviesCalls)
        assertEquals(1, bed.local.savePopularMoviesCalls)
        assertEquals(listOf(1, 99), bed.local.lastSavedPopularMovies?.map { it.id })
    }

    @Test
    fun `get movie details should not save when popular cache is null`() = runTest {
        val bed = createTestBed(localPopularMovies = null)

        val result = bed.repository.getMovieDetails(99)

        assertNotNull(result)
        assertEquals(1, bed.remote.getMovieDetailsCalls)
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get movie details should map remote fields correctly including null backdrop`() = runTest {
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
            remoteMovieDetailsProvider = { remoteDetail },
        )

        val result = bed.repository.getMovieDetails(999)

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
        assertEquals(1, bed.remote.getMovieDetailsCalls)
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get movie details should return cached popular movie and skip remote when id exists`() = runTest {
        val cachedMovies = listOf(movie(id = 1), movie(id = 99))
        val bed = createTestBed(localPopularMovies = cachedMovies)

        val result = bed.repository.getMovieDetails(99)

        assertNotNull(result)
        assertEquals(99, result.id)
        assertEquals(cachedMovies[1], result)
        assertEquals(0, bed.remote.getMovieDetailsCalls)
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get movie details should propagate local exception and never call remote`() = runTest {
        val bed = createTestBed(shouldThrowOnLocalDetails = true)

        assertFailsWith<IllegalStateException> { bed.repository.getMovieDetails(1) }
        assertEquals(0, bed.remote.getMovieDetailsCalls)
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get movie details should propagate remote exception and not save cache`() = runTest {
        val bed = createTestBed(localPopularMovies = null, shouldThrowOnRemoteDetails = true)

        assertFailsWith<IllegalStateException> { bed.repository.getMovieDetails(1) }
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }
}



