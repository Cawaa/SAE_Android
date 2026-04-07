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

enum class OrderBy { DATE, RATING }

data class GenreOption(val id: Int, val label: String)

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

    private val _selectedYear = MutableStateFlow<Int?>(null)
    val selectedYear: StateFlow<Int?> = _selectedYear.asStateFlow()

    private val _selectedGenreId = MutableStateFlow<Int?>(null)
    val selectedGenreId: StateFlow<Int?> = _selectedGenreId.asStateFlow()

    private val _minRating = MutableStateFlow(0f)
    val minRating: StateFlow<Float> = _minRating.asStateFlow()

    private val _orderBy = MutableStateFlow(OrderBy.DATE)
    val orderBy: StateFlow<OrderBy> = _orderBy.asStateFlow()

    private val _isGlobalSearchMode = MutableStateFlow(false)

    private val _availableYears = MutableStateFlow<List<Int>>(emptyList())
    val availableYears: StateFlow<List<Int>> = _availableYears.asStateFlow()

    private val _availableGenres = MutableStateFlow<List<GenreOption>>(emptyList())
    val availableGenres: StateFlow<List<GenreOption>> = _availableGenres.asStateFlow()

    private var currentMovies: List<Movie> = emptyList()
    private val genreNamesById = mapOf(
        28 to "Action",
        12 to "Aventure",
        16 to "Animation",
        35 to "Comedie",
        80 to "Crime",
        99 to "Documentaire",
        18 to "Drame",
        10751 to "Famille",
        14 to "Fantastique",
        36 to "Histoire",
        27 to "Horreur",
        10402 to "Musique",
        9648 to "Mystere",
        10749 to "Romance",
        878 to "Science-fiction",
        10770 to "Telefilm",
        53 to "Thriller",
        10752 to "Guerre",
        37 to "Western"
    )

    init {
        // Debounce : attend 400ms après la dernière frappe avant d'appeler l'API
        // Évite un appel réseau à chaque caractère tapé
        viewModelScope.launch {
            _searchQuery
                .debounce(400L)
                .distinctUntilChanged()
                .collect {
                    if (_isGlobalSearchMode.value) {
                        refreshSearchResults()
                    } else {
                        val query = _searchQuery.value
                        if (query.isBlank()) loadMoviesByCategory()
                        else performSearch(query)
                    }
                }
        }
        loadMoviesByCategory()
    }

    /** Charge les films selon la catégorie active. */
    fun loadMoviesByCategory() {
        _isGlobalSearchMode.value = false
        viewModelScope.launch {
            _uiState.value = MovieUiState.Loading
            try {
                val response = when (_category.value) {
                    MovieCategory.POPULAR   -> repository.getPopularMovies()
                    MovieCategory.TOP_RATED -> repository.getTopRatedMovies()
                }
                currentMovies = response.results
                updateAvailableFilters(currentMovies)
                applyFiltersAndPublish()
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

    fun onYearFilterChanged(year: Int?) {
        _selectedYear.value = year
        if (_isGlobalSearchMode.value) refreshSearchResults() else applyFiltersAndPublish()
    }

    fun onGenreFilterChanged(genreId: Int?) {
        _selectedGenreId.value = genreId
        if (_isGlobalSearchMode.value) refreshSearchResults() else applyFiltersAndPublish()
    }

    fun onMinRatingChanged(value: Float) {
        _minRating.value = value
        if (_isGlobalSearchMode.value) refreshSearchResults() else applyFiltersAndPublish()
    }

    fun onOrderByChanged(value: OrderBy) {
        _orderBy.value = value
        if (_isGlobalSearchMode.value) refreshSearchResults() else applyFiltersAndPublish()
    }

    /** Active le mode Recherche globale : filtres appliqués côté API sur toute la base TMDB. */
    fun loadSearchCatalog() {
        _isGlobalSearchMode.value = true
        refreshSearchResults()
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = MovieUiState.Loading
            try {
                val response = repository.searchMovies(query)
                currentMovies = response.results
                updateAvailableFilters(currentMovies)
                applyFiltersAndPublish()
            } catch (e: Exception) {
                _uiState.value = MovieUiState.Error(e.message ?: "Erreur réseau")
            }
        }
    }

    private fun refreshSearchResults() {
        viewModelScope.launch {
            _uiState.value = MovieUiState.Loading
            try {
                val query = _searchQuery.value.trim()
                if (query.isBlank()) {
                    val sortBy = when (_orderBy.value) {
                        OrderBy.DATE -> "primary_release_date.desc"
                        OrderBy.RATING -> "vote_average.desc"
                    }

                    // Source de référence des options (année/genre) indépendante du tri actif.
                    // On utilise un tri stable/large pour éviter que la liste des années ne
                    // soit biaisée vers les pages les plus récentes quand l'ordre actif est DATE.
                    val discoverCatalog = repository.discoverMoviesAcrossPages(
                        maxPages = 5,
                        sortBy = "popularity.desc"
                    )
                    updateAvailableFilters(discoverCatalog)

                    val hasServerFilters = _selectedYear.value != null ||
                        _selectedGenreId.value != null ||
                        _minRating.value > 0f

                    currentMovies = if (hasServerFilters) {
                        repository.discoverMoviesAcrossPages(
                            maxPages = 5,
                            year = _selectedYear.value,
                            genreId = _selectedGenreId.value,
                            minRating = _minRating.value,
                            sortBy = sortBy
                        )
                    } else {
                        discoverCatalog
                    }
                } else {
                    val searched = repository.searchMoviesAcrossPages(query, maxPages = 5)
                    updateAvailableFilters(searched)
                    currentMovies = applyLocalFilters(searched)
                }
                _uiState.value = MovieUiState.Success(currentMovies)
            } catch (e: Exception) {
                _uiState.value = MovieUiState.Error(e.message ?: "Erreur réseau")
            }
        }
    }

    private fun applyFiltersAndPublish() {
        if (currentMovies.isEmpty()) {
            _uiState.value = MovieUiState.Success(emptyList())
            return
        }

        _uiState.value = MovieUiState.Success(applyLocalFilters(currentMovies))
    }

    private fun applyLocalFilters(source: List<Movie>): List<Movie> {
        val year = _selectedYear.value
        val genreId = _selectedGenreId.value
        val minRating = _minRating.value

        var filtered = source
        if (year != null) {
            filtered = filtered.filter { it.releaseDate?.take(4)?.toIntOrNull() == year }
        }
        if (genreId != null) {
            filtered = filtered.filter { it.genreIds.contains(genreId) }
        }
        if (minRating > 0f) {
            filtered = filtered.filter { it.voteAverage >= minRating.toDouble() }
        }

        return when (_orderBy.value) {
            OrderBy.DATE -> sortByDateDesc(filtered)
            OrderBy.RATING -> sortByRatingDesc(filtered)
        }
    }

    private fun sortByDateDesc(movies: List<Movie>): List<Movie> {
        return movies.sortedWith(
            compareBy<Movie> { releaseDateKey(it) == 0 }.thenByDescending { releaseDateKey(it) }
        )
    }

    private fun sortByRatingDesc(movies: List<Movie>): List<Movie> {
        return movies.sortedWith(
            compareBy<Movie> { it.voteAverage <= 0.0 }.thenByDescending { it.voteAverage }
        )
    }

    private fun releaseDateKey(movie: Movie): Int {
        val value = movie.releaseDate?.replace("-", "")?.toIntOrNull() ?: 0
        return value
    }

    private fun updateAvailableFilters(source: List<Movie>) {
        val years = source
            .mapNotNull { it.releaseDate?.take(4)?.toIntOrNull() }
            .distinct()
            .sortedDescending()
        _availableYears.value = years
        if (_selectedYear.value != null && _selectedYear.value !in years) {
            _selectedYear.value = null
        }

        val genreIds = source
            .flatMap { it.genreIds }
            .distinct()
        val genres = genreIds
            .map { id -> GenreOption(id, genreNamesById[id] ?: "Genre $id") }
            .sortedBy { it.label }
        _availableGenres.value = genres
        if (_selectedGenreId.value != null && genreIds.none { it == _selectedGenreId.value }) {
            _selectedGenreId.value = null
        }
    }
}