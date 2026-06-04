package edu.dyds.movies.data

import edu.dyds.movies.data.external.DetailedMovieSource
import edu.dyds.movies.data.external.PopularMoviesSource
import edu.dyds.movies.data.external.tmdb.toDomainMovie
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
        val enrichedMovie = detailedMovieSource.getMovieByTitle(title)

        if (enrichedMovie != null) {
            val cachedMovies = localDataSource.getPopularMovies()
            val updatedMovies: List<Movie> = if (cachedMovies != null) {
                val mutableCachedMovies = cachedMovies.toMutableList()
                val existingMovieIndex = mutableCachedMovies.indexOfFirst { it.title == enrichedMovie.title }
                if (existingMovieIndex != -1) {
                    mutableCachedMovies[existingMovieIndex] = enrichedMovie
                } else {
                    mutableCachedMovies.add(enrichedMovie)
                }
                mutableCachedMovies.toList()
            } else {
                listOf(enrichedMovie)
            }
            localDataSource.savePopularMovies(updatedMovies)
            return enrichedMovie
        }

        return localDataSource.getMovieByTitle(title)
    }
}
