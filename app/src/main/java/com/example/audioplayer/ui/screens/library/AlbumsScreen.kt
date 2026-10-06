package com.example.audioplayer.ui.screens.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.molecules.EmptyState
import com.example.audioplayer.ui.components.organisms.albumGrid
import com.example.audioplayer.ui.components.templates.Page

@Composable
fun AlbumsScreen() {
    val a = LocalActions.current
    val lib by a.vm.library.collectAsState()
    Page("Album") {
        if (lib.albums.isEmpty()) item { EmptyState("Tidak Ada Album", "Album dibaca dari tag lagu di perangkat kamu.") }
        albumGrid(lib.albums)
    }
}
