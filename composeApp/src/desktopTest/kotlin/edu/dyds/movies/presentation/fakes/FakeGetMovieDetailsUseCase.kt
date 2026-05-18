package edu.dyds.movies.presentation.fakes

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.usecase.GetMovieDetailsUseCase

class FakeGetMovieDetailsUseCase(
    private val behaviour: suspend (Int) -> Movie?,
) : GetMovieDetailsUseCase {
    override suspend fun execute(id: Int): Movie? = behaviour(id)
}
