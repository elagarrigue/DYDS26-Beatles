package edu.dyds.movies.data.external

interface DetailedMovieSource {
    suspend fun getMovieByTitle(title: String): RemoteMovie
}

