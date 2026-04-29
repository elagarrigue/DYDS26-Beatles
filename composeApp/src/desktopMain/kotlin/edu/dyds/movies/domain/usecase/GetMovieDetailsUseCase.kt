package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.repository.MoviesRepository

class GetMovieDetailsUseCase(
    private val moviesRepository: MoviesRepository
) : IGetMovieDetailsUseCase {
    override suspend fun execute(id: Int): Movie? {
        return try {
            moviesRepository.getMovieDetails(id)
        } catch (e: Exception) {
            null
        }
    }
}
