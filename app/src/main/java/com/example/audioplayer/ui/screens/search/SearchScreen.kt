package com.example.audioplayer.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.Album
import com.example.audioplayer.data.Artist
import com.example.audioplayer.data.Library
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.Route
import com.example.audioplayer.ui.components.atoms.Artwork
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.bounce
import com.example.audioplayer.ui.components.molecules.EmptyState
import com.example.audioplayer.ui.components.molecules.NavRow
import com.example.audioplayer.ui.components.molecules.SectionHeader
import com.example.audioplayer.ui.components.organisms.AlbumCarousel
import com.example.audioplayer.ui.components.organisms.songList
import com.example.audioplayer.ui.components.templates.Page
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private data class SearchResults(val songs: List<Song>, val albums: List<Album>, val artists: List<Artist>)

private fun search(lib: Library, query: String): SearchResults {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return SearchResults(emptyList(), emptyList(), emptyList())
    return SearchResults(
        songs = lib.songs.filter { it.title.lowercase().contains(q) || it.artist.lowercase().contains(q) || it.album.lowercase().contains(q) }.take(60),
        albums = lib.albums.filter { it.title.lowercase().contains(q) || it.artist.lowercase().contains(q) }.take(20),
        artists = lib.artists.filter { it.name.lowercase().contains(q) }.take(8),
    )
}

private data class Category(val label: String, val route: Route, val colors: List<Color>)

private val categories = listOf(
    Category("Lagu", Route.Songs, listOf(Color(0xFFFF2D55), Color(0xFFFF6482))),
    Category("Album", Route.Albums, listOf(Color(0xFF5856D6), Color(0xFF7D7AFF))),
    Category("Artis", Route.Artists, listOf(Color(0xFFFF9500), Color(0xFFFFB340))),
    Category("Folder", Route.Folders, listOf(Color(0xFF34C759), Color(0xFF30D158))),
    Category("Favorit", Route.Favorites, listOf(Color(0xFFAF52DE), Color(0xFFDA8FFF))),
    Category("Terakhir Diputar", Route.Recent, listOf(Color(0xFF007AFF), Color(0xFF409CFF))),
)

@Composable
fun SearchScreen() {
    val a = LocalActions.current
    val c = LocalIos.current
    val lib by a.vm.library.collectAsState()
    val player by a.vm.player.collectAsState()
    val query = a.nav.query
    val results by produceState(SearchResults(emptyList(), emptyList(), emptyList()), query, lib) {
        delay(120) // debounce ketikan
        value = withContext(Dispatchers.Default) { search(lib, query) }
    }

    Page("Cari", Modifier.imePadding()) {
        if (query.isBlank()) {
            item { SectionHeader("Jelajahi") }
            items(categories.chunked(2), key = { it.first().label }) { row ->
                Row(Modifier.padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { cat ->
                        Box(
                            Modifier
                                .weight(1f)
                                .height(96.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(cat.colors))
                                .bounce { a.nav.push(cat.route) }
                                .padding(12.dp),
                        ) { Txt(cat.label, IosType.headline, Modifier.align(Alignment.BottomStart), color = Color.White, maxLines = 2) }
                    }
                }
            }
            return@Page
        }
        if (results.artists.isNotEmpty()) {
            item { SectionHeader("Artis") }
            items(results.artists, key = { "ar:" + it.name }) { artist ->
                NavRow(
                    artist.name, { a.nav.push(Route.ArtistDetail(artist.name)) },
                    leading = { Artwork(artist.songs.first(), 48.dp, Modifier.size(48.dp), corner = 24.dp) },
                    subtitle = "Artis",
                )
            }
        }
        if (results.albums.isNotEmpty()) {
            item { SectionHeader("Album") }
            item { AlbumCarousel(results.albums) }
        }
        if (results.songs.isNotEmpty()) {
            item { SectionHeader("Lagu") }
            songList(results.songs, player, a)
        }
        if (results.songs.isEmpty() && results.albums.isEmpty() && results.artists.isEmpty()) {
            item { EmptyState("Tidak Ada Hasil", "Tidak ada yang cocok dengan \"$query\".") }
        }
    }
}
