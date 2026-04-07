package dev.mobile.tpsae.data

import dev.mobile.tpsae.model.MovieDetail
import dev.mobile.tpsae.model.Movie
import dev.mobile.tpsae.model.MovieResponse
import io.ktor.client.call.*
import io.ktor.client.request.*

/**
 * Couche d'accès aux données réseau.
 * Seule classe qui connaît l'API TMDB ; le ViewModel ne doit pas faire de HTTP directement.
 * Les fonctions sont suspend car les appels réseau sont asynchrones.
 */
class MovieRepository {

    private val client  = TmdbApi.client
    private val baseUrl = TmdbApi.BASE_URL
    private val apiKey  = TmdbApi.API_KEY

    /** Récupère la liste des films populaires (triés par popularité décroissante). */
    suspend fun getPopularMovies(page: Int = 1): MovieResponse {
        return client.get("$baseUrl/movie/popular") {
            parameter("api_key",  apiKey)
            parameter("language", "fr-FR")
            parameter("page",     page)
        }.body() // .body() désérialise automatiquement le JSON grâce au plugin ContentNegotiation
    }

    /** Recherche des films par titre. Retourne une liste paginée. */
    suspend fun searchMovies(query: String, page: Int = 1): MovieResponse {
        return client.get("$baseUrl/search/movie") {
            parameter("api_key",  apiKey)
            parameter("query",    query)
            parameter("language", "fr-FR")
            parameter("page",     page)
        }.body()
    }

    /**
     * Endpoint Discover : permet de filtrer sur l'ensemble de la base TMDB.
     * Utilisé par l'écran Recherche pour éviter de filtrer uniquement la page courante.
     */
    suspend fun discoverMovies(
        page: Int = 1,
        year: Int? = null,
        genreId: Int? = null,
        minRating: Float = 0f,
        sortBy: String = "primary_release_date.desc"
    ): MovieResponse {
        return client.get("$baseUrl/discover/movie") {
            parameter("api_key", apiKey)
            parameter("language", "fr-FR")
            parameter("page", page)
            parameter("sort_by", sortBy)
            year?.let { parameter("primary_release_year", it) }
            genreId?.let { parameter("with_genres", it) }
            if (minRating > 0f) {
                parameter("vote_average.gte", minRating)
            }
        }.body()
    }

    /** Agrège plusieurs pages Discover pour élargir la couverture des filtres disponibles. */
    suspend fun discoverMoviesAcrossPages(
        maxPages: Int = 5,
        year: Int? = null,
        genreId: Int? = null,
        minRating: Float = 0f,
        sortBy: String = "primary_release_date.desc"
    ): List<Movie> {
        val firstPage = discoverMovies(
            page = 1,
            year = year,
            genreId = genreId,
            minRating = minRating,
            sortBy = sortBy
        )
        if (firstPage.totalPages <= 1 || maxPages <= 1) return firstPage.results

        val all = mutableListOf<Movie>()
        all.addAll(firstPage.results)

        val lastPage = minOf(firstPage.totalPages, maxPages)
        for (page in 2..lastPage) {
            all.addAll(
                discoverMovies(
                    page = page,
                    year = year,
                    genreId = genreId,
                    minRating = minRating,
                    sortBy = sortBy
                ).results
            )
        }
        return all.distinctBy { it.id }
    }

    /** Agrège plusieurs pages de recherche pour élargir le périmètre des filtres locaux. */
    suspend fun searchMoviesAcrossPages(query: String, maxPages: Int = 5): List<Movie> {
        val firstPage = searchMovies(query, page = 1)
        if (firstPage.totalPages <= 1 || maxPages <= 1) return firstPage.results

        val all = mutableListOf<Movie>()
        all.addAll(firstPage.results)

        val lastPage = minOf(firstPage.totalPages, maxPages)
        for (page in 2..lastPage) {
            all.addAll(searchMovies(query, page = page).results)
        }
        return all
    }

    /**
     * Récupère les films les mieux notés.
     * Utile pour proposer un filtre supplémentaire dans l'UI.
     */
    suspend fun getTopRatedMovies(page: Int = 1): MovieResponse {
        return client.get("$baseUrl/movie/top_rated") {
            parameter("api_key",  apiKey)
            parameter("language", "fr-FR")
            parameter("page",     page)
        }.body()
    }

    /** Récupère le détail d'un film (runtime, genres, etc.). */
    suspend fun getMovieDetails(movieId: Int): MovieDetail {
        return client.get("$baseUrl/movie/$movieId") {
            parameter("api_key",  apiKey)
            parameter("language", "fr-FR")
        }.body()
    }
}