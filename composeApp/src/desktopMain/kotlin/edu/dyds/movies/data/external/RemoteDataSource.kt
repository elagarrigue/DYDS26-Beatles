package edu.dyds.movies.data.external

interface RemoteDataSource {
    suspend fun getPopularMovies(): List<RemoteMovie>
    suspend fun getMovieDetails(id: Int): RemoteMovie
}

