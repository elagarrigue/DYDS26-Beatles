package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.CancellationException

class GetMovieDetailsUseCaseImpl(
    private val moviesRepository: MoviesRepository
) : GetMovieDetailsUseCase {
    override suspend fun execute(title: String): Movie? {
        return try {
            moviesRepository.getMovieByTitle(title)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }
}