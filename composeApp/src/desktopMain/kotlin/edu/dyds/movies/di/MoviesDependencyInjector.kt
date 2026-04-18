package edu.dyds.movies.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.dyds.movies.MoviesViewModel
import edu.dyds.movies.data.MoviesRepositoryImpl
import edu.dyds.movies.data.external.RemoteDataSource
import edu.dyds.movies.data.local.LocalDataSource
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

private const val API_KEY = "d18da1b5da16397619c688b0263cd281"

object MoviesDependencyInjector {

    private val tmdbHttpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            install(DefaultRequest) {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "api.themoviedb.org"
                    parameters.append("api_key", API_KEY)
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 5000
            }
        }

    private val localDataSource = LocalDataSource()

    private val remoteDataSource = RemoteDataSource(tmdbHttpClient)

    private val moviesRepository = MoviesRepositoryImpl(
        localDataSource = localDataSource,
        remoteDataSource = remoteDataSource,
    )

    // Se exponen para ser consumidos cuando estén implementados en Etapa 1.
    val getPopularMoviesUseCaseProvider = { /* GetPopularMoviesUseCase(moviesRepository) */ }
    val getMovieDetailsUseCaseProvider = { /* GetMovieDetailsUseCase(moviesRepository) */ }

    @Composable
    fun getMoviesViewModel(): MoviesViewModel {
        return viewModel { MoviesViewModel(moviesRepository) }
    }
}
