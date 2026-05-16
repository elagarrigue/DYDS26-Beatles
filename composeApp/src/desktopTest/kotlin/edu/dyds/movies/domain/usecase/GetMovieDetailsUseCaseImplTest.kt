package edu.dyds.movies.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetMovieDetailsUseCaseImplTest {

    @Test
    fun `execute should delegate to repository with correct id and return null by default`() = runTest {
        val repositoryFake = MoviesRepositoryFake()
        val useCase = GetMovieDetailsUseCaseImpl(repositoryFake)
        val movieId = 42

        val result = useCase.execute(movieId)

        assertTrue(repositoryFake.getMovieDetailsCalled, "Repository should be called")
        assertEquals(movieId, repositoryFake.capturedMovieId, "Should pass correct movie id to repository")
        assertNull(result, "Result should be null by default when no movieDetailResult is set")
    }

    @Test
    fun `execute should return movie when repository returns movie`() = runTest {
        val testMovie = TestMovieFactory.createTestMovie(
            id = 123,
            title = "Inception",
            voteAverage = 8.8
        )

        val repositoryFake = MoviesRepositoryFake()
        repositoryFake.movieDetailResult = testMovie

        val useCase = GetMovieDetailsUseCaseImpl(repositoryFake)

        val result = useCase.execute(123)

        assertEquals(testMovie, result, "Should return the movie from repository")
        assertEquals(123, result?.id, "Should return movie with correct id")
        assertEquals("Inception", result?.title, "Should return movie with correct title")
        assertEquals(8.8, result?.voteAverage, "Should return movie with correct vote average")
    }

    @Test
    fun `execute should return null when repository returns null`() = runTest {
        val repositoryFake = MoviesRepositoryFake()
        repositoryFake.movieDetailResult = null

        val useCase = GetMovieDetailsUseCaseImpl(repositoryFake)

        val result = useCase.execute(999)

        assertNull(result, "Should return null when movie not found")
    }

    @Test
    fun `execute should return null when repository throws exception`() = runTest {
        val repositoryFake = MoviesRepositoryFake()
        repositoryFake.shouldThrowException = true

        val useCase = GetMovieDetailsUseCaseImpl(repositoryFake)

        val result = useCase.execute(123)

        assertNull(result, "Should return null when exception occurs")
    }

    @Test
    fun `execute should pass different ids correctly to repository`() = runTest {
        val repositoryFake = MoviesRepositoryFake()
        val useCase = GetMovieDetailsUseCaseImpl(repositoryFake)

        useCase.execute(10)
        val firstId = repositoryFake.capturedMovieId

        useCase.execute(20)
        val secondId = repositoryFake.capturedMovieId

        assertEquals(10, firstId, "Should capture first id correctly")
        assertEquals(20, secondId, "Should capture second id correctly")
    }
}