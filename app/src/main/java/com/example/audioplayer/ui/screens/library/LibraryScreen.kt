package com.example.audioplayer.ui.screens.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.Route
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.molecules.GlassIconButton
import com.example.audioplayer.ui.components.molecules.NavRow
import com.example.audioplayer.ui.components.molecules.SectionHeader
import com.example.audioplayer.ui.components.organisms.PermissionCard
import com.example.audioplayer.ui.components.organisms.albumGrid
import com.example.audioplayer.ui.components.templates.Page

@Composable
fun LibraryScreen() {
    val a = LocalActions.current
    val lib by a.vm.library.collectAsState()
    val granted by a.vm.hasPermission.collectAsState()
    Page("Pustaka", actions = { GlassIconButton(Icons.Gear, { a.nav.push(Route.Settings) }) }) {
        item { NavRow("Playlist", { a.nav.push(Route.Playlists) }, Icons.Queue, big = true) }
        item { NavRow("Artis", { a.nav.push(Route.Artists) }, Icons.Person, big = true) }
        item { NavRow("Album", { a.nav.push(Route.Albums) }, Icons.Album, big = true) }
        item { NavRow("Lagu", { a.nav.push(Route.Songs) }, Icons.Note, big = true) }
        item { NavRow("Folder", { a.nav.push(Route.Folders) }, Icons.Folder, big = true) }
        item { NavRow("Favorit", { a.nav.push(Route.Favorites) }, Icons.Heart, big = true) }
        item { NavRow("Terakhir Diputar", { a.nav.push(Route.Recent) }, Icons.History, big = true) }
        if (!granted && lib.songs.isEmpty()) item { PermissionCard() }
        if (lib.recentAlbums.isNotEmpty()) {
            item { SectionHeader("Baru Ditambahkan") }
            albumGrid(lib.recentAlbums)
        }
    }
}
