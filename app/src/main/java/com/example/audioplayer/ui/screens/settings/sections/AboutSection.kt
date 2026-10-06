package com.example.audioplayer.ui.screens.settings.sections

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.atoms.InsetHairline
import com.example.audioplayer.ui.components.molecules.InfoRow
import com.example.audioplayer.ui.components.molecules.SettingsGroup

/** Ringkasan isi pustaka dan versi aplikasi. */
@Composable
fun AboutSection() {
    val lib by LocalActions.current.vm.library.collectAsState()
    SettingsGroup("TENTANG") {
        InfoRow("Lagu", "${lib.songs.size}")
        InsetHairline()
        InfoRow("Album", "${lib.albums.size}")
        InsetHairline()
        InfoRow("Artis", "${lib.artists.size}")
        InsetHairline()
        InfoRow("Versi", "1.0")
    }
}
