package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.entity.Movie

interface GetMovieDetailsUseCase {
    suspend fun execute(title: String): Movie?
}