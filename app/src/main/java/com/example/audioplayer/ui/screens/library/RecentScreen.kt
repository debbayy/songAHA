package com.example.audioplayer.ui.screens.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.molecules.EmptyState
import com.example.audioplayer.ui.components.organisms.songList
import com.example.audioplayer.ui.components.templates.Page

@Composable
fun RecentScreen() {
    val a = LocalActions.current
    val lib by a.vm.library.collectAsState()
    val player by a.vm.player.collectAsState()
    val ids by a.vm.store.recent.collectAsState()
    val songs = remember(lib, ids) { ids.mapNotNull { lib.byId[it] } }
    Page("Terakhir Diputar") {
        if (songs.isEmpty()) item { EmptyState("Belum Ada Riwayat", "Lagu yang kamu putar akan muncul di sini.") }
        songList(songs, player, a)
    }
}
