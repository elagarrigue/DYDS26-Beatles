package edu.dyds.movies.data.external.omdb

import edu.dyds.movies.data.external.DetailedMovieSource
import edu.dyds.movies.domain.entity.Movie
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class OMDBMoviesExternalSource(
    private val httpClient: HttpClient,
) : DetailedMovieSource {

    override suspend fun getMovieByTitle(title: String): Movie? {
        val remoteResponse = httpClient
            .get("/") {
                parameter("t", title)
            }
            .body<OMDBRemoteMovie>()

        return remoteResponse.toDomainMovieOrNull()
    }
}
