package com.example.audioplayer.ui.screens.library

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.Route
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.molecules.GlassIconButton
import com.example.audioplayer.ui.components.molecules.NavRow
import com.example.audioplayer.ui.components.templates.Page
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun FoldersScreen() {
    val a = LocalActions.current
    val c = LocalIos.current
    val lib by a.vm.library.collectAsState()
    Page("Folder", actions = { GlassIconButton(Icons.Add, a.pickFolder) }) {
        item {
            NavRow("Tambah Folder…", a.pickFolder, leading = { FolderTile(c.accent, Icons.Add) }, subtitle = "Pilih folder musik dari penyimpanan")
        }
        items(lib.folders, key = { it.path }) { f ->
            NavRow(
                f.name, { a.nav.push(Route.FolderDetail(f.path)) },
                leading = { FolderTile(c.accent, Icons.Folder) },
                subtitle = "${f.songs.size} lagu",
            )
        }
    }
}

@Composable
private fun FolderTile(tint: Color, icon: ImageVector) {
    Box(
        Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(LocalIos.current.fill),
        Alignment.Center,
    ) { Ico(icon, tint, size = 26.dp) }
}
