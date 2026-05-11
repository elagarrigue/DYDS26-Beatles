package edu.dyds.movies.presentation.detail

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.usecase.GetMovieDetailsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private class GetMovieDetailsUseCaseFake(
        private val behaviour: suspend (Int) -> Movie?
    ) : GetMovieDetailsUseCase {
        override suspend fun execute(id: Int): Movie? = behaviour(id)
    }

    @BeforeTest
    fun setup() {
    }

    @AfterTest
    fun tearDown() {
        try {
            Dispatchers.resetMain()
        } catch (_: Throwable) {}
    }

    @Test
    fun `initial state is not loading and movie null`() = runTest {
        val useCase = GetMovieDetailsUseCaseFake { null }
        val vm = DetailViewModel(useCase)

        val state = vm.movieDetailStateFlow.value
        assertEquals(false, state.isLoading)
        assertEquals(null, state.movie)
    }

    @Test
    fun `getMovieDetail updates state to loading then loaded with movie`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        val sample = Movie(
            id = 10,
            title = "Title",
            overview = "Overview",
            releaseDate = "2021-01-01",
            poster = "poster",
            backdrop = null,
            originalTitle = "Title",
            originalLanguage = "en",
            popularity = 12.0,
            voteAverage = 8.0
        )
        val useCase = GetMovieDetailsUseCaseFake { id -> if (id == 10) sample else null }
        val vm = DetailViewModel(useCase)

        vm.getMovieDetail(10)
        advanceUntilIdle()

        val final = vm.movieDetailStateFlow.value
        assertEquals(false, final.isLoading)
        assertEquals(sample, final.movie)
    }

    @Test
    fun `getMovieDetail with missing movie sets movie null`() = runTest {
        val useCase = GetMovieDetailsUseCaseFake { null }
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        val vm = DetailViewModel(useCase)

        vm.getMovieDetail(99)
        advanceUntilIdle()

        val final = vm.movieDetailStateFlow.value
        assertEquals(false, final.isLoading)
        assertEquals(null, final.movie)
    }
}




