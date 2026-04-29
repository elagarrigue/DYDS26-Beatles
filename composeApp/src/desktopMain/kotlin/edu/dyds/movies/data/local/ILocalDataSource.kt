package edu.dyds.movies.data.local

interface ILocalDataSource {
    fun getPopularMovies(): List<LocalMovie>?
    fun savePopularMovies(movies: List<LocalMovie>)
    fun getMovieDetails(id: Int): LocalMovie?
}

