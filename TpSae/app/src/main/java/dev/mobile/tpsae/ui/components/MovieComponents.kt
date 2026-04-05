package dev.mobile.tpsae.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.mobile.tpsae.data.TmdbApi
import dev.mobile.tpsae.model.Movie
import dev.mobile.tpsae.viewmodel.MovieCategory
import android.content.Intent
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import dev.mobile.tpsae.MainActivity
import dev.mobile.tpsae.SearchActivity

/**
 * Carte d'un film affichée dans la LazyColumn.
 * Composant stateless : reçoit les données et un callback de clic.
 */
@Composable
fun MovieCard(
    movie: Movie,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.height(150.dp)) {

            // Chargement asynchrone de l'affiche via Coil
            AsyncImage(
                model = "${TmdbApi.IMAGE_BASE_URL}${movie.posterPath}",
                contentDescription = "Affiche de ${movie.title}",
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text       = movie.title,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text     = movie.overview,
                        style    = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Note + année en bas de la carte
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector    = Icons.Default.Star,
                        contentDescription = "Note",
                        tint           = Color(0xFFFFC107),
                        modifier       = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text       = String.format("%.1f", movie.voteAverage),
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    movie.releaseDate?.let {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text  = it.take(4), // affiche uniquement l'année
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Barre de recherche réutilisable.
 * Stateless : l'état du texte vit dans le ViewModel.
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value         = query,
        onValueChange = onQueryChange,
        modifier      = modifier.fillMaxWidth(),
        placeholder   = { Text("Rechercher un film…") },
        leadingIcon   = { Icon(Icons.Default.Search, contentDescription = "Recherche") },
        singleLine    = true,
        shape         = RoundedCornerShape(12.dp)
    )
}

/**
 * Sélecteur de catégorie sous forme de FilterChips Material 3.
 */
@Composable
fun CategorySelector(
    selectedCategory: MovieCategory,
    onCategorySelected: (MovieCategory) -> Unit
) {
    val categories = listOf(
        MovieCategory.POPULAR   to "Populaires",
        MovieCategory.TOP_RATED to "Mieux notés"
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.forEach { (cat, label) ->
            FilterChip(
                selected = selectedCategory == cat,
                onClick  = { onCategorySelected(cat) },
                label    = { Text(label) }
            )
        }
    }
}

@Composable
fun AppBottomNavBar(currentScreen: String) {
    val context = LocalContext.current

    NavigationBar {
        // Bouton Accueil
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Accueil") },
            label = { Text("Accueil") },
            selected = currentScreen == "Home",
            onClick = {
                if (currentScreen != "Home") {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    }
                    context.startActivity(intent)
                }
            }
        )
        // Bouton Recherche
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = "Recherche") },
            label = { Text("Recherche") },
            selected = currentScreen == "Search",
            onClick = {
                if (currentScreen != "Search") {
                    val intent = Intent(context, SearchActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    }
                    context.startActivity(intent)
                }
            }
        )
    }
}