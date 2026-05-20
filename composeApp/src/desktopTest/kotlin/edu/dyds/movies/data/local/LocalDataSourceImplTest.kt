package edu.dyds.movies.data.local

import edu.dyds.movies.test.movie
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocalDataSourceImplTest {

    private lateinit var localDataSource: LocalDataSourceImpl

    @BeforeTest
    fun setUp() {
        localDataSource = LocalDataSourceImpl()
    }

    @Test
    fun `get popular movies should return null when cache is empty`() {
        val result = localDataSource.getPopularMovies()

        assertNull(result)
    }

    @Test
    fun `save popular movies should persist movies and return a copy`() {
        val movies = listOf(movie(id = 1), movie(id = 2))

        localDataSource.savePopularMovies(movies)
        val firstRead = localDataSource.getPopularMovies()
        val secondRead = localDataSource.getPopularMovies()

        assertEquals(movies, firstRead)
        assertEquals(movies, secondRead)
    }

    @Test
    fun `get popular movies should return an independent copy from internal cache`() {
        val movies = listOf(movie(id = 1), movie(id = 2))

        localDataSource.savePopularMovies(movies)
        val firstRead = localDataSource.getPopularMovies()
        val mutatedCopy = firstRead?.toMutableList()
        mutatedCopy?.removeAt(0)

        assertEquals(movies, localDataSource.getPopularMovies())
    }

    @Test
    fun `save popular movies should replace previous cache`() {
        localDataSource.savePopularMovies(listOf(movie(id = 1), movie(id = 2)))
        localDataSource.savePopularMovies(listOf(movie(id = 3)))

        assertEquals(listOf(movie(id = 3)), localDataSource.getPopularMovies())
    }

    @Test
    fun `get movie by title should return movie when it exists in cache`() {
        val targetMovie = movie(id = 42, title = "Expected movie")
        localDataSource.savePopularMovies(listOf(movie(id = 1), targetMovie, movie(id = 3)))

        val result = localDataSource.getMovieByTitle("Expected movie")

        assertEquals(targetMovie, result)
    }

    @Test
    fun `get movie by title should return null when title does not exist in cache`() {
        localDataSource.savePopularMovies(listOf(movie(id = 1), movie(id = 2)))

        val result = localDataSource.getMovieByTitle("NonExistentTitle")

        assertNull(result)
    }

    @Test
    fun `details miss implies title is not present in popular cache`() {
        localDataSource.savePopularMovies(listOf(movie(id = 1), movie(id = 2, title = "Movie 2")))

        val missingTitle = "NonExistentTitle"
        val details = localDataSource.getMovieByTitle(missingTitle)
        val popular = localDataSource.getPopularMovies()

        assertNull(details)
        assertTrue(popular?.none { it.title == missingTitle } == true)
    }

    @Test
    fun `save popular movies with empty list should clear cache`() {
        localDataSource.savePopularMovies(listOf(movie(id = 1)))

        localDataSource.savePopularMovies(emptyList())

        assertNull(localDataSource.getPopularMovies())
        assertNull(localDataSource.getMovieByTitle("Movie 1"))
    }
}