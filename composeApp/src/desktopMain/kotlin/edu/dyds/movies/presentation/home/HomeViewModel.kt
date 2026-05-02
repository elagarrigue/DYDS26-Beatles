package edu.dyds.movies.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.dyds.movies.domain.entity.QualifiedMovie
import edu.dyds.movies.domain.usecase.GetPopularMoviesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase,
) : ViewModel() {

    private val moviesStateMutableStateFlow = MutableStateFlow(MoviesUiState())

    val moviesStateFlow: StateFlow<MoviesUiState> = moviesStateMutableStateFlow.asStateFlow()

    fun getAllMovies() {
        viewModelScope.launch {
            moviesStateMutableStateFlow.value = MoviesUiState(isLoading = true)
            moviesStateMutableStateFlow.value = MoviesUiState(
                isLoading = false,
                movies = getPopularMoviesUseCase.execute()
            )
        }
    }

    data class MoviesUiState(
        val isLoading: Boolean = false,
        val movies: List<QualifiedMovie> = emptyList(),
    )
}

