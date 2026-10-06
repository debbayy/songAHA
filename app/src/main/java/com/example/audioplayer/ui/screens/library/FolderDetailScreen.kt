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
fun FolderDetailScreen(path: String) {
    val a = LocalActions.current
    val lib by a.vm.library.collectAsState()
    val player by a.vm.player.collectAsState()
    val folder = lib.folderByPath[path]
    val songs = folder?.songs.orEmpty()
    Page(folder?.name ?: "Folder") {
        if (songs.isEmpty()) item { EmptyState("Folder Kosong", "Tidak ada lagu di folder ini.") }
        else item { PlayShuffleButtons({ a.vm.playSongs(songs) }, { a.vm.shuffle(songs) }, Modifier.padding(vertical = 8.dp)) }
        songList(songs, player, a)
    }
}
