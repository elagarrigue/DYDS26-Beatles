package edu.dyds.movies.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.dyds.movies.config.AppConfig
import edu.dyds.movies.data.MoviesRepositoryImpl
import edu.dyds.movies.data.external.broker.DetailedMovieBrokerExternalSource
import edu.dyds.movies.data.external.omdb.OMDBMoviesExternalSource
import edu.dyds.movies.data.external.tmdb.TMDBMoviesExternalSource
import edu.dyds.movies.data.local.LocalDataSource
import edu.dyds.movies.data.local.LocalDataSourceImpl
import edu.dyds.movies.domain.usecase.GetMovieDetailsUseCase
import edu.dyds.movies.domain.usecase.GetMovieDetailsUseCaseImpl
import edu.dyds.movies.domain.usecase.GetPopularMoviesUseCase
import edu.dyds.movies.domain.usecase.GetPopularMoviesUseCaseImpl
import edu.dyds.movies.presentation.detail.DetailViewModel
import edu.dyds.movies.presentation.home.HomeViewModel
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

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
                    parameters.append("api_key", AppConfig.TMDB_API_KEY)
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = AppConfig.TMDB_REQUEST_TIMEOUT_MS
            }
        }

    private val omdbHttpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            install(DefaultRequest) {
                url {
                    takeFrom(AppConfig.OMDB_BASE_URL)
                    parameters.append("apikey", AppConfig.OMDB_API_KEY)
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = AppConfig.OMDB_REQUEST_TIMEOUT_MS
            }
        }

    private val localDataSource: LocalDataSource = LocalDataSourceImpl()

    private val tmdbMoviesExternalSource = TMDBMoviesExternalSource(tmdbHttpClient)
    private val omdbMoviesExternalSource = OMDBMoviesExternalSource(omdbHttpClient)
    private val detailedMovieBroker = DetailedMovieBrokerExternalSource(
        tmdbDetailedMovieSource = tmdbMoviesExternalSource,
        omdbDetailedMovieSource = omdbMoviesExternalSource,
    )

    private val moviesRepository = MoviesRepositoryImpl(
        localDataSource = localDataSource,
        popularMoviesSource = tmdbMoviesExternalSource,
        detailedMovieSource = detailedMovieBroker,
    )

    private val getPopularMoviesUseCase: GetPopularMoviesUseCase = GetPopularMoviesUseCaseImpl(moviesRepository)
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase = GetMovieDetailsUseCaseImpl(moviesRepository)

    @Composable
    fun getHomeViewModel(): HomeViewModel {
        return viewModel { HomeViewModel(getPopularMoviesUseCase) }
    }

    @Composable
    fun getDetailViewModel(): DetailViewModel {
        return viewModel { DetailViewModel(getMovieDetailsUseCase) }
    }
}
