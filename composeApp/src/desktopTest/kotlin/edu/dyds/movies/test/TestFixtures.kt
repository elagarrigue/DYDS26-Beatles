package edu.dyds.movies.test

import edu.dyds.movies.data.external.RemoteDataSource
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

fun remoteMovie(id: Int): RemoteMovie {
    return RemoteMovie(
        id = id,
        title = "Remote Movie $id",
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
    private val shouldThrowOnGetDetails: Boolean = false,
    private val detailsCache: Map<Int, Movie> = emptyMap(),
) : LocalDataSource {
    var getPopularMoviesCalls = 0
    var savePopularMoviesCalls = 0
    var getMovieDetailsCalls = 0
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

    override fun getMovieDetails(id: Int): Movie? {
        getMovieDetailsCalls++
        if (shouldThrowOnGetDetails) throw IllegalStateException("local getDetails error")
        return detailsCache[id] ?: cachedPopularMovies?.firstOrNull { it.id == id }
    }

    fun hasNoDuplicateIds(): Boolean {
        val saved = lastSavedPopularMovies ?: return true
        return saved.map { it.id }.distinct().size == saved.size
    }
}

class RemoteDataSourceFake(
    private val popularMoviesProvider: () -> List<RemoteMovie> = { emptyList() },
    private val movieDetailsProvider: (Int) -> RemoteMovie = { remoteMovie(it) },
    private val shouldThrowOnPopular: Boolean = false,
    private val shouldThrowOnDetails: Boolean = false,
) : RemoteDataSource {
    var getPopularMoviesCalls = 0
    var getMovieDetailsCalls = 0

    override suspend fun getPopularMovies(): List<RemoteMovie> {
        getPopularMoviesCalls++
        if (shouldThrowOnPopular) throw IllegalStateException("remote getPopular error")
        return popularMoviesProvider()
    }

    override suspend fun getMovieDetails(id: Int): RemoteMovie {
        getMovieDetailsCalls++
        if (shouldThrowOnDetails) throw IllegalStateException("remote getDetails error")
        return movieDetailsProvider(id)
    }
}



