package edu.dyds.movies.data.external.tmdb

import edu.dyds.movies.data.external.DetailedMovieSource
import edu.dyds.movies.data.external.PopularMoviesSource
import edu.dyds.movies.domain.entity.Movie
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class TMDBMoviesExternalSource(
    private val httpClient: HttpClient,
) : PopularMoviesSource, DetailedMovieSource {

    override suspend fun getPopularMovies(): List<RemoteMovie> {
        return httpClient
            .get("/3/discover/movie?sort_by=popularity.desc")
            .body<RemoteResult>()
            .results
    }

    override suspend fun getMovieByTitle(title: String): Movie? {
        val results = httpClient
            .get("/3/search/movie") {
                parameter("query", title)
            }
            .body<RemoteResult>()
            .results

        return results.firstOrNull()?.toDomainMovie()
    }
}


