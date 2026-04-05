package dev.mobile.tpsae.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mobile.tpsae.data.MovieRepository
import dev.mobile.tpsae.model.Movie
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** Les différents états possibles de l'UI (pattern UDF). */
sealed class MovieUiState {
    data object Loading : MovieUiState()
    data class Success(val movies: List<Movie>) : MovieUiState()
    data class Error(val message: String) : MovieUiState()
}

/** Catégorie de films sélectionnée. */
enum class MovieCategory { POPULAR, TOP_RATED }

/**
 * ViewModel principal.
 * Contient toute la logique métier, l'UI ne fait qu'observer les StateFlow.
 */
@OptIn(FlowPreview::class)
class MainViewModel : ViewModel() {

    private val repository = MovieRepository()

    // État de la liste de films exposé à l'UI
    private val _uiState = MutableStateFlow<MovieUiState>(MovieUiState.Loading)
    val uiState: StateFlow<MovieUiState> = _uiState.asStateFlow()

    // Texte de la barre de recherche
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Catégorie active (filtre)
    private val _category = MutableStateFlow(MovieCategory.POPULAR)
    val category: StateFlow<MovieCategory> = _category.asStateFlow()

    init {
        // Debounce : attend 400ms après la dernière frappe avant d'appeler l'API
        // Évite un appel réseau à chaque caractère tapé
        viewModelScope.launch {
            _searchQuery
                .debounce(400L)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) loadMoviesByCategory()
                    else performSearch(query)
                }
        }
        loadMoviesByCategory()
    }

    /** Charge les films selon la catégorie active. */
    fun loadMoviesByCategory() {
        viewModelScope.launch {
            _uiState.value = MovieUiState.Loading
            try {
                val response = when (_category.value) {
                    MovieCategory.POPULAR   -> repository.getPopularMovies()
                    MovieCategory.TOP_RATED -> repository.getTopRatedMovies()
                }
                _uiState.value = MovieUiState.Success(response.results)
            } catch (e: Exception) {
                _uiState.value = MovieUiState.Error(e.message ?: "Erreur réseau")
            }
        }
    }

    /** Appelé à chaque changement dans le champ de recherche. */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /** Change la catégorie et recharge immédiatement. */
    fun onCategoryChanged(newCategory: MovieCategory) {
        _category.value = newCategory
        _searchQuery.value = ""
        loadMoviesByCategory()
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = MovieUiState.Loading
            try {
                val response = repository.searchMovies(query)
                _uiState.value = MovieUiState.Success(response.results)
            } catch (e: Exception) {
                _uiState.value = MovieUiState.Error(e.message ?: "Erreur réseau")
            }
        }
    }
}