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
    fun `get movie details should return movie by id when it exists in cache`() {
        val targetMovie = movie(id = 42, title = "Expected movie")
        localDataSource.savePopularMovies(listOf(movie(id = 1), targetMovie, movie(id = 3)))

        val result = localDataSource.getMovieDetails(42)

        assertEquals(targetMovie, result)
    }

    @Test
    fun `get movie details should return null when id does not exist in cache`() {
        localDataSource.savePopularMovies(listOf(movie(id = 1), movie(id = 2)))

        val result = localDataSource.getMovieDetails(99)

        assertNull(result)
    }

    @Test
    fun `details miss implies id is not present in popular cache`() {
        localDataSource.savePopularMovies(listOf(movie(id = 1), movie(id = 2)))

        val missingId = 99
        val details = localDataSource.getMovieDetails(missingId)
        val popular = localDataSource.getPopularMovies()

        assertNull(details)
        assertTrue(popular?.none { it.id == missingId } == true)
    }

    @Test
    fun `save popular movies with empty list should clear cache`() {
        localDataSource.savePopularMovies(listOf(movie(id = 1)))

        localDataSource.savePopularMovies(emptyList())

        assertNull(localDataSource.getPopularMovies())
        assertNull(localDataSource.getMovieDetails(1))
    }
}


