package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.entity.Movie

object TestMovieFactory {
    fun createTestMovie(
        id: Int = 1,
        title: String = "Test Movie",
        overview: String = "Test overview",
        releaseDate: String = "2024-01-01",
        poster: String = "https://example.com/poster.jpg",
        backdrop: String? = "https://example.com/backdrop.jpg",
        originalTitle: String = "Test Movie",
        originalLanguage: String = "en",
        popularity: Double = 100.0,
        voteAverage: Double = 7.0
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

