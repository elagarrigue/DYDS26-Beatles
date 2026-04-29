package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.entity.QualifiedMovie

interface IGetPopularMoviesUseCase {
    suspend fun execute(): List<QualifiedMovie>
}

