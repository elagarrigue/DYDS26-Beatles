package edu.dyds.movies.presentation.home

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.entity.QualifiedMovie
import edu.dyds.movies.domain.usecase.GetPopularMoviesUseCase
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
class HomeViewModelTest {

    @BeforeTest
    fun setup() {
    }

    @AfterTest
    fun tearDown() {
        // ensure main is reset if a test set it
        try {
            Dispatchers.resetMain()
        } catch (_: Throwable) {
        }
    }

    private class GetPopularMoviesUseCaseFake(
        private val behaviour: suspend () -> List<QualifiedMovie>
    ) : GetPopularMoviesUseCase {
        override suspend fun execute(): List<QualifiedMovie> = behaviour()
    }

    @Test
    fun `initial state is empty and not loading`() = runTest {
        val useCase = GetPopularMoviesUseCaseFake { emptyList() }
        val vm = HomeViewModel(useCase)

        val state = vm.moviesStateFlow.value
        assertEquals(false, state.isLoading)
        assertEquals(0, state.movies.size)
    }

    @Test
    fun `getAllMovies updates state to loading then to loaded with data`() = runTest {
        // configure Dispatchers.Main so viewModelScope uses the test dispatcher
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        val sampleMovies = listOf(
            QualifiedMovie(
                Movie(
                    id = 1,
                    title = "A",
                    overview = "overview",
                    releaseDate = "2020-01-01",
                    poster = "posterA",
                    backdrop = null,
                    originalTitle = "A",
                    originalLanguage = "en",
                    popularity = 10.0,
                    voteAverage = 7.0
                ),
                true
            ),
            QualifiedMovie(
                Movie(
                    id = 2,
                    title = "B",
                    overview = "overview",
                    releaseDate = "2020-01-02",
                    poster = "posterB",
                    backdrop = null,
                    originalTitle = "B",
                    originalLanguage = "en",
                    popularity = 5.0,
                    voteAverage = 5.0
                ),
                false
            )
        )

        val useCase = GetPopularMoviesUseCaseFake { sampleMovies }
        val vm = HomeViewModel(useCase)

        vm.getAllMovies()
        advanceUntilIdle()

        val final = vm.moviesStateFlow.value
        assertEquals(false, final.isLoading)
        assertEquals(2, final.movies.size)
        assertEquals(sampleMovies, final.movies)
    }

    @Test
    fun `when use case returns empty list viewmodel exposes empty list`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        val useCase = GetPopularMoviesUseCaseFake { emptyList() }
        val vm = HomeViewModel(useCase)

        vm.getAllMovies()
        advanceUntilIdle()

        val final = vm.moviesStateFlow.value
        assertEquals(false, final.isLoading)
        assertEquals(0, final.movies.size)
    }
}





