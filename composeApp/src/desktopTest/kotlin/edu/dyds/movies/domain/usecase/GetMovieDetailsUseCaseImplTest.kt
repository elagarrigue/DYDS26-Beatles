package edu.dyds.movies.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetMovieDetailsUseCaseImplTest {

    @Test
    fun `execute should delegate to repository with correct id`() = runTest {
        // arrange
        val repositoryFake = MoviesRepositoryFake()
        val useCase = GetMovieDetailsUseCaseImpl(repositoryFake)
        val movieId = 42

        // act
        useCase.execute(movieId)

        // assert
        assertTrue(repositoryFake.getMovieDetailsCalled, "Repository should be called")
        assertEquals(movieId, repositoryFake.capturedMovieId, "Should pass correct movie id to repository")
    }

    @Test
    fun `execute should return movie when repository returns movie`() = runTest {
        // arrange
        val testMovie = TestMovieFactory.createTestMovie(
            id = 123,
            title = "Inception",
            voteAverage = 8.8
        )

        val repositoryFake = MoviesRepositoryFake()
        repositoryFake.movieDetailResult = testMovie

        val useCase = GetMovieDetailsUseCaseImpl(repositoryFake)

        // act
        val result = useCase.execute(123)

        // assert
        assertEquals(testMovie, result, "Should return the movie from repository")
        assertEquals(123, result?.id, "Should return movie with correct id")
        assertEquals("Inception", result?.title, "Should return movie with correct title")
        assertEquals(8.8, result?.voteAverage, "Should return movie with correct vote average")
    }

    @Test
    fun `execute should return null when repository returns null`() = runTest {
        // arrange
        val repositoryFake = MoviesRepositoryFake()
        repositoryFake.movieDetailResult = null

        val useCase = GetMovieDetailsUseCaseImpl(repositoryFake)

        // act
        val result = useCase.execute(999)

        // assert
        assertNull(result, "Should return null when movie not found")
    }

    @Test
    fun `execute should return null when repository throws exception`() = runTest {
        // arrange
        val repositoryFake = MoviesRepositoryFake()
        repositoryFake.shouldThrowException = true

        val useCase = GetMovieDetailsUseCaseImpl(repositoryFake)

        // act
        val result = useCase.execute(123)

        // assert
        assertNull(result, "Should return null when exception occurs")
    }

    @Test
    fun `execute should pass different ids correctly to repository`() = runTest {
        // arrange
        val repositoryFake = MoviesRepositoryFake()
        val useCase = GetMovieDetailsUseCaseImpl(repositoryFake)

        // act - first call
        useCase.execute(10)
        val firstId = repositoryFake.capturedMovieId

        // act - second call
        useCase.execute(20)
        val secondId = repositoryFake.capturedMovieId

        // assert
        assertEquals(10, firstId, "Should capture first id correctly")
        assertEquals(20, secondId, "Should capture second id correctly")
    }
}

