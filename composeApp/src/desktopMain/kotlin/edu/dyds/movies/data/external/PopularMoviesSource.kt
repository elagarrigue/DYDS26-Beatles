package edu.dyds.movies.data.external

interface PopularMoviesSource {
    suspend fun getPopularMovies(): List<RemoteMovie>
}

