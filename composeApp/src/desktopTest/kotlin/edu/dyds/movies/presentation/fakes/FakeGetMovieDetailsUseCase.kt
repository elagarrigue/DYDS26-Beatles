package edu.dyds.movies.presentation.fakes

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.usecase.GetMovieDetailsUseCase

class FakeGetMovieDetailsUseCase(
    private val behaviour: suspend (String) -> Movie?, // Changed from Int to String
) : GetMovieDetailsUseCase {
    override suspend fun execute(title: String): Movie? = behaviour(title) // Changed from id to title
}