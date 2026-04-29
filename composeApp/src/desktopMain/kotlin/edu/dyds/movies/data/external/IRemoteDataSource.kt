package edu.dyds.movies.data.external

interface IRemoteDataSource {
    suspend fun getPopularMovies(): List<RemoteMovie>
    suspend fun getMovieDetails(id: Int): RemoteMovie
}

