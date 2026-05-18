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
    fun `getMovieDetail delegates requested id and stores returned movie`() = runTest {

        val expectedMovie = movie(id = 10, title = "Title")
        val requestedIds = arrayListOf<Int>()
        val useCase = FakeGetMovieDetailsUseCase { id ->
            requestedIds += id
            if (id == expectedMovie.id) expectedMovie else null
        }
        val viewModel = DetailViewModel(useCase)

        viewModel.getMovieDetail(expectedMovie.id)

        assertEquals(listOf(expectedMovie.id), requestedIds)

        assertEquals(
            DetailViewModel.MovieDetailUiState(movie = expectedMovie),
            viewModel.movieDetailStateFlow.value,
        )
    }

    @Test
    fun `getMovieDetail keeps empty state when repository returns null`() = runTest {

        val requestedIds = arrayListOf<Int>()
        val useCase = FakeGetMovieDetailsUseCase { id ->
            requestedIds += id
            null
        }
        val viewModel = DetailViewModel(useCase)

        viewModel.getMovieDetail(99)

        assertEquals(listOf(99), requestedIds)

        assertEquals(DetailViewModel.MovieDetailUiState(), viewModel.movieDetailStateFlow.value)
    }
}