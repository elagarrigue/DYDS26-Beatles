Cambio: se desea integrar otro servicio (https://www.omdbapi.com/) para incrementar los datos del detalle de una
película. Obtener la lista de películas no cambia. Esto implica:

· En vez de "get movie by id", vamos a hacer "get movie by title". El id de la lista de peliculas actual hace referencia
al id de TMDB, lo cual no tiene correlación en OMDB. A su vez, ambos servicios soportan la búsqueda de películas
por título.

· Lógica de construcción del objeto movie resultante:

o Si ambos servicios retornan resultados, se crea un objeto movie con propiedades combinadas:

private fun buildMovie(
tmdbMovie: Movie.MovieItem,
omdbMovie: Movie.MovieItem
) =
Movie.MovieItem(
id = tmdbMovie.id,
title = tmdbMovie.title,
overview = "TMDB: ${tmdbMovie.overview}\n\nOMDB: ${omdbMovie.overview}",
releaseDate = tmdbMovie.releaseDate,
poster = tmdbMovie.poster,
backdrop = tmdbMovie.backdrop,
originalTitle = tmdbMovie.originalTitle,
originalLanguage = tmdbMovie.originalLanguage,
popularity = (tmdbMovie.popularity + omdbMovie.popularity) / 2.0,
voteAverage = (tmdbMovie.voteAverage + omdbMovie.voteAverage) / 2.0
)

· Si sólo uno de los servicios retorna un resultado, retornar ese resultado. Modificar el overview agregando el string
"TMDB: " u "OMDB: " según corresponda.

· Si ninguno retorna un resultado, retornar vacío

Tareas (implementar en distintos commits):

· Reemplazar TMDB get movie details by id por search movie by title.
Incluye:

interface MoviesExternalSource {
    suspend fun getPopularMovies(): List<MovieItem>
    suspend fun getMovieById(id: Int): MovieItem
    suspend fun getMovieByTitle(title: String): MovieItem
}

@Serializable
data class RemoteMovie(
    val id: Int,
    val title: String,
    val overview: String,
    @SerialName("release_date") val releaseDate: String?,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("backdrop_path") val backdropPath: String?,
    @SerialName("original_title") val originalTitle: String,
    @SerialName("original_language") val originalLanguage: String,
    val popularity: Double?,
    @SerialName("vote_average") val voteAverage: Double?
)

fun toDomainMovie() = Movie.MovieItem(
    id = id,
    title = title,
    overview = overview,
    releaseDate = releaseDate ?: "",
    poster = "${TMDB_IMAGE_BASE_URL}/w185$posterPath",
    backdrop = backdropPath?.let { "${TMDB_IMAGE_BASE_URL}/w780$it" },
    originalTitle = originalTitle,
    originalLanguage = originalLanguage,
    popularity = popularity ?: 0.0,
    voteAverage = voteAverage ?: 0.0
)

internal class TMDBMoviesExternalSourceImpl(
    private val tmdbHttpClient: HttpClient,
) : MoviesExternalSource {
    override suspend fun getPopularMovies(): List<MovieItem> =
        getTMDBMovies().results.map { it.toDomainMovie() }

    override suspend fun getMovieById(id: Int): MovieItem =
        getTMDBMovieDetails(id).toDomainMovie()

    override suspend fun getMovieByTitle(title: String): MovieItem =
        getTMDBMovieDetails(title).results.first().toDomainMovie()

    private suspend fun getTMDBMovies(): RemoteResult =
        tmdbHttpClient.get("/3/discover/movie?sort_by=popularity.desc").body()

    private suspend fun getTMDBMovieDetails(id: Int): RemoteMovie =
        tmdbHttpClient.get("/3/movie/$id").body()

    private suspend fun getTMDBMovieDetails(title: String): RemoteResult =
        tmdbHttpClient.get("/3/search/movie?query=$title").body()
}

@Composable
fun DetailScreen(viewModel: DetailViewModel, title: String, onBack: () -> Unit) { ... }

private fun NavGraphBuilder.homeDestination(navController: NavHostController) {
    composable(HOME) {
        HomeScreen(
            viewModel = getHomeViewModel(),
            onGoodMovieClick = {
                navController.navigate("$DETAIL/${it.title}")
            }
        )
    }
}

private fun NavGraphBuilder.detailDestination(navController: NavHostController) {
    composable(
        route = "$DETAIL/{$MOVIE_TITLE}",
        arguments = listOf(navArgument(MOVIE_TITLE) { type = NavType.StringType })
    ) { backstackEntry ->
        val movieTitle = backstackEntry.arguments?.getString(MOVIE_TITLE)
    }
}

interface MoviesExternalSource {
    suspend fun getPopularMovies(): List<MovieItem>
}

interface MovieExternalSource {
    suspend fun getMovieByTitle(title: String): MovieItem
}

private const val OMDB_API_KEY = "a96e7f78"

private val omdbHttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(DefaultRequest) {
        url {
            protocol = URLProtocol.HTTPS
            host = "www.omdbapi.com"
            parameters.append("apikey", OMDB_API_KEY)
        }
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 5000
    }
}

@Serializable
data class RemoteMovie(
    @SerialName("Title") val title: String,
    @SerialName("Plot") val plot: String,
    @SerialName("Released") val released: String,
    @SerialName("Year") val year: String,
    @SerialName("Poster") val poster: String,
    @SerialName("Language") val language: String,
    @SerialName("Metascore") val metaScore: String,
    val imdbRating: Double,
) {
    fun toDomainMovie() = Movie.MovieItem(
        id = title.hashCode(),
        title = title,
        overview = plot,
        releaseDate = if (released.isNotEmpty() && released != "N/A") released else year,
        poster = poster,
        backdrop = poster,
        originalTitle = title,
        originalLanguage = language,
        popularity = imdbRating,
        voteAverage = if (metaScore.isNotEmpty() && metaScore != "N/A") metaScore.toDouble() else 0.0
    )
}

internal class OMDBMoviesExternalSource(
    private val omdbHttpClient: HttpClient,
) : MovieExternalSource {
    override suspend fun getMovieByTitle(title: String): Movie.MovieItem =
        getOMDBMovieDetails(title).toDomainMovie()

    private suspend fun getOMDBMovieDetails(title: String): RemoteMovie =
        omdbHttpClient.get(urlString = "/?t=$title").body()
}