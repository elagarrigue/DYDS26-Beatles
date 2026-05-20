package edu.dyds.movies.data

import edu.dyds.movies.data.external.RemoteDataSource
import edu.dyds.movies.data.local.LocalDataSource
import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.repository.MoviesRepository

class MoviesRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
) : MoviesRepository {

    override suspend fun getPopularMovies(): List<Movie> {
        localDataSource.getPopularMovies()?.let { localMovies ->
            return localMovies
        }

        return remoteDataSource.getPopularMovies().also { remoteMovies ->
            localDataSource.savePopularMovies(remoteMovies.map { it.toDomainMovie() })
        }.map { it.toDomainMovie() }
    }

    override suspend fun getMovieByTitle(title: String): Movie? {
        val localMovie = localDataSource.getMovieByTitle(title)
        if (localMovie != null) {
            return localMovie
        }

        return try {
            val remoteMovie = remoteDataSource.getMovieByTitle(title)
            val domainMovie = remoteMovie.toDomainMovie()
            val cachedMovies = localDataSource.getPopularMovies()
            if (cachedMovies != null) {
                localDataSource.savePopularMovies(cachedMovies + domainMovie)
            }
            domainMovie
        } catch (e: Exception) {
            throw e // Rethrow the exception
        }
    }
}