package edu.dyds.movies.data.local

import edu.dyds.movies.domain.entity.Movie

class LocalDataSourceImpl : LocalDataSource {

    private val cacheMovies: MutableList<Movie> = mutableListOf()

    override fun getPopularMovies(): List<Movie>? {
        return cacheMovies.takeIf { it.isNotEmpty() }?.toList()
    }

    override fun savePopularMovies(movies: List<Movie>) {
        cacheMovies.clear()
        cacheMovies.addAll(movies)
    }

    override fun getMovieDetails(id: Int): Movie? {
        return cacheMovies.firstOrNull { it.id == id }
    }
}

