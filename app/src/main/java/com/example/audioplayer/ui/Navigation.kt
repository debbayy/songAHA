package com.example.audioplayer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.components.AlertSpec
import com.example.audioplayer.ui.components.Icons
import com.example.audioplayer.ui.components.SheetAction
import com.example.audioplayer.ui.components.SheetSpec

enum class Tab(val label: String) {
    Home("Beranda"), Library("Pustaka"), Playlists("Playlist"), Search("Cari");

    val icon
        get() = when (this) {
            Home -> Icons.Home
            Library -> Icons.Library
            Playlists -> Icons.Queue
            Search -> Icons.Search
        }
}

sealed interface Route {
    data object Home : Route
    data object Library : Route
    data object Playlists : Route
    data object Search : Route
    data object Songs : Route
    data object Albums : Route
    data object Artists : Route
    data object Folders : Route
    data object Favorites : Route
    data object Recent : Route
    data object Settings : Route
    data class AlbumDetail(val key: String) : Route
    data class ArtistDetail(val name: String) : Route
    data class FolderDetail(val path: String) : Route
    data class PlaylistDetail(val id: String) : Route
}

/**
 * Navigasi sederhana tanpa library: tiap tab punya back stack sendiri (seperti UITabBarController).
 * Disimpan di ViewModel supaya tidak hilang saat layar diputar.
 */
class NavViewModel : ViewModel() {
    var tab by mutableStateOf(Tab.Home)
        private set
    var previousTab by mutableStateOf(Tab.Home)
        private set

    /** true kalau perpindahan terakhir adalah "kembali" (untuk arah animasi). */
    var lastWasPop by mutableStateOf(false)
        private set

    var nowPlayingOpen by mutableStateOf(false)
    var queueOpen by mutableStateOf(false)
    var query by mutableStateOf("")

    private val stacks: Map<Tab, SnapshotStateList<Route>> = mapOf(
        Tab.Home to mutableStateListOf(Route.Home),
        Tab.Library to mutableStateListOf(Route.Library),
        Tab.Playlists to mutableStateListOf(Route.Playlists),
        Tab.Search to mutableStateListOf(Route.Search),
    )

    fun stack(t: Tab = tab): List<Route> = stacks.getValue(t)
    val current: Route get() = stacks.getValue(tab).last()

    fun select(t: Tab) {
        if (t == tab) {
            // ketuk tab yang aktif = kembali ke root, seperti iOS
            val s = stacks.getValue(t)
            if (s.size > 1) {
                lastWasPop = true
                while (s.size > 1) s.removeAt(s.lastIndex)
            }
            return
        }
        if (t == Tab.Search) previousTab = tab
        lastWasPop = false
        tab = t
    }

    fun push(route: Route) {
        nowPlayingOpen = false
        val s = stacks.getValue(tab)
        if (s.last() == route) return
        lastWasPop = false
        s.add(route)
    }

    fun pop(): Boolean {
        val s = stacks.getValue(tab)
        if (s.size <= 1) return false
        lastWasPop = true
        s.removeAt(s.lastIndex)
        return true
    }

    /** Tombol back Android: tutup overlay dulu, lalu pop, lalu kembali ke Beranda. */
    fun back(): Boolean = when {
        nowPlayingOpen && queueOpen -> { queueOpen = false; true }
        nowPlayingOpen -> { nowPlayingOpen = false; true }
        pop() -> true
        tab == Tab.Search -> { select(previousTab); true }
        tab != Tab.Home -> { select(Tab.Home); true }
        else -> false
    }
}

/** Akses ke ViewModel & overlay global dari layar mana pun. */
class AppActions(
    val vm: PlayerViewModel,
    val nav: NavViewModel,
    val showSheet: (SheetSpec) -> Unit,
    val showAlert: (AlertSpec) -> Unit,
    val toast: (String) -> Unit,
    val requestAudioPermission: () -> Unit,
    val pickFolder: () -> Unit,
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
