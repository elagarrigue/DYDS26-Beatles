package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.repository.MoviesRepository

class GetMovieDetailsUseCase(
    private val moviesRepository: MoviesRepository
) {
    suspend fun execute(id: Int): Movie? {
        return try {
            moviesRepository.getMovieDetails(id)?.toDomainMovie()
        } catch (e: Exception) {
            null
        }
    }
}
