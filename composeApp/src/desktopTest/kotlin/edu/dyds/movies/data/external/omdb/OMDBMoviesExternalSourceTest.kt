package edu.dyds.movies.data.external.omdb

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class OMDBMoviesExternalSourceTest {

    private lateinit var httpClient: HttpClient

    @AfterTest
    fun tearDown() {
        httpClient.close()
    }

    private fun createTestHttpClient(responseBody: String): HttpClient {
        return HttpClient(MockEngine {
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

    @Test
    fun `getMovieByTitle should map OMDB payload to unified remote movie`() = runTest {
        val payload = OMDBRemoteMovie(
            title = "The Matrix",
            plot = "A hacker discovers reality.",
            released = "31 Mar 1999",
            year = "1999",
            poster = "https://img.omdb/matrix.jpg",
            language = "English",
            metaScore = "73",
            imdbRating = "8.7",
            response = "True",
        )
        httpClient = createTestHttpClient(Json.encodeToString(payload))

        val result = OMDBMoviesExternalSource(httpClient).getMovieByTitle("The Matrix")

        requireNotNull(result)
        assertEquals("The Matrix", result.title)
        assertEquals("A hacker discovers reality.", result.overview)
        assertEquals("31 Mar 1999", result.releaseDate)
        assertEquals("https://img.omdb/matrix.jpg", result.poster)
        assertEquals("https://img.omdb/matrix.jpg", result.backdrop)
        assertEquals("English", result.originalLanguage)
        assertEquals(7.3, result.popularity)
        assertEquals(8.7, result.voteAverage)
    }

    @Test
    fun `getMovieByTitle should return null when OMDB says response false`() = runTest {
        val payload = OMDBRemoteMovie(
            response = "False",
        )
        httpClient = createTestHttpClient(Json.encodeToString(payload))

        val result = OMDBMoviesExternalSource(httpClient).getMovieByTitle("Unknown")

        assertNull(result)
    }

    @Test
    fun `getMovieByTitle should fallback values when OMDB fields are NA`() = runTest {
        val payload = OMDBRemoteMovie(
            title = "Movie NA",
            plot = "N/A",
            released = "N/A",
            year = "2001",
            poster = "N/A",
            language = "",
            metaScore = "N/A",
            imdbRating = "N/A",
            response = "True",
        )
        httpClient = createTestHttpClient(Json.encodeToString(payload))

        val result = OMDBMoviesExternalSource(httpClient).getMovieByTitle("Movie NA")

        requireNotNull(result)
        assertEquals("Movie NA", result.title)
        assertEquals("N/A", result.overview)
        assertEquals("2001", result.releaseDate)
        assertEquals("", result.poster)
        assertEquals(null, result.backdrop)
        assertEquals("unknown", result.originalLanguage)
        assertEquals(0.0, result.popularity)
        assertEquals(0.0, result.voteAverage)
    }
}

