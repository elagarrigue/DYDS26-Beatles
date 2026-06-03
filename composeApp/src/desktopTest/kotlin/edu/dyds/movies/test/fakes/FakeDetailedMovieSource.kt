package edu.dyds.movies.test.fakes

import edu.dyds.movies.data.external.DetailedMovieSource
import edu.dyds.movies.domain.entity.Movie

class FakeDetailedMovieSource(
    private val provider: suspend (String) -> Movie?,
) : DetailedMovieSource {
    override suspend fun getMovieByTitle(title: String): Movie? = provider(title)
}

