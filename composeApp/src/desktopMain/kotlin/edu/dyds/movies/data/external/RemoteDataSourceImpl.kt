package edu.dyds.movies.data.external

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class RemoteDataSourceImpl(
    private val tmdbHttpClient: HttpClient,
) : RemoteDataSource {
    override suspend fun getPopularMovies(): List<RemoteMovie> {
        return tmdbHttpClient
            .get("/3/discover/movie?sort_by=popularity.desc")
            .body<RemoteResult>()
            .results
    }

    override suspend fun getMovieDetails(id: Int): RemoteMovie {
        return tmdbHttpClient.get("/3/movie/$id").body()
    }
}

