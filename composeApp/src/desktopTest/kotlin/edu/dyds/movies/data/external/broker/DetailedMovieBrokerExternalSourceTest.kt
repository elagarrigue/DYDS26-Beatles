package edu.dyds.movies.data.external.broker

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.test.fakes.FakeDetailedMovieSource
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import java.util.concurrent.CancellationException

class DetailedMovieBrokerExternalSourceTest {

    private lateinit var tmdbMovie: Movie
    private lateinit var omdbMovie: Movie

    @BeforeTest
    fun setUp() {
        tmdbMovie = Movie(
            id = 1,
            title = "Inception",
            overview = "Dreams inside dreams.",
            releaseDate = "2010-07-16",
            poster = "https://image.tmdb.org/t/p/w185/inception.jpg",
            backdrop = "https://image.tmdb.org/t/p/w185/inception_backdrop.jpg",
            originalTitle = "Inception",
            originalLanguage = "en",
            popularity = 90.0,
            voteAverage = 8.0,
        )

        omdbMovie = Movie(
            id = 10,
            title = "Inception",
            overview = "A thief who steals corporate secrets.",
            releaseDate = "16 Jul 2010",
            poster = "https://img.omdb/inception.jpg",
            backdrop = "https://img.omdb/inception.jpg",
            originalTitle = "Inception",
            originalLanguage = "English",
            popularity = 80.0,
            voteAverage = 7.0,
        )
    }

    @Test
    fun `getMovieByTitle should combine movies when both sources return data`() = runTest {
        val broker = DetailedMovieBrokerExternalSource(
            tmdbDetailedMovieSource = FakeDetailedMovieSource { tmdbMovie },
            omdbDetailedMovieSource = FakeDetailedMovieSource { omdbMovie },
        )

        val result = broker.getMovieByTitle("Inception")

        requireNotNull(result)
        assertEquals(tmdbMovie.id, result.id)
        assertEquals(tmdbMovie.title, result.title)
        assertEquals("TMDB: ${tmdbMovie.overview}\n\nOMDB: ${omdbMovie.overview}", result.overview)
        val tmdbPopularity = requireNotNull(tmdbMovie.popularity)
        val omdbPopularity = requireNotNull(omdbMovie.popularity)
        assertEquals((tmdbPopularity + omdbPopularity) / 2.0, result.popularity)
        val tmdbVoteAverage = requireNotNull(tmdbMovie.voteAverage)
        val omdbVoteAverage = requireNotNull(omdbMovie.voteAverage)
        assertEquals((tmdbVoteAverage + omdbVoteAverage) / 2.0, result.voteAverage)
    }

    @Test
    fun `getMovieByTitle should return TMDB movie with prefixed overview when OMDB has no data`() = runTest {
        val broker = DetailedMovieBrokerExternalSource(
            tmdbDetailedMovieSource = FakeDetailedMovieSource { tmdbMovie },
            omdbDetailedMovieSource = FakeDetailedMovieSource { null },
        )

        val result = broker.getMovieByTitle("Inception")

        requireNotNull(result)
        assertEquals("TMDB: ${tmdbMovie.overview}", result.overview)
        assertEquals(tmdbMovie.id, result.id)
    }

    @Test
    fun `getMovieByTitle should return OMDB movie with prefixed overview when TMDB has no data`() = runTest {
        val broker = DetailedMovieBrokerExternalSource(
            tmdbDetailedMovieSource = FakeDetailedMovieSource { null },
            omdbDetailedMovieSource = FakeDetailedMovieSource { omdbMovie },
        )

        val result = broker.getMovieByTitle("Inception")

        requireNotNull(result)
        assertEquals("OMDB: ${omdbMovie.overview}", result.overview)
        assertEquals(omdbMovie.id, result.id)
    }

    @Test
    fun `getMovieByTitle should return null when both sources have no data`() = runTest {
        val broker = DetailedMovieBrokerExternalSource(
            tmdbDetailedMovieSource = FakeDetailedMovieSource { null },
            omdbDetailedMovieSource = FakeDetailedMovieSource { null },
        )

        val result = broker.getMovieByTitle("Unknown")

        assertNull(result)
    }

    @Test
    fun `getMovieByTitle should tolerate source errors and fallback to available source`() = runTest {
        val broker = DetailedMovieBrokerExternalSource(
            tmdbDetailedMovieSource = FakeDetailedMovieSource { throw IllegalStateException("tmdb down") },
            omdbDetailedMovieSource = FakeDetailedMovieSource { omdbMovie },
        )

        val result = broker.getMovieByTitle("Inception")

        requireNotNull(result)
        assertEquals("OMDB: ${omdbMovie.overview}", result.overview)
    }

    @Test
    fun `getMovieByTitle should rethrow cancellation exception`() = runTest {
        val broker = DetailedMovieBrokerExternalSource(
            tmdbDetailedMovieSource = FakeDetailedMovieSource { throw CancellationException("cancelled") },
            omdbDetailedMovieSource = FakeDetailedMovieSource { omdbMovie },
        )

        assertFailsWith<CancellationException> {
            broker.getMovieByTitle("Inception")
        }
    }

}
