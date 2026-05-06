package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.repository.MoviesRepository

class MoviesRepositoryFake : MoviesRepository {
    // Banderas para verificar delegación
    var getPopularMoviesCalled = false
    var getMovieDetailsCalled = false

    // Capturador de parámetros
    var capturedMovieId: Int? = null

    // Comportamiento configurable
    var shouldThrowException = false
    var movieDetailResult: Movie? = null
    var popularMoviesResult: List<Movie> = emptyList()

    override suspend fun getPopularMovies(): List<Movie> {
        getPopularMoviesCalled = true
        if (shouldThrowException) throw Exception("Repository exception")
        return popularMoviesResult
    }

    override suspend fun getMovieDetails(id: Int): Movie? {
        getMovieDetailsCalled = true
        capturedMovieId = id
        if (shouldThrowException) throw Exception("Repository exception")
        return movieDetailResult
    }
}

