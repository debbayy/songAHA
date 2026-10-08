package com.example.audioplayer.ui.screens.library

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.molecules.EmptyState
import com.example.audioplayer.ui.components.organisms.AlphabetIndex
import com.example.audioplayer.ui.components.organisms.albumGrid
import com.example.audioplayer.ui.components.organisms.alphabetSections
import com.example.audioplayer.ui.components.templates.Page

@Composable
fun AlbumsScreen() {
    val a = LocalActions.current
    val lib by a.vm.library.collectAsState()
    val listState = rememberLazyListState()
    // item 0 = judul, lalu baris grid berisi 2 album
    val sections = remember(lib.albums) { alphabetSections(lib.albums, firstIndex = 1, perItem = 2) { it.title } }
    Page("Album", state = listState, overlay = { AlphabetIndex(listState, sections) }) {
        if (lib.albums.isEmpty()) item { EmptyState("Tidak Ada Album", "Album dibaca dari tag lagu di perangkat kamu.") }
        albumGrid(lib.albums)
    }
}
