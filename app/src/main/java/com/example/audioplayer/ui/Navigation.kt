package com.example.audioplayer.ui

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.audioplayer.data.Library
import com.example.audioplayer.ui.components.atoms.Icons

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

/** Isi bagian tengah layar Now Playing. */
enum class NowPlayingPanel { Artwork, Lyrics, Queue }

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
    data object Equalizer : Route
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
    var panel by mutableStateOf(NowPlayingPanel.Artwork)

    /** Tombol lirik/antrean: buka panel itu, atau kembali ke cover kalau sudah terbuka. */
    fun togglePanel(target: NowPlayingPanel) {
        panel = if (panel == target) NowPlayingPanel.Artwork else target
    }
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
        nowPlayingOpen && panel != NowPlayingPanel.Artwork -> { panel = NowPlayingPanel.Artwork; true }
        nowPlayingOpen -> { nowPlayingOpen = false; true }
        pop() -> true
        tab == Tab.Search -> { select(previousTab); true }
        tab != Tab.Home -> { select(Tab.Home); true }
        else -> false
    }
}
