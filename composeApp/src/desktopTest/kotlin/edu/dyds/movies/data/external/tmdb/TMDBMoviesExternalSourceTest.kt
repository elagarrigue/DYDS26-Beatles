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
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class TMDBMoviesExternalSourceTest {

    private lateinit var httpClient: HttpClient

    @AfterTest
    fun tearDown() {
        httpClient.close()
    }

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
        val expectedMovies = listOf(remoteMovie(1), remoteMovie(2))
        val response = RemoteResult(
            page = 1,
            results = expectedMovies,
            totalPages = 1,
            totalResults = 2,
        )
        httpClient = createTestHttpClient { Json.encodeToString(response) }

        val result = TMDBMoviesExternalSource(httpClient).getPopularMovies()

        assertEquals(expectedMovies, result)
    }

    @Test
    fun `getPopularMovies should return empty list when response is empty`() = runTest {
        val response = RemoteResult(
            page = 1,
            results = emptyList(),
            totalPages = 0,
            totalResults = 0,
        )
        httpClient = createTestHttpClient { Json.encodeToString(response) }

        val result = TMDBMoviesExternalSource(httpClient).getPopularMovies()

        assertEquals(emptyList(), result)
    }

    @Test
    fun `getPopularMovies should handle large result sets`() = runTest {
        val expectedMovies = (1..50).map { remoteMovie(it) }.toList()
        val response = RemoteResult(
            page = 1,
            results = expectedMovies,
            totalPages = 5,
            totalResults = 250,
        )
        httpClient = createTestHttpClient { Json.encodeToString(response) }

        val result = TMDBMoviesExternalSource(httpClient).getPopularMovies()

        assertEquals(50, result.size)
        assertEquals(expectedMovies, result)
    }

    // ==================== getMovieByTitle tests ====================

    @Test
    fun `getMovieByTitle should return first matching remote movie`() = runTest {
        val expectedMovie = remoteMovie(42, "Expected Movie")
        val response = RemoteResult(
            page = 1,
            results = listOf(expectedMovie, remoteMovie(43, "Other Movie")),
            totalPages = 1,
            totalResults = 2,
        )
        httpClient = createTestHttpClient { Json.encodeToString(response) }

        val result = TMDBMoviesExternalSource(httpClient).getMovieByTitle("Expected Movie")

        assertEquals(expectedMovie, result)
    }

    @Test
    fun `getMovieByTitle should map remote fields correctly`() = runTest {
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

        val result = requireNotNull(TMDBMoviesExternalSource(httpClient).getMovieByTitle("Test Movie"))

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

        val result = TMDBMoviesExternalSource(httpClient).getMovieByTitle("No Backdrop Movie")

        assertNotNull(result)
        assertEquals(null, result.backdropPath)
    }

    @Test
    fun `getMovieByTitle should return null when response is empty`() = runTest {
        val response = RemoteResult(
            page = 1,
            results = emptyList(),
            totalPages = 0,
            totalResults = 0,
        )
        httpClient = createTestHttpClient { Json.encodeToString(response) }

        val result = TMDBMoviesExternalSource(httpClient).getMovieByTitle("Non Existent Movie")

        assertNull(result)
    }

    @Test
    fun `getMovieByTitle should handle special characters in query`() = runTest {
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

        val result = requireNotNull(TMDBMoviesExternalSource(httpClient).getMovieByTitle(title))

        assertEquals(11, result.id)
        assertEquals("Star Wars: Episode IV - A New Hope", result.title)
    }

    @Test
    fun `getMovieByTitle should return first result when multiple matches exist`() = runTest {
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

        val result = requireNotNull(TMDBMoviesExternalSource(httpClient).getMovieByTitle("Movie"))

        assertEquals(100, result.id)
        assertEquals("Movie First Result", result.title)
    }

    @Test
    fun `getMovieByTitle should handle numeric IDs correctly`() = runTest {
         val remoteMovie = RemoteMovie(
             id = 550,
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

        val result = requireNotNull(TMDBMoviesExternalSource(httpClient).getMovieByTitle("Fight Club"))

        assertEquals(550, result.id)
    }

    // ==================== HTTP Error Handling tests ====================

    @Test
    fun `getPopularMovies should propagate HTTP 500 error`() = runTest {
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

        assertFailsWith<Exception> {
            TMDBMoviesExternalSource(httpClient).getPopularMovies()
        }
    }

    @Test
    fun `getMovieByTitle should propagate HTTP 500 error`() = runTest {
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

        assertFailsWith<Exception> {
            TMDBMoviesExternalSource(httpClient).getMovieByTitle("Test")
        }
    }

    @Test
    fun `getPopularMovies should propagate exception when response is malformed`() = runTest {
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

        assertFailsWith<Exception> {
            TMDBMoviesExternalSource(httpClient).getPopularMovies()
        }
    }

    @Test
    fun `getMovieByTitle should propagate exception when response is malformed`() = runTest {
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

        assertFailsWith<Exception> {
            TMDBMoviesExternalSource(httpClient).getMovieByTitle("Test")
        }
    }
}

