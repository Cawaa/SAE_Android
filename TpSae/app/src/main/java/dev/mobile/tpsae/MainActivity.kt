package dev.mobile.tpsae

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dev.mobile.tpsae.ui.screens.MovieListScreen
import dev.mobile.tpsae.ui.theme.TpSaeTheme
import dev.mobile.tpsae.viewmodel.MainViewModel

/**
 * Activité principale : point d'entrée de l'application.
 * Délègue toute la logique au ViewModel et aux Composables.
 */
class MainActivity : ComponentActivity() {

    // viewModels() crée le ViewModel lié au cycle de vie de cette activité
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TpSaeTheme {
                MovieListScreen(
                    viewModel    = viewModel,
                    onMovieClick = { movie ->
                        // Passage du film (Parcelable) à la deuxième activité via Intent
                        val intent = Intent(this, DetailActivity::class.java).apply {
                            putExtra(DetailActivity.EXTRA_MOVIE, movie)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}