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
            // map remote movies directly to domain Movie and cache them
            localDataSource.savePopularMovies(remoteMovies.map { it.toDomainMovie() })
        }.map { it.toDomainMovie() }
    }

    override suspend fun getMovieDetails(id: Int): Movie? {
        return localDataSource.getMovieDetails(id) ?: run {
            remoteDataSource.getMovieDetails(id).also { remoteMovie ->
                localDataSource.getPopularMovies()?.let { cachedMovies ->
                    if (cachedMovies.none { it.id == remoteMovie.id }) {
                        localDataSource.savePopularMovies(cachedMovies + remoteMovie.toDomainMovie())
                    }
                }
            }.toDomainMovie()
        }
    }
}

