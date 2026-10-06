package com.example.audioplayer.ui.screens.settings.sections

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.Route
import com.example.audioplayer.ui.components.molecules.LinkRow
import com.example.audioplayer.ui.components.molecules.SettingsGroup

/** Pintu masuk ke Equalizer, menampilkan preset yang sedang aktif. */
@Composable
fun SoundSection() {
    val a = LocalActions.current
    val eq by a.vm.equalizer.settings.collectAsState()
    SettingsGroup("SUARA") {
        LinkRow("Equalizer", if (eq.enabled) eq.presetLabel else "Mati") { a.nav.push(Route.Equalizer) }
    }
}
