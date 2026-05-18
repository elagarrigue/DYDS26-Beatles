package edu.dyds.movies.presentation.fakes

import edu.dyds.movies.domain.entity.QualifiedMovie
import edu.dyds.movies.domain.usecase.GetPopularMoviesUseCase

class FakeGetPopularMoviesUseCase(
    private val behaviour: suspend () -> List<QualifiedMovie>,
) : GetPopularMoviesUseCase {
    override suspend fun execute(): List<QualifiedMovie> = behaviour()
}
