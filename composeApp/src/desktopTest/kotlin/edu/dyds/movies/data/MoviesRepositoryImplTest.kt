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
        shouldReturnEmptyResultsOnTitle: Boolean = false,
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
            shouldReturnEmptyResultsOnTitle = shouldReturnEmptyResultsOnTitle,
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
    fun `get movie by title should fetch remote and update local cache even if local already has it`() = runTest {
        val cachedMovie = remoteMovie(id = 42, title = "Expected movie").toDomainMovie() // Consistent Movie object
        val remoteMovieFromBroker = remoteMovie(id = 100, title = "Expected movie") // Simulate remote returning same title but different ID
        val bed = createTestBed(
            localPopularMovies = listOf(cachedMovie),
            remoteMovieByTitleProvider = { remoteMovieFromBroker }
        )

        val result = bed.repository.getMovieByTitle("Expected movie")

        assertNotNull(result)
        assertEquals(remoteMovieFromBroker.toDomainMovie(), result) // Result should be from remote
        assertEquals(1, bed.tmdb.getMovieByTitleCalls) // Remote should be called
        assertEquals(1, bed.local.getPopularMoviesCalls) // Local popular movies read to update
        assertEquals(1, bed.local.savePopularMoviesCalls) // Local popular movies saved
        // Verify the cache was updated with the remote version
        assertEquals(listOf(remoteMovieFromBroker.toDomainMovie()), bed.local.lastSavedPopularMovies)
    }

    @Test
    fun `get movie by title should fetch remote and always merge into non-null popular cache`() = runTest {
        val cachedMovie = remoteMovie(id = 1, title = "Cached Movie").toDomainMovie() // Consistent Movie object
        val targetTitle = "New Movie"
        val remoteMovieFromBroker = remoteMovie(id = 2, title = targetTitle) // Remote returns a new movie
        val bed = createTestBed(
            localPopularMovies = listOf(cachedMovie),
            remoteMovieByTitleProvider = { remoteMovieFromBroker },
        )

        val result = bed.repository.getMovieByTitle(targetTitle)

        assertNotNull(result)
        assertEquals(remoteMovieFromBroker.toDomainMovie(), result) // Result should be from remote
        assertEquals(1, bed.tmdb.getMovieByTitleCalls)
        assertEquals(1, bed.local.getPopularMoviesCalls) // Local popular movies read to update
        assertEquals(1, bed.local.savePopularMoviesCalls)
        // Verify the cache was updated with the new remote movie
        assertEquals(
            listOf(cachedMovie, remoteMovieFromBroker.toDomainMovie()),
            bed.local.lastSavedPopularMovies
        )
    }

    @Test
    fun `get movie by title should cache remote movie when popular cache is empty list`() = runTest {
        val targetTitle = "Empty Cache Movie"
        val remoteMovieFromBroker = remoteMovie(id = 1, title = targetTitle)
        val bed = createTestBed(
            localPopularMovies = emptyList(),
            remoteMovieByTitleProvider = { remoteMovieFromBroker },
        )

        val result = bed.repository.getMovieByTitle(targetTitle)

        assertNotNull(result)
        assertEquals(remoteMovieFromBroker.toDomainMovie(), result)
        assertEquals(1, bed.tmdb.getMovieByTitleCalls)
        assertEquals(1, bed.local.getPopularMoviesCalls) // Local popular movies read to update
        assertEquals(1, bed.local.savePopularMoviesCalls)
        assertEquals(listOf(remoteMovieFromBroker.toDomainMovie()), bed.local.lastSavedPopularMovies)
    }

    @Test
    fun `get movie by title should initialize popular cache when null`() = runTest {
        val targetTitle = "Null Cache Movie"
        val remoteMovieFromBroker = remoteMovie(id = 1, title = targetTitle)
        val bed = createTestBed(
            localPopularMovies = null,
            remoteMovieByTitleProvider = { remoteMovieFromBroker },
        )

        val result = bed.repository.getMovieByTitle(targetTitle)

        assertNotNull(result)
        assertEquals(remoteMovieFromBroker.toDomainMovie(), result)
        assertEquals(1, bed.tmdb.getMovieByTitleCalls)
        assertEquals(1, bed.local.getPopularMoviesCalls) // Now expects a read to update cache
        assertEquals(1, bed.local.savePopularMoviesCalls) // Now expects a save to initialize cache
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
        assertEquals(1, bed.local.getPopularMoviesCalls) // Now expects a read to update cache
        assertEquals(1, bed.local.savePopularMoviesCalls) // Now expects a save to initialize cache
    }

    @Test
    fun `get movie by title should fetch remote and update local cache when title exists in local`() = runTest {
        val cachedMovie = remoteMovie(id = 1, title = "Movie 1").toDomainMovie() // Consistent Movie object
        val cachedMovie99 = remoteMovie(id = 99, title = "Movie 99").toDomainMovie() // Consistent Movie object
        val remoteMovieFromBroker = remoteMovie(id = 100, title = "Movie 99") // Simulate remote returning same title but different ID
        val bed = createTestBed(
            localPopularMovies = listOf(cachedMovie, cachedMovie99),
            remoteMovieByTitleProvider = { remoteMovieFromBroker },
        )

        val result = bed.repository.getMovieByTitle("Movie 99")

        assertNotNull(result)
        assertEquals(remoteMovieFromBroker.toDomainMovie(), result) // Result should be from remote
        assertEquals(1, bed.tmdb.getMovieByTitleCalls) // Remote should be called
        assertEquals(1, bed.local.getPopularMoviesCalls) // Local popular movies read to update
        assertEquals(1, bed.local.savePopularMoviesCalls) // Local popular movies saved
        // Verify the cache was updated with the remote version
        assertEquals(
            listOf(cachedMovie, remoteMovieFromBroker.toDomainMovie()),
            bed.local.lastSavedPopularMovies
        )
    }

    @Test
    fun `get movie by title should propagate local exception if remote returns null`() = runTest {
        val bed = createTestBed(
            shouldReturnEmptyResultsOnTitle = true, // Simulate remote returning null
            shouldThrowOnLocalByTitle = true // Local will throw when called
        )

        assertFailsWith<IllegalStateException> { bed.repository.getMovieByTitle("Any Movie") }
        assertEquals(1, bed.tmdb.getMovieByTitleCalls) // Remote is called first
        assertEquals(1, bed.local.getMovieByTitleCalls) // Local is called after remote returns null
        assertEquals(0, bed.local.savePopularMoviesCalls) // No save if exception
    }

    @Test
    fun `get movie by title should propagate remote exception and not save cache`() = runTest {
        val bed = createTestBed(localPopularMovies = null, shouldThrowOnRemoteByTitle = true)

        assertFailsWith<IllegalStateException> { bed.repository.getMovieByTitle("Any Movie") }
        assertEquals(0, bed.local.savePopularMoviesCalls)
    }

    @Test
    fun `get movie by title should return null when remote returns empty results`() = runTest {
        val local = LocalDataSourceSpy(cachedPopularMovies = null)
        val tmdb = TMDBMoviesExternalSourceFake(shouldReturnEmptyResultsOnTitle = true)
        val repository = MoviesRepositoryImpl(local, tmdb, tmdb)

        val result = repository.getMovieByTitle("Nonexistent Movie")

        assertEquals(null, result)
        assertEquals(0, local.savePopularMoviesCalls)
    }
}
