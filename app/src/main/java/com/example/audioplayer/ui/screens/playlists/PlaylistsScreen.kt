package com.example.audioplayer.ui.screens.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.Route
import com.example.audioplayer.ui.components.atoms.Artwork
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.molecules.GlassIconButton
import com.example.audioplayer.ui.components.molecules.NavRow
import com.example.audioplayer.ui.components.templates.Page
import com.example.audioplayer.ui.newPlaylist
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun PlaylistsScreen() {
    val a = LocalActions.current
    val c = LocalIos.current
    val lib by a.vm.library.collectAsState()
    val playlists by a.vm.store.playlists.collectAsState()
    Page("Playlist", actions = { GlassIconButton(Icons.Add, { a.newPlaylist { a.nav.push(Route.PlaylistDetail(it)) } }) }) {
        item {
            NavRow(
                "Playlist Baru…", { a.newPlaylist { a.nav.push(Route.PlaylistDetail(it)) } },
                leading = {
                    Box(Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(c.fill), Alignment.Center) {
                        Ico(Icons.Add, c.accent, size = 30.dp)
                    }
                },
                subtitle = "Kumpulkan lagu favoritmu",
            )
        }
        items(playlists, key = { it.id }) { p ->
            val first = p.songIds.firstNotNullOfOrNull { lib.byId[it] }
            NavRow(
                p.name, { a.nav.push(Route.PlaylistDetail(p.id)) },
                leading = { Artwork(first, 56.dp, Modifier.size(56.dp), corner = 8.dp) },
                subtitle = "${p.songIds.size} lagu",
            )
        }
    }
}
