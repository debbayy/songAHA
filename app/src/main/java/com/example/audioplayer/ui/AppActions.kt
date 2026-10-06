package com.example.audioplayer.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.organisms.AlertSpec
import com.example.audioplayer.ui.components.organisms.SheetAction
import com.example.audioplayer.ui.components.organisms.SheetSpec

/** Akses ke ViewModel & overlay global dari layar mana pun. */
class AppActions(
    val vm: PlayerViewModel,
    val nav: NavViewModel,
    val showSheet: (SheetSpec) -> Unit,
    val showAlert: (AlertSpec) -> Unit,
    val toast: (String) -> Unit,
    val requestAudioPermission: () -> Unit,
    val pickFolder: () -> Unit,
    val pickWallpaper: () -> Unit,
    val pickLyrics: (Song) -> Unit,
)

val LocalActions = staticCompositionLocalOf<AppActions> { error("AppActions belum disediakan") }

fun AppActions.songMenu(song: Song, extra: List<SheetAction> = emptyList()) {
    val fav = song.id in vm.store.favorites.value
    val actions = buildList {
        add(SheetAction("Putar Berikutnya", Icons.PlayNext) { vm.playNext(song); toast("Diputar Berikutnya") })
        add(SheetAction("Tambah ke Antrean", Icons.Queue) { vm.addToQueue(listOf(song)); toast("Ditambahkan ke Antrean") })
        add(
            SheetAction(if (fav) "Hapus dari Favorit" else "Favoritkan", if (fav) Icons.HeartFill else Icons.Heart) {
                vm.store.toggleFavorite(song.id)
            }
        )
        add(SheetAction("Tambah ke Playlist…", Icons.PlaylistAdd) { addToPlaylist(listOf(song)) })
        if (song.album.isNotBlank()) add(SheetAction("Buka Album", Icons.Album) { nav.push(Route.AlbumDetail(song.albumKey)) })
        add(SheetAction("Buka Artis", Icons.Person) { nav.push(Route.ArtistDetail(song.displayArtist)) })
        addAll(extra)
    }
    showSheet(SheetSpec(actions, song = song))
}

fun AppActions.addToPlaylist(songs: List<Song>) {
    val ids = songs.map { it.id }
    val newPlaylist = SheetAction("Playlist Baru…", Icons.Add) {
        showAlert(
            AlertSpec(
                title = "Playlist Baru",
                message = "Masukkan nama untuk playlist ini.",
                placeholder = "Nama playlist",
                confirm = "Buat",
            ) { name ->
                vm.store.createPlaylist(name, ids)
                toast("Ditambahkan ke \"$name\"")
            }
        )
    }
    val existing = vm.store.playlists.value.map { p ->
        SheetAction(p.name, Icons.Queue) {
            vm.store.addToPlaylist(p.id, ids)
            toast("Ditambahkan ke \"${p.name}\"")
        }
    }
    showSheet(SheetSpec(listOf(newPlaylist) + existing, title = "Tambah ke Playlist", subtitle = "${songs.size} lagu"))
}

fun AppActions.newPlaylist(onCreated: (String) -> Unit = {}) {
    showAlert(
        AlertSpec(
            title = "Playlist Baru",
            message = "Masukkan nama untuk playlist ini.",
            placeholder = "Nama playlist",
            confirm = "Buat",
        ) { name -> onCreated(vm.store.createPlaylist(name).id) }
    )
}

fun AppActions.sleepTimerMenu() {
    val options = listOf(5, 15, 30, 45, 60).map { m ->
        SheetAction("$m menit", Icons.Moon) { vm.setSleepTimer(m); toast("Berhenti dalam $m menit") }
    } + SheetAction("Akhir lagu ini", Icons.Note) { vm.setSleepTimer(-1); toast("Berhenti di akhir lagu") }
    val off = if (vm.sleep.value != null) listOf(SheetAction("Matikan Timer", Icons.Close, destructive = true) { vm.setSleepTimer(0) })
    else emptyList()
    showSheet(SheetSpec(options + off, title = "Timer Tidur", subtitle = "Musik akan dijeda otomatis"))
}

fun AppActions.lyricsMenu(song: Song) {
    val actions = buildList {
        add(SheetAction("Impor File Lirik (.lrc)…", Icons.Lyrics) { pickLyrics(song) })
        if (vm.lyrics.hasImported(song)) {
            add(SheetAction("Hapus Lirik Impor", Icons.Trash, destructive = true) { vm.lyrics.removeImported(song) })
        }
    }
    showSheet(SheetSpec(actions, title = "Lirik", subtitle = song.title))
}
