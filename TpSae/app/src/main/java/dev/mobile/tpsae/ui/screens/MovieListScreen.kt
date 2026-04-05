package dev.mobile.tpsae.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mobile.tpsae.model.Movie
import dev.mobile.tpsae.ui.components.CategorySelector
import dev.mobile.tpsae.ui.components.MovieCard
import dev.mobile.tpsae.ui.components.SearchBar
import dev.mobile.tpsae.viewmodel.MainViewModel
import dev.mobile.tpsae.viewmodel.MovieCategory
import dev.mobile.tpsae.viewmodel.MovieUiState

// --- 1. Conteneur principal avec les onglets de navigation ---
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onMovieClick: (Movie) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Accueil") },
                    label = { Text("Accueil") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Recherche") },
                    label = { Text("Recherche") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (selectedTab == 0) {
                HomeScreen(viewModel, onMovieClick)
            } else {
                SearchScreen(viewModel, onMovieClick)
            }
        }
    }
}

// --- 2. Page Accueil (Films du moment) ---
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onMovieClick: (Movie) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // À l'ouverture de l'accueil, on force l'affichage des films populaires
    LaunchedEffect(Unit) {
        viewModel.onSearchQueryChanged("")
        viewModel.onCategoryChanged(MovieCategory.POPULAR)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Films du moment",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        MovieListContent(uiState, onMovieClick, onRetry = { viewModel.loadMoviesByCategory() })
    }
}

// --- 3. Page Recherche (Recherche avancée + Filtres) ---
@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    onMovieClick: (Movie) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))

        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::onSearchQueryChanged
        )

        Spacer(modifier = Modifier.height(12.dp))

        CategorySelector(
            selectedCategory = category,
            onCategorySelected = viewModel::onCategoryChanged
        )

        Spacer(modifier = Modifier.height(12.dp))

        MovieListContent(uiState, onMovieClick, onRetry = { viewModel.loadMoviesByCategory() })
    }
}

// --- Composant réutilisable pour afficher la liste de films ---
@Composable
fun MovieListContent(
    uiState: MovieUiState,
    onMovieClick: (Movie) -> Unit,
    onRetry: () -> Unit
) {
    when (uiState) {
        is MovieUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is MovieUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Erreur de chargement", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onRetry) { Text("Réessayer") }
                }
            }
        }
        is MovieUiState.Success -> {
            if (uiState.movies.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucun résultat.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    items(uiState.movies, key = { it.id }) { movie ->
                        MovieCard(movie = movie, onClick = { onMovieClick(movie) })
                    }
                }
            }
        }
    }
}