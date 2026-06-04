package edu.dyds.movies.data.external.omdb

import edu.dyds.movies.domain.entity.Movie
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OMDBRemoteMovie(
    @SerialName("Title") val title: String? = null,
    @SerialName("Plot") val plot: String? = null,
    @SerialName("Released") val released: String? = null,
    @SerialName("Year") val year: String? = null,
    @SerialName("Poster") val poster: String? = null,
    @SerialName("Language") val language: String? = null,
    @SerialName("Metascore") val metaScore: String? = null,
    @SerialName("imdbRating") val imdbRating: String? = null,
    @SerialName("Response") val response: String,
)

internal fun OMDBRemoteMovie.toDomainMovieOrNull(): Movie? {
    if (!response.equals("True", ignoreCase = true)) return null

    val resolvedTitle = title?.takeIf { it.isNotBlank() } ?: return null
    val resolvedOverview = plot.orEmpty().ifBlank { "No overview available." }
    val releaseDate = released.takeUnless { it == null || it == "N/A" } ?: year.orEmpty()
    val resolvedPoster = poster.takeUnless { it == null || it == "N/A" }.orEmpty()
    val resolvedLanguage = language.orEmpty().ifBlank { "unknown" }
    val resolvedVoteAverage = imdbRating?.toDoubleOrNull() ?: 0.0
    val resolvedPopularity = metaScore?.toDoubleOrNull()?.div(10.0) ?: 0.0

    return Movie(
        id = resolvedTitle.hashCode(),
        title = resolvedTitle,
        overview = resolvedOverview,
        releaseDate = releaseDate,
        poster = resolvedPoster,
        backdrop = resolvedPoster.ifBlank { null },
        originalTitle = resolvedTitle,
        originalLanguage = resolvedLanguage,
        popularity = resolvedPopularity,
        voteAverage = resolvedVoteAverage,
    )
}
