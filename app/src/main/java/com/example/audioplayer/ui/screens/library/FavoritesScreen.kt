package com.example.audioplayer.ui.screens.library

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.molecules.EmptyState
import com.example.audioplayer.ui.components.molecules.PlayShuffleButtons
import com.example.audioplayer.ui.components.organisms.songList
import com.example.audioplayer.ui.components.templates.Page

@Composable
fun FavoritesScreen() {
    val a = LocalActions.current
    val lib by a.vm.library.collectAsState()
    val player by a.vm.player.collectAsState()
    val favIds by a.vm.store.favorites.collectAsState()
    val songs = remember(lib, favIds) { lib.songs.filter { it.id in favIds } }
    Page("Favorit") {
        if (songs.isEmpty()) item { EmptyState("Belum Ada Favorit", "Tahan sebuah lagu lalu pilih \"Favoritkan\", atau ketuk ♥ di layar Sedang Diputar.") }
        else item { PlayShuffleButtons({ a.vm.playSongs(songs) }, { a.vm.shuffle(songs) }, Modifier.padding(vertical = 8.dp)) }
        songList(songs, player, a)
    }
}
