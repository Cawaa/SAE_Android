package dev.mobile.tpsae

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mobile.tpsae.ui.components.AppBottomNavBar
import dev.mobile.tpsae.ui.components.CategorySelector
import dev.mobile.tpsae.ui.components.SearchBar
import dev.mobile.tpsae.ui.screens.MovieListContent
import dev.mobile.tpsae.ui.theme.TpSaeTheme
import dev.mobile.tpsae.viewmodel.MainViewModel

class SearchActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TpSaeTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val category by viewModel.category.collectAsStateWithLifecycle()

                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("Recherche") })
                    },
                    // Ajout de la barre de navigation en bas
                    bottomBar = { AppBottomNavBar(currentScreen = "Search") }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        SearchBar(query = searchQuery, onQueryChange = viewModel::onSearchQueryChanged)
                        Spacer(modifier = Modifier.height(12.dp))
                        CategorySelector(selectedCategory = category, onCategorySelected = viewModel::onCategoryChanged)
                        Spacer(modifier = Modifier.height(12.dp))

                        MovieListContent(
                            uiState = uiState,
                            onMovieClick = { movie ->
                                val intent = Intent(this@SearchActivity, DetailActivity::class.java).apply {
                                    putExtra(DetailActivity.EXTRA_MOVIE, movie)
                                }
                                startActivity(intent)
                            },
                            onRetry = { viewModel.loadMoviesByCategory() }
                        )
                    }
                }
            }
        }
    }
}