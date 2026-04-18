package edu.dyds.movies.domain.repository

import edu.dyds.movies.data.external.RemoteMovie

interface MoviesRepository {
    suspend fun getPopularMovies(): List<RemoteMovie>
    suspend fun getMovieDetails(id: Int): RemoteMovie?
}
