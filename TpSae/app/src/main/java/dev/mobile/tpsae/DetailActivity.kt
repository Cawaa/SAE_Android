package dev.mobile.tpsae

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.mobile.tpsae.data.TmdbApi
import dev.mobile.tpsae.model.Movie
import dev.mobile.tpsae.ui.theme.TpSaeTheme

/**
 * Deuxième activité : affiche le détail d'un film.
 * Reçoit l'objet Movie via Parcelable depuis MainActivity.
 */
class DetailActivity : ComponentActivity() {

    companion object {
        const val EXTRA_MOVIE = "extra_movie"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Récupération compatible API 33+ et versions antérieures
        val movie: Movie? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_MOVIE, Movie::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_MOVIE)
        }

        setContent {
            TpSaeTheme {
                if (movie != null) {
                    MovieDetailScreen(movie = movie, onBack = { finish() })
                } else {
                    Text("Film introuvable.", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

/**
 * Composable de l'écran de détail.
 * Scrollable verticalement pour les synopsis longs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(movie: Movie, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails du film") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Grande image de couverture classique
            AsyncImage(
                model = "${TmdbApi.BACKDROP_BASE_URL}${movie.backdropPath ?: movie.posterPath}",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Crop
            )

            // Informations textuelles structurées simplement
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Note : ${String.format("%.1f", movie.voteAverage)}/10")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Date de sortie : ${movie.releaseDate ?: "Inconnue"}")
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Synopsis",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = movie.overview.ifBlank { "Aucun synopsis disponible pour ce film." },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
