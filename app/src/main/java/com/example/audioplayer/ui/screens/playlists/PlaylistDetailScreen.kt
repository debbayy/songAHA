package com.example.audioplayer.ui.screens.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.atoms.Artwork
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.rememberReorderState
import com.example.audioplayer.ui.components.foundation.reorderable
import com.example.audioplayer.ui.components.foundation.withStableKeys
import com.example.audioplayer.ui.components.molecules.EmptyState
import com.example.audioplayer.ui.components.molecules.GlassIconButton
import com.example.audioplayer.ui.components.molecules.PlayShuffleButtons
import com.example.audioplayer.ui.components.organisms.AlertSpec
import com.example.audioplayer.ui.components.organisms.SheetAction
import com.example.audioplayer.ui.components.organisms.SheetSpec
import com.example.audioplayer.ui.components.organisms.songList
import com.example.audioplayer.ui.components.templates.Page
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos
import com.example.audioplayer.ui.theme.rememberArtTint
import com.example.audioplayer.ui.util.songCountLabel

/** Lagu di playlist beserta posisinya di songIds (bisa beda dari urutan tampil kalau ada lagu yang sudah terhapus). */
private data class PlaylistEntry(val position: Int, val song: Song)

@Composable
fun PlaylistDetailScreen(id: String) {
    val a = LocalActions.current
    val c = LocalIos.current
    val lib by a.vm.library.collectAsState()
    val player by a.vm.player.collectAsState()
    val playlists by a.vm.store.playlists.collectAsState()
    val playlist = playlists.firstOrNull { it.id == id }
    if (playlist == null) {
        Page("Playlist") { item { EmptyState("Playlist Dihapus", "Playlist ini sudah tidak ada.") } }
        return
    }
    val entries = remember(lib, playlist) {
        playlist.songIds
            .mapIndexedNotNull { pos, songId -> lib.byId[songId]?.let { PlaylistEntry(pos, it) } }
            .withStableKeys { it.song.id }
    }
    val listState = rememberLazyListState()
    val reorder = rememberReorderState(entries, key = { it.key }, listState) { from, to ->
        a.vm.store.movePlaylistSong(id, entries[from].value.position, entries[to].value.position)
    }
    val songs = reorder.items.map { it.value.song }
    val tint = rememberArtTint(songs.firstOrNull(), c.fill)

    fun menu() = a.showSheet(
        SheetSpec(
            title = playlist.name,
            subtitle = "${songs.size} lagu",
            actions = listOf(
                SheetAction("Tambah ke Antrean", Icons.Queue) { a.vm.addToQueue(songs); a.toast("Ditambahkan ke Antrean") },
                SheetAction("Ubah Nama", Icons.Pencil) {
                    a.showAlert(AlertSpec("Ubah Nama Playlist", initial = playlist.name, confirm = "Simpan") {
                        a.vm.store.renamePlaylist(id, it)
                    })
                },
                SheetAction("Hapus Playlist", Icons.Trash, destructive = true) {
                    a.showAlert(
                        AlertSpec(
                            "Hapus \"${playlist.name}\"?", "Lagu di perangkat tidak ikut terhapus.",
                            input = false, confirm = "Hapus", destructive = true,
                        ) {
                            a.nav.pop()
                            a.vm.store.deletePlaylist(id)
                        }
                    )
                },
            ),
        )
    )

    Page(
        playlist.name,
        state = listState,
        listModifier = Modifier.reorderable(reorder),
        actions = { GlassIconButton(Icons.More, { menu() }) },
        header = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(tint.copy(alpha = 0.5f), c.background)))
                    .statusBarsPadding()
                    .padding(top = 64.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Artwork(songs.firstOrNull(), 230.dp, Modifier.size(230.dp).shadow(18.dp, RoundedCornerShape(12.dp)), corner = 12.dp)
                Txt(playlist.name, IosType.title2, Modifier.padding(top = 18.dp, start = 24.dp, end = 24.dp), align = TextAlign.Center, maxLines = 2)
                Txt("Playlist · ${songCountLabel(songs)}", IosType.footnote, Modifier.padding(top = 4.dp, bottom = 16.dp), color = c.secondaryLabel)
                if (songs.isNotEmpty()) PlayShuffleButtons({ a.vm.playSongs(songs) }, { a.vm.shuffle(songs) })
            }
        },
    ) {
        if (songs.isEmpty()) item {
            EmptyState("Playlist Masih Kosong", "Tahan sebuah lagu lalu pilih \"Tambah ke Playlist…\".")
        }
        songList(
            songs, player, a,
            key = { i, _ -> reorder.items[i].key },
            reorder = reorder,
            extra = { _, i ->
                listOf(SheetAction("Hapus dari Playlist", Icons.Trash, destructive = true) {
                    a.vm.store.removeFromPlaylist(id, reorder.items[i].value.position)
                })
            },
        )
        if (songs.size > 1) item {
            Txt(
                "Tahan lalu geser lagu untuk mengubah urutan.", IosType.footnote,
                Modifier.padding(horizontal = 16.dp, vertical = 12.dp), color = c.secondaryLabel,
            )
        }
    }
}
