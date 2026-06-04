package edu.dyds.movies.data.external.tmdb

import edu.dyds.movies.domain.entity.Movie

private const val TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w185"

fun RemoteMovie.toDomainMovie(): Movie {
    val resolvedOverview = overview.ifBlank { "No overview available." }
    val resolvedReleaseDate = releaseDate.orEmpty()
    val resolvedPoster = posterPath?.let { "$TMDB_IMAGE_BASE$it" }.orEmpty()
    val resolvedBackdrop = backdropPath?.let { "$TMDB_IMAGE_BASE$it" }

    return Movie(
        id = id,
        title = title,
        overview = resolvedOverview,
        releaseDate = resolvedReleaseDate,
        poster = resolvedPoster,
        backdrop = resolvedBackdrop,
        originalTitle = originalTitle,
        originalLanguage = originalLanguage,
        popularity = popularity ?: 0.0,
        voteAverage = voteAverage ?: 0.0,
    )
}

