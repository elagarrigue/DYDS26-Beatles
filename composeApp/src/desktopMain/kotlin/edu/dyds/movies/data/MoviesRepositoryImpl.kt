package edu.dyds.movies.data

import edu.dyds.movies.data.external.DetailedMovieSource
import edu.dyds.movies.data.external.PopularMoviesSource
import edu.dyds.movies.data.local.LocalDataSource
import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.repository.MoviesRepository

class MoviesRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val popularMoviesSource: PopularMoviesSource,
    private val detailedMovieSource: DetailedMovieSource,
) : MoviesRepository {

    override suspend fun getPopularMovies(): List<Movie> {
        localDataSource.getPopularMovies()?.let { localMovies ->
            return localMovies
        }

        return popularMoviesSource.getPopularMovies().also { remoteMovies ->
            localDataSource.savePopularMovies(remoteMovies.map { it.toDomainMovie() })
        }.map { it.toDomainMovie() }
    }

    override suspend fun getMovieByTitle(title: String): Movie? {
        // 1. Intentar obtener la película enriquecida del broker (TMDB+OMDB)
        val remoteMovie = detailedMovieSource.getMovieByTitle(title)
        val enrichedMovie = remoteMovie?.toDomainMovie() // Convertir RemoteMovie a Movie

        if (enrichedMovie != null) {
            // Si se obtiene una película enriquecida, actualizar el caché local
            val cachedMovies = localDataSource.getPopularMovies()
            val updatedMovies: List<Movie> = if (cachedMovies != null) {
                val mutableCachedMovies = cachedMovies.toMutableList()
                val existingMovieIndex = mutableCachedMovies.indexOfFirst { it.title == enrichedMovie.title }
                if (existingMovieIndex != -1) {
                    // Reemplazar la película existente
                    mutableCachedMovies[existingMovieIndex] = enrichedMovie
                } else {
                    // Añadir la nueva película
                    mutableCachedMovies.add(enrichedMovie)
                }
                mutableCachedMovies.toList() // Convert back to immutable list
            } else {
                // Inicializar el caché con la película enriquecida
                listOf(enrichedMovie)
            }
            localDataSource.savePopularMovies(updatedMovies)
            return enrichedMovie
        }

        // 2. Si el broker no la proporcionó, intentar obtenerla del caché local
        return localDataSource.getMovieByTitle(title)
    }
}
