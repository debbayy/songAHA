package com.example.audioplayer.ui.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.Route
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.molecules.EmptyState
import com.example.audioplayer.ui.components.molecules.GlassIconButton
import com.example.audioplayer.ui.components.molecules.PlayShuffleButtons
import com.example.audioplayer.ui.components.molecules.SectionHeader
import com.example.audioplayer.ui.components.organisms.AlbumCarousel
import com.example.audioplayer.ui.components.organisms.PermissionCard
import com.example.audioplayer.ui.components.organisms.SongCarousel
import com.example.audioplayer.ui.components.templates.Page

@Composable
fun HomeScreen() {
    val a = LocalActions.current
    val vm = a.vm
    val lib by vm.library.collectAsState()
    val loading by vm.loading.collectAsState()
    val granted by vm.hasPermission.collectAsState()
    val recentIds by vm.store.recent.collectAsState()
    val favIds by vm.store.favorites.collectAsState()
    val recent = remember(lib, recentIds) { recentIds.mapNotNull { lib.byId[it] } }
    val favorites = remember(lib, favIds) { lib.songs.filter { it.id in favIds } }

    Page("Beranda", actions = { GlassIconButton(Icons.Gear, { a.nav.push(Route.Settings) }) }) {
        if (!granted && lib.songs.isEmpty()) item { PermissionCard() }
        if (lib.songs.isNotEmpty()) item {
            PlayShuffleButtons({ vm.playSongs(lib.songs) }, { vm.shuffle(lib.songs) }, Modifier.padding(top = 8.dp))
        }
        if (recent.isNotEmpty()) {
            item { SectionHeader("Terakhir Diputar") { a.nav.push(Route.Recent) } }
            item { SongCarousel(recent.take(15)) }
        }
        if (lib.recentAlbums.isNotEmpty()) {
            item { SectionHeader("Baru Ditambahkan") { a.nav.push(Route.Albums) } }
            item { AlbumCarousel(lib.recentAlbums) }
        }
        if (favorites.isNotEmpty()) {
            item { SectionHeader("Favorit") { a.nav.push(Route.Favorites) } }
            item { SongCarousel(favorites.take(15)) }
        }
        if (lib.songs.isNotEmpty() && recent.isEmpty() && lib.recentAlbums.isEmpty()) {
            item { SectionHeader("Lagu") { a.nav.push(Route.Songs) } }
            item { SongCarousel(lib.songs.take(15)) }
        }
        if (granted && !loading && lib.songs.isEmpty()) item {
            EmptyState("Belum Ada Musik", "Salin file musik ke HP kamu, atau pilih folder di Pustaka › Folder.")
        }
    }
}
