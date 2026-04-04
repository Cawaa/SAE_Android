package dev.mobile.tpsae.data

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import dev.mobile.tpsae.BuildConfig

/**
 * Singleton centralisant la configuration du client HTTP Ktor et les constantes de l'API TMDB.
 * Un seul client HTTP est créé pour toute l'application (bonne pratique).
 */
object TmdbApi {

    const val BASE_URL        = "https://api.themoviedb.org/3"
    const val IMAGE_BASE_URL  = "https://image.tmdb.org/t/p/w500"   // affiches
    const val BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w780" // images de fond

    val API_KEY = BuildConfig.TMDB_API_KEY


    /**
     * Client Ktor configuré avec :
     * - ContentNegotiation : désérialisation automatique JSON → data class Kotlin
     * - Logging            : affiche les requêtes/réponses dans Logcat (debug)
     */
    val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true  // ignore les champs JSON inconnus du modèle
                coerceInputValues  = true // évite les crashs sur des null inattendus
            })
        }
        install(Logging) {
            level = LogLevel.HEADERS // passer à LogLevel.BODY pour voir le JSON complet
        }
    }
}