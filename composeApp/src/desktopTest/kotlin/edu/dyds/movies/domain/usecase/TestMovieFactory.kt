package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.entity.Movie

object TestMovieFactory {
    fun createTestMovie(
        id: Int = 0,
        title: String = "Test Movie",
        overview: String = "Test Overview",
        releaseDate: String = "2023-01-01",
        poster: String = "",
        backdrop: String? = null,
        originalTitle: String = "Test Original Title",
        originalLanguage: String = "en",
        popularity: Double = 0.0,
        voteAverage: Double = 0.0
    ): Movie {
        return Movie(
            id = id,
            title = title,
            overview = overview,
            releaseDate = releaseDate,
            poster = poster,
            backdrop = backdrop,
            originalTitle = originalTitle,
            originalLanguage = originalLanguage,
            popularity = popularity,
            voteAverage = voteAverage
        )
    }
}
