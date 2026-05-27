package edu.dyds.movies.config

/**
 * Configuración centralizada de la aplicación.
 *
 * NOTA: En producción, las claves API deberían almacenarse en variables de entorno
 * o un archivo de configuración no versionado (agregado a .gitignore).
 */
object AppConfig {
    const val TMDB_BASE_URL = "https://api.themoviedb.org"
    const val TMDB_REQUEST_TIMEOUT_MS = 5000L
    const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p"
    const val TMDB_POSTER_WIDTH = "w185"
    const val TMDB_BACKDROP_WIDTH = "w780"

    const val OMDB_BASE_URL = "https://www.omdbapi.com"
    const val OMDB_REQUEST_TIMEOUT_MS = 5000L

    fun tmdbApiKey(): String = requireEnv("TMDB_API_KEY")

    fun omdbApiKey(): String = requireEnv("OMDB_API_KEY")

    private fun requireEnv(name: String): String {
        return (System.getenv(name) ?: System.getProperty(name))
            ?.takeIf { it.isNotBlank() }
            ?: error("Missing required environment variable: $name")
    }
}


