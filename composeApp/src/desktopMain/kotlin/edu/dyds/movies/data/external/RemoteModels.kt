
package edu.dyds.movies.data.external

import edu.dyds.movies.config.AppConfig
import edu.dyds.movies.domain.entity.Movie
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteResult(
    val page: Int,
    val results: List<RemoteMovie>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int,
)

@Serializable
data class RemoteMovie(
    val id: Int,
    val title: String,
    val overview: String,
    @SerialName("release_date") val releaseDate: String,
    @SerialName("poster_path") val posterPath: String,
    @SerialName("backdrop_path") val backdropPath: String?,
    @SerialName("original_title") val originalTitle: String,
    @SerialName("original_language") val originalLanguage: String,
    val popularity: Double,
    @SerialName("vote_average") val voteAverage: Double,
) {
    private fun resolveImageUrl(pathOrUrl: String?, width: String): String? {
        if (pathOrUrl.isNullOrBlank()) return null
        return if (pathOrUrl.startsWith("http")) {
            pathOrUrl
        } else {
            "${AppConfig.TMDB_IMAGE_BASE_URL}/$width$pathOrUrl"
        }
    }

    fun toDomainMovie(): Movie {
        return Movie(
            id = id,
            title = title,
            overview = overview,
            releaseDate = releaseDate,
            poster = resolveImageUrl(posterPath, AppConfig.TMDB_POSTER_WIDTH).orEmpty(),
            backdrop = resolveImageUrl(backdropPath, AppConfig.TMDB_BACKDROP_WIDTH),
            originalTitle = originalTitle,
            originalLanguage = originalLanguage,
            popularity = popularity,
            voteAverage = voteAverage,
        )
    }
}
