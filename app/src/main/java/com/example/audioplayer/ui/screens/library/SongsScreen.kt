package com.example.audioplayer.ui.screens.library

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.molecules.EmptyState
import com.example.audioplayer.ui.components.molecules.PlayShuffleButtons
import com.example.audioplayer.ui.components.organisms.AlphabetIndex
import com.example.audioplayer.ui.components.organisms.alphabetSections
import com.example.audioplayer.ui.components.organisms.songList
import com.example.audioplayer.ui.components.templates.Page

@Composable
fun SongsScreen() {
    val a = LocalActions.current
    val lib by a.vm.library.collectAsState()
    val player by a.vm.player.collectAsState()
    val listState = rememberLazyListState()
    // item 0 = judul, item 1 = tombol Putar/Acak, lagu mulai dari item 2
    val sections = remember(lib.songs) { alphabetSections(lib.songs, firstIndex = 2) { it.title } }
    Page("Lagu", state = listState, overlay = { AlphabetIndex(listState, sections) }) {
        if (lib.songs.isEmpty()) item { EmptyState("Tidak Ada Lagu", "Lagu di perangkat kamu akan muncul di sini.") }
        else item { PlayShuffleButtons({ a.vm.playSongs(lib.songs) }, { a.vm.shuffle(lib.songs) }, Modifier.padding(vertical = 8.dp)) }
        songList(lib.songs, player, a)
    }
}
