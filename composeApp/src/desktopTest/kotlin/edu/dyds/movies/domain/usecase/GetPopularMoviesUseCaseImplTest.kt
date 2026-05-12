package edu.dyds.movies.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetPopularMoviesUseCaseImplTest {

    @Test
    fun `execute should delegate to repository`() = runTest {
        val repositoryFake = MoviesRepositoryFake()
        val useCase = GetPopularMoviesUseCaseImpl(repositoryFake)

        useCase.execute()

        assertTrue(repositoryFake.getPopularMoviesCalled, "Repository should be called")
    }

    @Test
    fun `execute should return movies sorted by vote average descending`() = runTest {
        val movie1 = TestMovieFactory.createTestMovie(id = 1, voteAverage = 5.0)
        val movie2 = TestMovieFactory.createTestMovie(id = 2, voteAverage = 8.5)
        val movie3 = TestMovieFactory.createTestMovie(id = 3, voteAverage = 7.0)

        val repositoryFake = MoviesRepositoryFake()
        repositoryFake.popularMoviesResult = listOf(movie1, movie2, movie3)

        val useCase = GetPopularMoviesUseCaseImpl(repositoryFake)

        val result = useCase.execute()

        assertEquals(3, result.size, "Should return all movies")
        assertEquals(8.5, result[0].movie.voteAverage, "First movie should have highest vote average")
        assertEquals(7.0, result[1].movie.voteAverage, "Second movie should have middle vote average")
        assertEquals(5.0, result[2].movie.voteAverage, "Third movie should have lowest vote average")
    }

    @Test
    fun `execute should mark movie as good when vote average is greater or equal to 6`() = runTest {
        val goodMovie = TestMovieFactory.createTestMovie(id = 1, voteAverage = 6.0)
        val badMovie = TestMovieFactory.createTestMovie(id = 2, voteAverage = 5.9)

        val repositoryFake = MoviesRepositoryFake()
        repositoryFake.popularMoviesResult = listOf(goodMovie, badMovie)

        val useCase = GetPopularMoviesUseCaseImpl(repositoryFake)

        val result = useCase.execute()

        assertTrue(result[0].isGoodMovie, "Movie with 6.0 rating should be marked as good")
        assertFalse(result[1].isGoodMovie, "Movie with 5.9 rating should NOT be marked as good")
    }

    @Test
    fun `execute should return empty list when repository returns empty list`() = runTest {
        val repositoryFake = MoviesRepositoryFake()
        repositoryFake.popularMoviesResult = emptyList()

        val useCase = GetPopularMoviesUseCaseImpl(repositoryFake)

        val result = useCase.execute()

        assertEquals(0, result.size, "Should return empty list")
        assertTrue(result.isEmpty(), "Result should be empty")
    }

    @Test
    fun `execute should return empty list when repository throws exception`() = runTest {
        val repositoryFake = MoviesRepositoryFake()
        repositoryFake.shouldThrowException = true

        val useCase = GetPopularMoviesUseCaseImpl(repositoryFake)

        val result = useCase.execute()

        assertEquals(0, result.size, "Should return empty list on exception")
        assertTrue(result.isEmpty(), "Result should be empty when exception occurs")
    }
}






