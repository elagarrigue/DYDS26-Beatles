package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.repository.MoviesRepository

class MoviesRepositoryFake : MoviesRepository {

    var getMovieByTitleCalled: Boolean = false
    var capturedMovieTitle: String? = null
    var movieDetailResult: Movie? = null
    var shouldThrowException: Boolean = false

    override suspend fun getMovieByTitle(title: String): Movie? {
        getMovieByTitleCalled = true
        capturedMovieTitle = title
        if (shouldThrowException) {
            throw Exception("Test exception")
        }
        return movieDetailResult
    }

    override suspend fun getPopularMovies(): List<Movie> {
        TODO("Not yet implemented for this test")
    }
}
