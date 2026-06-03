package edu.dyds.movies.data.external.broker

import edu.dyds.movies.data.external.DetailedMovieSource
import edu.dyds.movies.domain.entity.Movie
import kotlinx.coroutines.CancellationException

class DetailedMovieBrokerExternalSource(
    private val tmdbDetailedMovieSource: DetailedMovieSource,
    private val omdbDetailedMovieSource: DetailedMovieSource,
) : DetailedMovieSource {

    override suspend fun getMovieByTitle(title: String): Movie? {
        val tmdbMovie = safeGetByTitle(tmdbDetailedMovieSource, title)
        val omdbMovie = safeGetByTitle(omdbDetailedMovieSource, title)

        return when {
            tmdbMovie != null && omdbMovie != null -> buildCombinedMovie(tmdbMovie, omdbMovie)
            tmdbMovie != null -> tmdbMovie.withOverviewPrefix("TMDB")
            omdbMovie != null -> omdbMovie.withOverviewPrefix("OMDB")
            else -> null
        }
    }

    private suspend fun safeGetByTitle(
        source: DetailedMovieSource,
        title: String,
    ): Movie? {
        return try {
            source.getMovieByTitle(title)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            null
        }
    }

    private fun buildCombinedMovie(
        tmdbMovie: Movie,
        omdbMovie: Movie,
    ): Movie {
        return tmdbMovie.copy(
            overview = "TMDB: ${tmdbMovie.overview}\n\nOMDB: ${omdbMovie.overview}",
            popularity = calculateAverage(tmdbMovie.popularity, omdbMovie.popularity),
            voteAverage = calculateAverage(tmdbMovie.voteAverage, omdbMovie.voteAverage),
        )
    }

    private fun calculateAverage(value1: Double, value2: Double): Double {
        return (value1 + value2) / 2.0
    }

    private fun Movie.withOverviewPrefix(sourceName: String): Movie {
        return copy(overview = "$sourceName: $overview")
    }
}
