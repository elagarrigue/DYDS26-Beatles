package edu.dyds.movies.config

/**
 * Configuración centralizada de la aplicación.
 *
 * Las claves API se leen desde variables de entorno. Si no están disponibles,
 * se usan valores por defecto (que deberían reemplazarse con valores válidos).
 */
object AppConfig {
    val TMDB_API_KEY: String = System.getenv("TMDB_API_KEY") ?: "YOUR_TMDB_API_KEY_HERE"
    val OMDB_API_KEY: String = System.getenv("OMDB_API_KEY") ?: "YOUR_OMDB_API_KEY_HERE"

    const val TMDB_BASE_URL = "https://api.themoviedb.org"
    const val TMDB_REQUEST_TIMEOUT_MS = 5000L
    const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p"
    const val TMDB_POSTER_WIDTH = "w185"
    const val TMDB_BACKDROP_WIDTH = "w780"

    const val OMDB_BASE_URL = "https://www.omdbapi.com"
    const val OMDB_REQUEST_TIMEOUT_MS = 5000L
}


