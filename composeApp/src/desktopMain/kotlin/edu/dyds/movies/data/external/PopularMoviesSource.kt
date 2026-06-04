package edu.dyds.movies.data.external

import edu.dyds.movies.data.external.tmdb.RemoteMovie

interface PopularMoviesSource {
    suspend fun getPopularMovies(): List<RemoteMovie>
}

