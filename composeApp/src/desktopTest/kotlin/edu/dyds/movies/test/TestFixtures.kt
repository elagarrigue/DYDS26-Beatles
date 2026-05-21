package edu.dyds.movies.test

import edu.dyds.movies.data.external.DetailedMovieSource
import edu.dyds.movies.data.external.PopularMoviesSource
import edu.dyds.movies.data.external.RemoteMovie
import edu.dyds.movies.data.local.LocalDataSource
import edu.dyds.movies.domain.entity.Movie

fun movie(id: Int, title: String = "Movie $id"): Movie {
    return Movie(
        id = id,
        title = title,
        overview = "Overview $id",
        releaseDate = "2026-01-01",
        poster = "https://image.tmdb.org/t/p/w185/poster$id.jpg",
        backdrop = "https://image.tmdb.org/t/p/w780/backdrop$id.jpg",
        originalTitle = title,
        originalLanguage = "en",
        popularity = 100.0,
        voteAverage = 8.0,
    )
}

fun remoteMovie(id: Int, title: String = "Remote Movie $id"): RemoteMovie {
    return RemoteMovie(
        id = id,
        title = title,
        overview = "Remote overview $id",
        releaseDate = "2026-01-01",
        posterPath = "/poster$id.jpg",
        backdropPath = "/backdrop$id.jpg",
        originalTitle = "Original $id",
        originalLanguage = "en",
        popularity = 100.0,
        voteAverage = 8.0,
    )
}

class LocalDataSourceSpy(
    var cachedPopularMovies: List<Movie>? = null,
    private val shouldThrowOnGetPopular: Boolean = false,
    private val shouldThrowOnGetByTitle: Boolean = false,
) : LocalDataSource {
    var getPopularMoviesCalls = 0
    var savePopularMoviesCalls = 0
    var getMovieByTitleCalls = 0
    var lastSavedPopularMovies: List<Movie>? = null
        private set

    override fun getPopularMovies(): List<Movie>? {
        getPopularMoviesCalls++
        if (shouldThrowOnGetPopular) throw IllegalStateException("local getPopular error")
        return cachedPopularMovies
    }

    override fun savePopularMovies(movies: List<Movie>) {
        savePopularMoviesCalls++
        lastSavedPopularMovies = movies
        cachedPopularMovies = movies.takeIf { it.isNotEmpty() }
    }

    override fun getMovieByTitle(title: String): Movie? {
        getMovieByTitleCalls++
        if (shouldThrowOnGetByTitle) throw IllegalStateException("local getByTitle error")
        return cachedPopularMovies?.firstOrNull { it.title == title }
    }

    fun hasNoDuplicateIds(): Boolean {
        val saved = lastSavedPopularMovies ?: return true
        return saved.map { it.id }.distinct().size == saved.size
    }
}

class TMDBMoviesExternalSourceFake(
    private val popularMoviesProvider: () -> List<RemoteMovie> = { emptyList() },
    private val movieByTitleProvider: (String) -> RemoteMovie = { remoteMovie(0, it) },
    private val shouldThrowOnPopular: Boolean = false,
    private val shouldThrowOnByTitle: Boolean = false,
    private val shouldReturnEmptyResultsOnTitle: Boolean = false,
) : PopularMoviesSource, DetailedMovieSource {
    var getPopularMoviesCalls = 0
    var getMovieByTitleCalls = 0

    override suspend fun getPopularMovies(): List<RemoteMovie> {
        getPopularMoviesCalls++
        if (shouldThrowOnPopular) throw IllegalStateException("remote getPopular error")
        return popularMoviesProvider()
    }

    override suspend fun getMovieByTitle(title: String): RemoteMovie {
        getMovieByTitleCalls++
        if (shouldThrowOnByTitle) throw IllegalStateException("remote getByTitle error")
        if (shouldReturnEmptyResultsOnTitle) throw IllegalArgumentException("No movie found for title: $title")
        return movieByTitleProvider(title)
    }
}