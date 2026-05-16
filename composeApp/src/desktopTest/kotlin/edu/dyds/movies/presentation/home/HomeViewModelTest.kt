package edu.dyds.movies.presentation.home

import edu.dyds.movies.domain.entity.QualifiedMovie
import edu.dyds.movies.presentation.fakes.FakeGetPopularMoviesUseCase // Importar el fake externo
import edu.dyds.movies.test.installMainDispatcher
import edu.dyds.movies.test.movie
import edu.dyds.movies.test.resetMainDispatcher
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @AfterTest
    fun tearDown() {
        resetMainDispatcher()
    }

    @Test
    fun `initial state is empty and not loading`() = runTest {
        val useCase = FakeGetPopularMoviesUseCase { emptyList() }
        val viewModel = HomeViewModel(useCase)

        assertEquals(HomeViewModel.MoviesUiState(), viewModel.moviesStateFlow.value)
    }

    @Test
    fun `getAllMovies exposes loading while waiting for data and loaded data afterwards`() = runTest {
        installMainDispatcher()

        val expectedMovies = listOf(
            QualifiedMovie(movie(id = 1, title = "A"), isGoodMovie = true),
            QualifiedMovie(movie(id = 2, title = "B"), isGoodMovie = false),
        )
        val deferredMovies = CompletableDeferred<List<QualifiedMovie>>()
        val useCase = FakeGetPopularMoviesUseCase { deferredMovies.await() }
        val viewModel = HomeViewModel(useCase)

        viewModel.getAllMovies()

        assertTrue(viewModel.moviesStateFlow.value.isLoading)

        deferredMovies.complete(expectedMovies)

        assertEquals(
            HomeViewModel.MoviesUiState(movies = expectedMovies),
            viewModel.moviesStateFlow.value,
        )
    }

    @Test
    fun `when use case returns empty list viewmodel exposes empty list`() = runTest {
        installMainDispatcher()

        val deferredMovies = CompletableDeferred<List<QualifiedMovie>>()
        val useCase = FakeGetPopularMoviesUseCase { deferredMovies.await() }
        val viewModel = HomeViewModel(useCase)

        viewModel.getAllMovies()

        assertTrue(viewModel.moviesStateFlow.value.isLoading)

        deferredMovies.complete(emptyList())

        assertEquals(HomeViewModel.MoviesUiState(), viewModel.moviesStateFlow.value)
    }
}