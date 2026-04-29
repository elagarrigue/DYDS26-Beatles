package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.entity.Movie

interface IGetMovieDetailsUseCase {
    suspend fun execute(id: Int): Movie?
}

