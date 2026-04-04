package dev.mobile.tpsae.data

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
}