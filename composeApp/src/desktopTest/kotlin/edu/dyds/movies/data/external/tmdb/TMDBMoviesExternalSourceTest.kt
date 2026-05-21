package edu.dyds.movies.data.external.tmdb

import edu.dyds.movies.data.external.RemoteMovie
import edu.dyds.movies.data.external.RemoteResult
import edu.dyds.movies.test.remoteMovie
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * Unit tests for TMDBMoviesExternalSource.
 *
 * These tests verify the correct behavior of the TMDB external data source,
 * including proper HTTP endpoint calls, response parsing, and error handling.
 *
 * Tests follow SOLID principles and clean code practices:
 * - Single Responsibility: Each test verifies one specific behavior
 * - Dependency Injection: Mock HttpClient is created per test
 * - DRY: Common factory method for HttpClient creation
 * - Meaningful names: Test names clearly describe what is being tested
 * - Before/After pattern: Resources are properly cleaned up with @AfterTest
 */
class TMDBMoviesExternalSourceTest {

    private lateinit var httpClient: HttpClient

    /**
     * AfterTest: Cleans up HTTP resources after each test.
     * This follows the "After" pattern for test cleanup with @AfterTest annotation.
     */
    @AfterTest
    fun tearDown() {
        httpClient.close()
    }

    /**
     * Helper to create a configured mock HttpClient for testing.
     * This follows the factory pattern to reduce code duplication.
     */
    private fun createTestHttpClient(
        responseProvider: (String) -> String,
    ): HttpClient {
        return HttpClient(MockEngine { request ->
            val responseBody = responseProvider(request.url.toString())
            respond(
                content = responseBody,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }


    // ==================== getPopularMovies tests ====================

    @Test
    fun `getPopularMovies should return list of remote movies`() = runTest {
        // Arrange
        val expectedMovies = listOf(remoteMovie(1), remoteMovie(2))
        val response = RemoteResult(
            page = 1,
            results = expectedMovies,
            totalPages = 1,
            totalResults = 2,
        )
        httpClient = createTestHttpClient { Json.encodeToString(response) }

        // Act
        val result = TMDBMoviesExternalSource(httpClient).getPopularMovies()

        // Assert
        assertEquals(expectedMovies, result)
    }

    @Test
    fun `getPopularMovies should return empty list when response is empty`() = runTest {
        // Arrange
        val response = RemoteResult(
            page = 1,
            results = emptyList(),
            totalPages = 0,
            totalResults = 0,
        )
        httpClient = createTestHttpClient { Json.encodeToString(response) }

        // Act
        val result = TMDBMoviesExternalSource(httpClient).getPopularMovies()

        // Assert
        assertEquals(emptyList(), result)
    }

    @Test
    fun `getPopularMovies should handle large result sets`() = runTest {
        // Arrange
        val expectedMovies = (1..50).map { remoteMovie(it) }.toList()
        val response = RemoteResult(
            page = 1,
            results = expectedMovies,
            totalPages = 5,
            totalResults = 250,
        )
        httpClient = createTestHttpClient { Json.encodeToString(response) }

        // Act
        val result = TMDBMoviesExternalSource(httpClient).getPopularMovies()

        // Assert
        assertEquals(50, result.size)
        assertEquals(expectedMovies, result)
    }

    // ==================== getMovieByTitle tests ====================

    @Test
    fun `getMovieByTitle should return first matching remote movie`() = runTest {
        // Arrange
        val expectedMovie = remoteMovie(42, "Expected Movie")
        val response = RemoteResult(
            page = 1,
            results = listOf(expectedMovie, remoteMovie(43, "Other Movie")),
            totalPages = 1,
            totalResults = 2,
        )
        httpClient = createTestHttpClient { Json.encodeToString(response) }

        // Act
        val result = TMDBMoviesExternalSource(httpClient).getMovieByTitle("Expected Movie")

        // Assert
        assertEquals(expectedMovie, result)
    }

    @Test
    fun `getMovieByTitle should map remote fields correctly`() = runTest {
        // Arrange
        val remoteMovie = RemoteMovie(
            id = 999,
            title = "Test Movie",
            overview = "Test overview",
            releaseDate = "2026-05-21",
            posterPath = "/test_poster.jpg",
            backdropPath = "/test_backdrop.jpg",
            originalTitle = "Película de Prueba",
            originalLanguage = "es",
            popularity = 42.5,
            voteAverage = 9.5,
        )
        val response = RemoteResult(
            page = 1,
            results = listOf(remoteMovie),
            totalPages = 1,
            totalResults = 1,
        )
        httpClient = createTestHttpClient { Json.encodeToString(response) }

        // Act
        val result = TMDBMoviesExternalSource(httpClient).getMovieByTitle("Test Movie")

        // Assert
        assertEquals(999, result.id)
        assertEquals("Test Movie", result.title)
        assertEquals("Test overview", result.overview)
        assertEquals("2026-05-21", result.releaseDate)
        assertEquals("/test_poster.jpg", result.posterPath)
        assertEquals("/test_backdrop.jpg", result.backdropPath)
        assertEquals("Película de Prueba", result.originalTitle)
        assertEquals("es", result.originalLanguage)
        assertEquals(42.5, result.popularity)
        assertEquals(9.5, result.voteAverage)
    }

    @Test
    fun `getMovieByTitle should map null backdrop correctly`() = runTest {
        // Arrange
        val remoteMovie = RemoteMovie(
            id = 888,
            title = "No Backdrop Movie",
            overview = "Movie without backdrop",
            releaseDate = "2026-05-21",
            posterPath = "/no_backdrop_poster.jpg",
            backdropPath = null,
            originalTitle = "Sin Backdrop",
            originalLanguage = "es",
            popularity = 10.0,
            voteAverage = 7.5,
        )
        val response = RemoteResult(
            page = 1,
            results = listOf(remoteMovie),
            totalPages = 1,
            totalResults = 1,
        )
        httpClient = createTestHttpClient { Json.encodeToString(response) }

        // Act
        val result = TMDBMoviesExternalSource(httpClient).getMovieByTitle("No Backdrop Movie")

        // Assert
        assertNotNull(result)
        assertEquals(null, result.backdropPath)
    }

    @Test
    fun `getMovieByTitle should throw IllegalArgumentException when response is empty`() = runTest {
        // Arrange
        val response = RemoteResult(
            page = 1,
            results = emptyList(),
            totalPages = 0,
            totalResults = 0,
        )
        httpClient = createTestHttpClient { Json.encodeToString(response) }

        // Act & Assert
        val exception = assertFailsWith<IllegalArgumentException> {
            TMDBMoviesExternalSource(httpClient).getMovieByTitle("Non Existent Movie")
        }
        assertEquals("No movie found for title: Non Existent Movie", exception.message)
    }

    @Test
    fun `getMovieByTitle should handle special characters in query`() = runTest {
        // Arrange
        val title = "Star Wars: Episode IV"
        val expectedMovie = RemoteMovie(
            id = 11,
            title = "Star Wars: Episode IV - A New Hope",
            overview = "Luke Skywalker...",
            releaseDate = "1977-05-25",
            posterPath = "/star_wars.jpg",
            backdropPath = "/star_wars_backdrop.jpg",
            originalTitle = "Star Wars: Episode IV - A New Hope",
            originalLanguage = "en",
            popularity = 100.0,
            voteAverage = 8.6,
        )
        val response = RemoteResult(
            page = 1,
            results = listOf(expectedMovie),
            totalPages = 1,
            totalResults = 1,
        )
        httpClient = createTestHttpClient { Json.encodeToString(response) }

        // Act
        val result = TMDBMoviesExternalSource(httpClient).getMovieByTitle(title)

        // Assert
        assertEquals(11, result.id)
        assertEquals("Star Wars: Episode IV - A New Hope", result.title)
    }

    @Test
    fun `getMovieByTitle should return first result when multiple matches exist`() = runTest {
        // Arrange
        val movie1 = remoteMovie(100, "Movie First Result")
        val movie2 = remoteMovie(101, "Movie Second Result")
        val movie3 = remoteMovie(102, "Movie Third Result")
        val response = RemoteResult(
            page = 1,
            results = listOf(movie1, movie2, movie3),
            totalPages = 1,
            totalResults = 3,
        )
        httpClient = createTestHttpClient { Json.encodeToString(response) }

        // Act
        val result = TMDBMoviesExternalSource(httpClient).getMovieByTitle("Movie")

        // Assert
        assertEquals(100, result.id)
        assertEquals("Movie First Result", result.title)
    }

    @Test
    fun `getMovieByTitle should handle numeric IDs correctly`() = runTest {
        // Arrange
        val remoteMovie = RemoteMovie(
            id = 550, // Fight Club
            title = "Fight Club",
            overview = "An insomniac office worker...",
            releaseDate = "1999-10-15",
            posterPath = "/fight_club.jpg",
            backdropPath = "/fight_club_backdrop.jpg",
            originalTitle = "Fight Club",
            originalLanguage = "en",
            popularity = 95.0,
            voteAverage = 8.4,
        )
        val response = RemoteResult(
            page = 1,
            results = listOf(remoteMovie),
            totalPages = 1,
            totalResults = 1,
        )
        httpClient = createTestHttpClient { Json.encodeToString(response) }

        // Act
        val result = TMDBMoviesExternalSource(httpClient).getMovieByTitle("Fight Club")

        // Assert
        assertEquals(550, result.id)
    }

    // ==================== HTTP Error Handling tests ====================

    @Test
    fun `getPopularMovies should propagate HTTP 500 error`() = runTest {
        // Arrange
        httpClient = HttpClient(MockEngine {
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString()),
            )
        }) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        // Act & Assert
        assertFailsWith<Exception> {
            TMDBMoviesExternalSource(httpClient).getPopularMovies()
        }
    }

    @Test
    fun `getMovieByTitle should propagate HTTP 500 error`() = runTest {
        // Arrange
        httpClient = HttpClient(MockEngine {
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString()),
            )
        }) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        // Act & Assert
        assertFailsWith<Exception> {
            TMDBMoviesExternalSource(httpClient).getMovieByTitle("Test")
        }
    }

    @Test
    fun `getPopularMovies should propagate exception when response is malformed`() = runTest {
        // Arrange: Return invalid JSON that cannot be deserialized as RemoteResult
        httpClient = HttpClient(MockEngine {
            respond(
                content = """{"invalid": "structure", "missing_results": true}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        // Act & Assert
        assertFailsWith<Exception> {
            TMDBMoviesExternalSource(httpClient).getPopularMovies()
        }
    }

    @Test
    fun `getMovieByTitle should propagate exception when response is malformed`() = runTest {
        // Arrange: Return invalid JSON that cannot be deserialized as RemoteResult
        httpClient = HttpClient(MockEngine {
            respond(
                content = """{"invalid": "structure", "no_results_field": true}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        // Act & Assert
        assertFailsWith<Exception> {
            TMDBMoviesExternalSource(httpClient).getMovieByTitle("Test")
        }
    }
}

