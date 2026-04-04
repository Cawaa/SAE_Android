package dev.mobile.tpsae.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Représente un film retourné par l'API TMDB.
 * @Serializable : permet la désérialisation JSON via kotlinx.serialization
 * @Parcelize   : génère automatiquement l'implémentation Parcelable
 *                pour passer l'objet entre activités via Intent.putExtra()
 */
@Parcelize
@Serializable
data class Movie(
    val id: Int,
    val title: String,
    // Les noms JSON en snake_case sont mappés avec @SerialName
    @SerialName("poster_path")   val posterPath: String?   = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    val overview: String                                    = "",
    @SerialName("vote_average")  val voteAverage: Double   = 0.0,
    @SerialName("release_date")  val releaseDate: String?  = null,
    // genre_ids n'existe que dans les réponses liste, pas dans le détail
    @SerialName("genre_ids")     val genreIds: List<Int>   = emptyList(),
    val popularity: Double                                  = 0.0
) : Parcelable

/**
 * Enveloppe de la réponse paginée de l'API TMDB.
 * Ex: /movie/popular ou /search/movie renvoient cet objet.
 */
@Serializable
data class MovieResponse(
    val page: Int,
    val results: List<Movie>,
    @SerialName("total_pages")   val totalPages: Int,
    @SerialName("total_results") val totalResults: Int
)