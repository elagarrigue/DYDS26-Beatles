package edu.dyds.movies.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.usecase.IGetMovieDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val getMovieDetailsUseCase: IGetMovieDetailsUseCase,
) : ViewModel() {

    private val movieDetailStateMutableStateFlow = MutableStateFlow(MovieDetailUiState())

    val movieDetailStateFlow: StateFlow<MovieDetailUiState> = movieDetailStateMutableStateFlow.asStateFlow()

    fun getMovieDetail(id: Int) {
        viewModelScope.launch {
            movieDetailStateMutableStateFlow.value = MovieDetailUiState(isLoading = true)
            movieDetailStateMutableStateFlow.value = MovieDetailUiState(
                isLoading = false,
                movie = getMovieDetailsUseCase.execute(id)
            )
        }
    }

    data class MovieDetailUiState(
        val isLoading: Boolean = false,
        val movie: Movie? = null,
    )
}

