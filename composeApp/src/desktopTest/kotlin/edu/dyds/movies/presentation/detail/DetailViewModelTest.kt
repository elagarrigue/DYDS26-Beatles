package edu.dyds.movies.presentation.detail

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.usecase.GetMovieDetailsUseCase
import edu.dyds.movies.presentation.fakes.FakeGetMovieDetailsUseCase
import edu.dyds.movies.test.installMainDispatcher
import edu.dyds.movies.test.movie
import edu.dyds.movies.test.resetMainDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @BeforeTest
    fun setUp() {
        installMainDispatcher()
    }

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
    fun `getMovieDetail delegates requested title and stores returned movie`() = runTest {

        val expectedMovie = movie(id = 10, title = "Inception")
        val requestedTitles = arrayListOf<String>()
        val useCase = FakeGetMovieDetailsUseCase { title ->
            requestedTitles += title
            if (title == expectedMovie.title) expectedMovie else null
        }
        val viewModel = DetailViewModel(useCase)

        viewModel.getMovieDetail(expectedMovie.title)

        assertEquals(listOf(expectedMovie.title), requestedTitles)

        assertEquals(
            DetailViewModel.MovieDetailUiState(movie = expectedMovie),
            viewModel.movieDetailStateFlow.value,
        )
    }

    @Test
    fun `getMovieDetail keeps empty state when repository returns null`() = runTest {

        val requestedTitles = arrayListOf<String>()
        val useCase = FakeGetMovieDetailsUseCase { title ->
            requestedTitles += title
            null
        }
        val viewModel = DetailViewModel(useCase)

        viewModel.getMovieDetail("NonExistentMovie")

        assertEquals(listOf("NonExistentMovie"), requestedTitles)

        assertEquals(DetailViewModel.MovieDetailUiState(), viewModel.movieDetailStateFlow.value)
    }
}