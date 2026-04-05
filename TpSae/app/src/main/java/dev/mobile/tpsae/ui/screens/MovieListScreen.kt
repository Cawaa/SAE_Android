package dev.mobile.tpsae.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import dev.mobile.tpsae.viewmodel.MovieUiState

/**
 * Écran principal : liste des films avec recherche et filtre.
 * Reçoit le ViewModel et un callback de navigation vers le détail.
 * collectAsStateWithLifecycle arrête la collecte quand l'UI n'est plus visible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    viewModel: MainViewModel,
    onMovieClick: (Movie) -> Unit
) {
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val category    by viewModel.category.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("🎬 CineApp") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SearchBar(
                query         = searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Le filtre de catégorie disparaît quand une recherche est active
            if (searchQuery.isBlank()) {
                CategorySelector(
                    selectedCategory   = category,
                    onCategorySelected = viewModel::onCategoryChanged
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            when (val state = uiState) {
                is MovieUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is MovieUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text  = "❌ ${state.message}",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadMoviesByCategory() }) {
                                Text("Réessayer")
                            }
                        }
                    }
                }

                is MovieUiState.Success -> {
                    if (state.movies.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Aucun film trouvé.")
                        }
                    } else {
                        // LazyColumn : ne compose que les items visibles à l'écran
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding      = PaddingValues(bottom = 16.dp)
                        ) {
                            items(
                                items = state.movies,
                                key   = { it.id } // key stable = recompositions optimisées
                            ) { movie ->
                                MovieCard(
                                    movie   = movie,
                                    onClick = { onMovieClick(movie) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}