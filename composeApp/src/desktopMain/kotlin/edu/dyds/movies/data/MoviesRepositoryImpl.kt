package edu.dyds.movies.data

import edu.dyds.movies.data.external.RemoteDataSource
import edu.dyds.movies.data.external.RemoteMovie
import edu.dyds.movies.data.local.LocalDataSource
import edu.dyds.movies.domain.repository.MoviesRepository

class MoviesRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
) : MoviesRepository {

    override suspend fun getPopularMovies(): List<RemoteMovie> {
        localDataSource.getPopularMovies()?.let { return it }

        return remoteDataSource.getPopularMovies().also { movies ->
            localDataSource.savePopularMovies(movies)
        }
    }

    override suspend fun getMovieDetails(id: Int): RemoteMovie? {
        return localDataSource.getMovieDetails(id) ?: remoteDataSource.getMovieDetails(id)
    }
}
