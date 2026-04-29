package edu.dyds.movies.data.local

class LocalDataSource : ILocalDataSource {

    private val cacheMovies: MutableList<LocalMovie> = mutableListOf()

    override fun getPopularMovies(): List<LocalMovie>? {
        return cacheMovies.takeIf { it.isNotEmpty() }?.toList()
    }

    override fun savePopularMovies(movies: List<LocalMovie>) {
        cacheMovies.clear()
        cacheMovies.addAll(movies)
    }

    override fun getMovieDetails(id: Int): LocalMovie? {
        return cacheMovies.firstOrNull { it.id == id }
    }
}

