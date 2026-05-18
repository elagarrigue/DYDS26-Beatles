package edu.dyds.movies.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Before

class GetMovieDetailsUseCaseImplTest {

    private lateinit var repositoryFake: MoviesRepositoryFake
    private lateinit var useCase: GetMovieDetailsUseCaseImpl

    @Before
    fun setUp() {
        repositoryFake = MoviesRepositoryFake()
        useCase = GetMovieDetailsUseCaseImpl(repositoryFake)
    }

    @Test
    fun `execute should delegate to repository with correct id and return null by default`() = runTest {
        val movieId = 42

        val result = useCase.execute(movieId)

        assertTrue(repositoryFake.getMovieDetailsCalled, "Repository should be called")
        assertNull(result, "Result should be null by default when no movieDetailResult is set")
    }

    @Test
    fun `execute should return movie when repository returns movie`() = runTest {
        val testMovie = TestMovieFactory.createTestMovie(
            id = 123,
            title = "Inception",
            voteAverage = 8.8
        )

        repositoryFake.movieDetailResult = testMovie

        val result = useCase.execute(123)

        assertEquals(testMovie, result, "Should return the movie from repository")
        assertEquals(123, result?.id, "Should return movie with correct id")
        assertEquals("Inception", result?.title, "Should return movie with correct title")
        assertEquals(8.8, result?.voteAverage, "Should return movie with correct vote average")
    }

    @Test
    fun `execute should return null when repository returns null`() = runTest {
        repositoryFake.movieDetailResult = null

        val result = useCase.execute(999)

        assertNull(result, "Should return null when movie not found")
    }

    @Test
    fun `execute should return null when repository throws exception`() = runTest {
        repositoryFake.shouldThrowException = true

        val result = useCase.execute(123)

        assertNull(result, "Should return null when exception occurs")
    }
}