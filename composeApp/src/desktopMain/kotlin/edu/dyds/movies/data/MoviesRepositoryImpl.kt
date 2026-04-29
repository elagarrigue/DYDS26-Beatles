package edu.dyds.movies.data

import edu.dyds.movies.data.external.IRemoteDataSource
import edu.dyds.movies.data.local.ILocalDataSource
import edu.dyds.movies.data.local.toDomainMovie
import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.repository.MoviesRepository

class MoviesRepositoryImpl(
    private val localDataSource: ILocalDataSource,
    private val remoteDataSource: IRemoteDataSource,
) : MoviesRepository {

    override suspend fun getPopularMovies(): List<Movie> {
        localDataSource.getPopularMovies()?.let { localMovies ->
            return localMovies.map { it.toDomainMovie() }
        }

        return remoteDataSource.getPopularMovies().also { movies ->
            localDataSource.savePopularMovies(movies.map { it.toLocalMovie() })
        }.map { it.toDomainMovie() }
    }

    override suspend fun getMovieDetails(id: Int): Movie? {
        return localDataSource.getMovieDetails(id)?.toDomainMovie() ?: run {
            remoteDataSource.getMovieDetails(id).also { remoteMovie ->
                localDataSource.getPopularMovies()?.let { cachedMovies ->
                    if (cachedMovies.none { it.id == remoteMovie.id }) {
                        localDataSource.savePopularMovies(cachedMovies + remoteMovie.toLocalMovie())
                    }
                }
            }.toDomainMovie()
        }
    }
}

