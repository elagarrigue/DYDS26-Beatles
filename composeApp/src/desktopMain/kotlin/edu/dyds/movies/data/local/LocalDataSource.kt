package edu.dyds.movies.data.local

import edu.dyds.movies.data.external.RemoteMovie

class LocalDataSource {

    private val cacheMovies: MutableList<RemoteMovie> = mutableListOf()

    fun getPopularMovies(): List<RemoteMovie>? {
        return cacheMovies.takeIf { it.isNotEmpty() }?.toList()
    }

    fun savePopularMovies(movies: List<RemoteMovie>) {
        cacheMovies.clear()
        cacheMovies.addAll(movies)
    }

    fun getMovieDetails(id: Int): RemoteMovie? {
        return cacheMovies.firstOrNull { it.id == id }
    }
}

