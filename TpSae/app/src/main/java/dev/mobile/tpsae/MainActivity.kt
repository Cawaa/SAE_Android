package dev.mobile.tpsae

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mobile.tpsae.ui.components.AppBottomNavBar
import dev.mobile.tpsae.ui.components.CategorySelector
import dev.mobile.tpsae.ui.screens.MovieListContent
import dev.mobile.tpsae.ui.theme.TpSaeTheme
import dev.mobile.tpsae.viewmodel.MainViewModel
import dev.mobile.tpsae.viewmodel.MovieCategory

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TpSaeTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val selectedCategory by viewModel.category.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.onSearchQueryChanged("")
                    viewModel.onCategoryChanged(MovieCategory.POPULAR)
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Accueil") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    },
                    // Ajout de la barre de navigation en bas
                    bottomBar = { AppBottomNavBar(currentScreen = "Home") }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Decouvrir les films du moment",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Choisis une categorie rapide ou passe en recherche avancee.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        startActivity(Intent(this@MainActivity, SearchActivity::class.java))
                                    }
                                ) {
                                    Icon(Icons.Default.Explore, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Recherche avancee")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        CategorySelector(
                            selectedCategory = selectedCategory,
                            onCategorySelected = viewModel::onCategoryChanged
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                            MovieListContent(
                                uiState = uiState,
                                onMovieClick = { movie ->
                                    val intent = Intent(this@MainActivity, DetailActivity::class.java).apply {
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
}