package com.example.audioplayer.ui.screens.search

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.Route
import com.example.audioplayer.ui.components.atoms.Artwork
import com.example.audioplayer.ui.components.molecules.EmptyState
import com.example.audioplayer.ui.components.molecules.NavRow
import com.example.audioplayer.ui.components.molecules.SectionHeader
import com.example.audioplayer.ui.components.organisms.AlbumCarousel
import com.example.audioplayer.ui.components.organisms.songList
import com.example.audioplayer.ui.components.templates.Page
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.withContext

/** Jeda setelah berhenti mengetik sebelum keyboard ditutup, kalau hasilnya sudah ada. */
private const val HIDE_KEYBOARD_AFTER_MS = 1_200L

@Composable
fun SearchScreen() {
    val a = LocalActions.current
    val lib by a.vm.library.collectAsState()
    val player by a.vm.player.collectAsState()
    val query = a.nav.query
    val results by produceState(SearchResults(), query, lib) {
        delay(120) // debounce ketikan
        value = withContext(Dispatchers.Default) { search(lib, query) }
    }

    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    fun hideKeyboard() {
        keyboard?.hide()
        focus.clearFocus()
    }
    // Hasil sudah ketemu dan user berhenti mengetik: tutup keyboard supaya hasil terlihat penuh
    LaunchedEffect(results) {
        if (!results.isEmpty) {
            delay(HIDE_KEYBOARD_AFTER_MS)
            hideKeyboard()
        }
    }
    // Menggulir hasil juga menutup keyboard, seperti iOS
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.filter { it }.collect { hideKeyboard() }
    }

    Page("Cari", Modifier.imePadding(), state = listState) {
        if (query.isBlank()) {
            browseGrid(a.nav)
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
        if (results.isEmpty) {
            item { EmptyState("Tidak Ada Hasil", "Tidak ada yang cocok dengan \"$query\".") }
        }
    }
}
