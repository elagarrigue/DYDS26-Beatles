package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.repository.MoviesRepository

class MoviesRepositoryFake : MoviesRepository {
    var getPopularMoviesCalled = false
    var getMovieDetailsCalled = false

    var capturedMovieId: Int? = null

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