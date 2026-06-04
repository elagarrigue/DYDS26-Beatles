package edu.dyds.movies.presentation.fakes

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.usecase.GetMovieDetailsUseCase

class FakeGetMovieDetailsUseCase(
    private val behaviour: suspend (String) -> Movie?,
) : GetMovieDetailsUseCase {
    override suspend fun execute(title: String): Movie? = behaviour(title)
}