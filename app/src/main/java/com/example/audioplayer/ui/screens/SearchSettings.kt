package com.example.audioplayer.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.example.audioplayer.ui.components.Artwork
import com.example.audioplayer.ui.components.EmptyState
import com.example.audioplayer.ui.components.Hairline
import com.example.audioplayer.ui.components.Ico
import com.example.audioplayer.ui.components.Icons
import com.example.audioplayer.ui.components.Page
import com.example.audioplayer.ui.components.SectionHeader
import com.example.audioplayer.ui.components.Segmented
import com.example.audioplayer.ui.components.Txt
import com.example.audioplayer.ui.components.bounce
import com.example.audioplayer.ui.components.pressable
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

// ---- Cari --------------------------------------------------------------------------------

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

// ---- Pengaturan --------------------------------------------------------------------------

@Composable
fun SettingsScreen() {
    val a = LocalActions.current
    val c = LocalIos.current
    val settings by a.vm.store.settings.collectAsState()
    val lib by a.vm.library.collectAsState()
    val folders by a.vm.store.safFolders.collectAsState()

    Page("Pengaturan", background = c.groupedBackground) {
        item {
            Group("TAMPILAN", footer = "Kaca \"Berwarna\" lebih pekat dan kontras, seperti pilihan tampilan Liquid Glass di iOS 27.") {
                SettingRow("Tema") {
                    Segmented(listOf("Otomatis", "Terang", "Gelap"), settings.theme, { t -> a.vm.store.updateSettings { it.copy(theme = t) } }, Modifier.width(210.dp))
                }
                Hairline(Modifier.padding(start = 16.dp))
                SettingRow("Kaca") {
                    Segmented(
                        listOf("Bening", "Berwarna"), if (settings.glassTinted) 1 else 0,
                        { i -> a.vm.store.updateSettings { it.copy(glassTinted = i == 1) } }, Modifier.width(170.dp),
                    )
                }
            }
        }
        item {
            Group("PUSTAKA", footer = "Folder tambahan berguna untuk musik yang tidak terbaca otomatis oleh sistem.") {
                ActionRow("Pindai Ulang Musik", Icons.Refresh) { a.vm.reload(); a.toast("Memindai ulang…") }
                Hairline(Modifier.padding(start = 16.dp))
                ActionRow("Tambah Folder…", Icons.Folder, a.pickFolder)
                folders.forEach { uri ->
                    Hairline(Modifier.padding(start = 16.dp))
                    Row(Modifier.fillMaxWidth().height(48.dp).padding(start = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Txt(folderLabel(uri), IosType.body, Modifier.weight(1f))
                        Box(Modifier.size(44.dp).pressable(highlight = false) { a.vm.removeFolder(uri) }, Alignment.Center) {
                            Ico(Icons.CloseCircle, c.tertiaryLabel, size = 22.dp)
                        }
                    }
                }
            }
        }
        item {
            Group("TENTANG") {
                InfoRow("Lagu", "${lib.songs.size}")
                Hairline(Modifier.padding(start = 16.dp))
                InfoRow("Album", "${lib.albums.size}")
                Hairline(Modifier.padding(start = 16.dp))
                InfoRow("Artis", "${lib.artists.size}")
                Hairline(Modifier.padding(start = 16.dp))
                InfoRow("Versi", "1.0")
            }
        }
    }
}

private fun folderLabel(uri: String): String = runCatching {
    android.provider.DocumentsContract.getTreeDocumentId(Uri.parse(uri)).substringAfterLast(':').ifBlank { "Penyimpanan" }
}.getOrDefault(uri)

@Composable
private fun Group(title: String, footer: String? = null, content: @Composable () -> Unit) {
    val c = LocalIos.current
    Column(Modifier.padding(horizontal = 16.dp)) {
        Txt(title, IosType.footnote, Modifier.padding(start = 16.dp, top = 22.dp, bottom = 6.dp), color = c.secondaryLabel)
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(c.cell)) { content() }
        if (footer != null) Txt(footer, IosType.footnote, Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp), color = c.secondaryLabel, maxLines = 4)
    }
}

@Composable
private fun SettingRow(label: String, trailing: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Txt(label, IosType.body, Modifier.weight(1f))
        trailing()
    }
}

@Composable
private fun ActionRow(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val c = LocalIos.current
    Row(Modifier.fillMaxWidth().height(48.dp).pressable(onClick = onClick).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Txt(label, IosType.body, Modifier.weight(1f), color = c.accent)
        Ico(icon, c.accent, size = 22.dp)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val c = LocalIos.current
    Row(Modifier.fillMaxWidth().height(46.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Txt(label, IosType.body, Modifier.weight(1f))
        Txt(value, IosType.body, color = c.secondaryLabel)
    }
}
