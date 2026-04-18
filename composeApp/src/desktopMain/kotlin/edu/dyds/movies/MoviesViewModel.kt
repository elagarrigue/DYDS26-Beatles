package edu.dyds.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.dyds.movies.data.MoviesRepositoryImpl
import edu.dyds.movies.data.external.RemoteMovie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private const val MIN_VOTE_AVERAGE = 6.0

class MoviesViewModel(
    private val moviesRepository: MoviesRepositoryImpl,
) : ViewModel() {

    private val moviesStateMutableStateFlow = MutableStateFlow(MoviesUiState())

    private val movieDetailStateMutableStateFlow = MutableStateFlow(MovieDetailUiState())

    val moviesStateFlow: Flow<MoviesUiState> = moviesStateMutableStateFlow

    val movieDetailStateFlow: Flow<MovieDetailUiState> = movieDetailStateMutableStateFlow

    fun getAllMovies() {
        viewModelScope.launch {
            moviesStateMutableStateFlow.emit(
                MoviesUiState(isLoading = true)
            )
            moviesStateMutableStateFlow.emit(
                MoviesUiState(
                    isLoading = false,
                    movies = getPopularMovies().sortAndMap()
                )
            )
        }
    }

    fun getMovieDetail(id: Int) {
        viewModelScope.launch {
            movieDetailStateMutableStateFlow.emit(
                MovieDetailUiState(isLoading = true)
            )
            movieDetailStateMutableStateFlow.emit(
                MovieDetailUiState(
                    isLoading = false,
                    movie = getMovieDetails(id)?.toDomainMovie()
                )
            )
        }
    }

    private suspend fun getPopularMovies() =
        try {
            moviesRepository.getPopularMovies()
        } catch (e: Exception) {
            emptyList()
        }

    private fun List<RemoteMovie>.sortAndMap(): List<QualifiedMovie> {
        return this
            .sortedByDescending { it.voteAverage }
            .map {
                QualifiedMovie(
                    movie = it.toDomainMovie(),
                    isGoodMovie = it.voteAverage >= MIN_VOTE_AVERAGE
                )
            }
    }

    private suspend fun getMovieDetails(id: Int) =
        try {
            moviesRepository.getMovieDetails(id)
        } catch (e: Exception) {
            null
        }


    data class MoviesUiState(
        val isLoading: Boolean = false,
        val movies: List<QualifiedMovie> = emptyList(),
    )

    data class MovieDetailUiState(
        val isLoading: Boolean = false,
        val movie: Movie? = null,
    )
}