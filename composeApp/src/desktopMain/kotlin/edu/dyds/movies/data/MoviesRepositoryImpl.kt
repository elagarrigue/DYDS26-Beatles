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
        val localMovie = localDataSource.getMovieByTitle(title)
        if (localMovie != null) {
            return localMovie
        }

        val remoteMovie = detailedMovieSource.getMovieByTitle(title)
        val domainMovie = remoteMovie.toDomainMovie()
        val cachedMovies = localDataSource.getPopularMovies()
        if (cachedMovies != null) {
            localDataSource.savePopularMovies(cachedMovies + domainMovie)
        }
        return domainMovie
    }
}