package com.example.audioplayer.ui.screens.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.atoms.Artwork
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
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
    val songs = remember(lib, playlist) { playlist.songIds.mapNotNull { lib.byId[it] } }
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
            extra = { _, i ->
                listOf(SheetAction("Hapus dari Playlist", Icons.Trash, destructive = true) {
                    // indeks di songs bisa berbeda dengan songIds kalau ada lagu yang hilang
                    val target = songs[i].id
                    var seen = -1
                    val realIndex = playlist.songIds.indexOfFirst { sid -> if (lib.byId.containsKey(sid)) seen++; sid == target && seen == i }
                    if (realIndex >= 0) a.vm.store.removeFromPlaylist(id, realIndex)
                })
            },
        )
        item { Spacer(Modifier.height(8.dp)) }
    }
}
