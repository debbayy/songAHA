package com.example.audioplayer.ui.screens.library

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.molecules.EmptyState
import com.example.audioplayer.ui.components.molecules.PlayShuffleButtons
import com.example.audioplayer.ui.components.organisms.songList
import com.example.audioplayer.ui.components.templates.Page

@Composable
fun SongsScreen() {
    val a = LocalActions.current
    val lib by a.vm.library.collectAsState()
    val player by a.vm.player.collectAsState()
    Page("Lagu") {
        if (lib.songs.isEmpty()) item { EmptyState("Tidak Ada Lagu", "Lagu di perangkat kamu akan muncul di sini.") }
        else item { PlayShuffleButtons({ a.vm.playSongs(lib.songs) }, { a.vm.shuffle(lib.songs) }, Modifier.padding(vertical = 8.dp)) }
        songList(lib.songs, player, a)
    }
}
