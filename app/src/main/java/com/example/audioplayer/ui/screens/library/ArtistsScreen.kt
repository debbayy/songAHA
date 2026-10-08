package com.example.audioplayer.ui.screens.library

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.Route
import com.example.audioplayer.ui.components.atoms.Artwork
import com.example.audioplayer.ui.components.molecules.EmptyState
import com.example.audioplayer.ui.components.molecules.NavRow
import com.example.audioplayer.ui.components.organisms.AlphabetIndex
import com.example.audioplayer.ui.components.organisms.alphabetSections
import com.example.audioplayer.ui.components.templates.Page

@Composable
fun ArtistsScreen() {
    val a = LocalActions.current
    val lib by a.vm.library.collectAsState()
    val listState = rememberLazyListState()
    // item 0 = judul, artis mulai dari item 1
    val sections = remember(lib.artists) { alphabetSections(lib.artists, firstIndex = 1) { it.name } }
    Page("Artis", state = listState, overlay = { AlphabetIndex(listState, sections) }) {
        if (lib.artists.isEmpty()) item { EmptyState("Tidak Ada Artis", "Artis dibaca dari tag lagu di perangkat kamu.") }
        items(lib.artists, key = { it.name }) { artist ->
            NavRow(
                artist.name,
                { a.nav.push(Route.ArtistDetail(artist.name)) },
                leading = { Artwork(artist.songs.first(), 48.dp, Modifier.size(48.dp), corner = 24.dp) },
                subtitle = "${artist.songs.size} lagu",
            )
        }
    }
}
