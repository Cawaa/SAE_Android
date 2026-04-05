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
                title = { Text(movie.title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
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
                .verticalScroll(rememberScrollState())
        ) {
            // Image de fond en haut
            AsyncImage(
                model              = "${TmdbApi.BACKDROP_BASE_URL}${movie.backdropPath ?: movie.posterPath}",
                contentDescription = "Backdrop de ${movie.title}",
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier                = Modifier.fillMaxWidth(),
                    horizontalArrangement   = Arrangement.spacedBy(12.dp),
                    verticalAlignment       = Alignment.Top
                ) {
                    // Miniature de l'affiche
                    AsyncImage(
                        model              = "${TmdbApi.IMAGE_BASE_URL}${movie.posterPath}",
                        contentDescription = null,
                        modifier           = Modifier
                            .width(90.dp)
                            .height(135.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = movie.title,
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        movie.releaseDate?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text  = "📅 $it",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector        = Icons.Default.Star,
                                contentDescription = "Note",
                                tint               = Color(0xFFFFC107)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text       = String.format("%.1f / 10", movie.voteAverage),
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text       = "Synopsis",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text  = movie.overview.ifBlank { "Aucun synopsis disponible." },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}