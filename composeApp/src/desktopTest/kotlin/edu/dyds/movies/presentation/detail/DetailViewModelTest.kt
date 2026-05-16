package edu.dyds.movies.presentation.detail

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.usecase.GetMovieDetailsUseCase
import edu.dyds.movies.presentation.fakes.FakeGetMovieDetailsUseCase // Importar el fake externo
import edu.dyds.movies.test.installMainDispatcher
import edu.dyds.movies.test.movie
import edu.dyds.movies.test.resetMainDispatcher
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @AfterTest
    fun tearDown() {
        resetMainDispatcher()
    }

    @Test
    fun `initial state is not loading and movie null`() = runTest {
        val useCase = FakeGetMovieDetailsUseCase { null }
        val viewModel = DetailViewModel(useCase)

        assertFalse(viewModel.movieDetailStateFlow.value.isLoading)
        assertNull(viewModel.movieDetailStateFlow.value.movie)
    }

    @Test
    fun `getMovieDetail exposes loading while waiting for data and loaded movie afterwards`() = runTest {
        installMainDispatcher()

        val expectedMovie = movie(id = 10, title = "Title")
        val deferredMovie = CompletableDeferred<Movie?>()
        val useCase = FakeGetMovieDetailsUseCase { id ->
            if (id == expectedMovie.id) deferredMovie.await() else null
        }
        val viewModel = DetailViewModel(useCase)

        viewModel.getMovieDetail(expectedMovie.id)

        assertTrue(viewModel.movieDetailStateFlow.value.isLoading)

        deferredMovie.complete(expectedMovie)

        assertEquals(
            DetailViewModel.MovieDetailUiState(movie = expectedMovie),
            viewModel.movieDetailStateFlow.value,
        )
    }

    @Test
    fun `getMovieDetail with missing movie sets movie null`() = runTest {
        installMainDispatcher()

        val deferredMovie = CompletableDeferred<Movie?>()
        val useCase = FakeGetMovieDetailsUseCase { deferredMovie.await() }
        val viewModel = DetailViewModel(useCase)

        viewModel.getMovieDetail(99)

        assertTrue(viewModel.movieDetailStateFlow.value.isLoading)

        deferredMovie.complete(null)

        assertEquals(DetailViewModel.MovieDetailUiState(), viewModel.movieDetailStateFlow.value)
    }
}