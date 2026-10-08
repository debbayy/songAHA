package com.example.audioplayer.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.NavViewModel
import com.example.audioplayer.ui.Route
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.molecules.SectionHeader

private data class Category(val label: String, val icon: ImageVector, val route: Route, val colors: List<Color>)

private val categories = listOf(
    Category("Lagu", Icons.Note, Route.Songs, listOf(Color(0xFFFF2D55), Color(0xFFFF6482))),
    Category("Album", Icons.Album, Route.Albums, listOf(Color(0xFF5856D6), Color(0xFF7D7AFF))),
    Category("Artis", Icons.Person, Route.Artists, listOf(Color(0xFFFF9500), Color(0xFFFFB340))),
    Category("Folder", Icons.Folder, Route.Folders, listOf(Color(0xFF34C759), Color(0xFF30D158))),
    Category("Favorit", Icons.HeartFill, Route.Favorites, listOf(Color(0xFFAF52DE), Color(0xFFDA8FFF))),
    Category("Terakhir Diputar", Icons.History, Route.Recent, listOf(Color(0xFF007AFF), Color(0xFF409CFF))),
)

/** Grid 2 kolom kategori pustaka, tampil saat kolom cari masih kosong. */
internal fun LazyListScope.browseGrid(nav: NavViewModel) {
    item { SectionHeader("Jelajahi") }
    items(categories.chunked(2), key = { it.first().label }) { row ->
        Row(Modifier.padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { cat ->
                CategoryCard(cat.label, cat.icon, cat.colors, onClick = { nav.push(cat.route) }, modifier = Modifier.weight(1f))
            }
        }
    }
}
