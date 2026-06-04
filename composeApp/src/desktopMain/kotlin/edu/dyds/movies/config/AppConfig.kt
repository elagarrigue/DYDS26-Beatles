package edu.dyds.movies.config

import java.io.File
import java.util.Properties

object AppConfig {
    private val localProperties: Properties by lazy { loadLocalProperties() }

    private fun loadLocalProperties(): Properties {
        val properties = Properties()
        val localFile = File(System.getProperty("user.dir"), "local.properties")
        if (localFile.exists()) {
            localFile.inputStream().use { properties.load(it) }
        }
        return properties
    }

    private fun resolveKey(name: String): String? {
        return System.getenv(name)
            ?: System.getProperty(name)
            ?: localProperties.getProperty(name)
    }

    val TMDB_API_KEY: String = resolveKey("TMDB_API_KEY") ?: "d18da1b5da16397619c688b0263cd281"
    val OMDB_API_KEY: String = resolveKey("OMDB_API_KEY") ?: "a96e7f78"

    const val TMDB_BASE_URL = "https://api.themoviedb.org"
    const val TMDB_REQUEST_TIMEOUT_MS = 5000L
    const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p"
    const val TMDB_POSTER_WIDTH = "w185"
    const val TMDB_BACKDROP_WIDTH = "w780"

    const val OMDB_BASE_URL = "https://www.omdbapi.com"
    const val OMDB_REQUEST_TIMEOUT_MS = 5000L
}
