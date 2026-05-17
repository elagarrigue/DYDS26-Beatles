package edu.dyds.movies.presentation.home

import edu.dyds.movies.domain.entity.QualifiedMovie
import edu.dyds.movies.presentation.fakes.FakeGetPopularMoviesUseCase
import edu.dyds.movies.test.installMainDispatcher
import edu.dyds.movies.test.movie
import edu.dyds.movies.test.resetMainDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun `getAllMovies delegates once and stores returned movies`() = runTest {
        installMainDispatcher()

        val expectedMovies = listOf(
            QualifiedMovie(movie(id = 1, title = "A"), isGoodMovie = true),
            QualifiedMovie(movie(id = 2, title = "B"), isGoodMovie = false),
        )
        val calls = arrayListOf<String>()
        val useCase = FakeGetPopularMoviesUseCase {
            calls += "execute"
            expectedMovies
        }
        val viewModel = HomeViewModel(useCase)

        viewModel.getAllMovies()

        assertEquals(listOf("execute"), calls)

        assertEquals(
            HomeViewModel.MoviesUiState(movies = expectedMovies),
            viewModel.moviesStateFlow.value,
        )
    }

    @Test
    fun `getAllMovies keeps empty state when use case returns empty list`() = runTest {
        installMainDispatcher()

        val calls = arrayListOf<String>()
        val useCase = FakeGetPopularMoviesUseCase {
            calls += "execute"
            emptyList()
        }
        val viewModel = HomeViewModel(useCase)

        viewModel.getAllMovies()

        assertEquals(listOf("execute"), calls)

        assertEquals(HomeViewModel.MoviesUiState(), viewModel.moviesStateFlow.value)
    }
}