package edu.dyds.movies.data.external.broker

import edu.dyds.movies.data.external.DetailedMovieSource
import edu.dyds.movies.data.external.RemoteMovie
import kotlinx.coroutines.CancellationException

class DetailedMovieBrokerExternalSource(
    private val tmdbDetailedMovieSource: DetailedMovieSource,
    private val omdbDetailedMovieSource: DetailedMovieSource,
) : DetailedMovieSource {

    override suspend fun getMovieByTitle(title: String): RemoteMovie? {
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
    ): RemoteMovie? {
        return try {
            source.getMovieByTitle(title)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            null
        }
    }

    private fun buildCombinedMovie(
        tmdbMovie: RemoteMovie,
        omdbMovie: RemoteMovie,
    ): RemoteMovie {
        return tmdbMovie.copy(
            overview = "TMDB: ${tmdbMovie.overview}\n\nOMDB: ${omdbMovie.overview}",
            popularity = (tmdbMovie.popularity + omdbMovie.popularity) / 2.0,
            voteAverage = (tmdbMovie.voteAverage + omdbMovie.voteAverage) / 2.0,
        )
    }

    private fun RemoteMovie.withOverviewPrefix(sourceName: String): RemoteMovie {
        return copy(overview = "$sourceName: $overview")
    }
}

