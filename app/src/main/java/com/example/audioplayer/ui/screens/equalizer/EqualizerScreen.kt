package com.example.audioplayer.ui.screens.equalizer

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.example.audioplayer.player.equalizer.EqualizerSettings
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.molecules.SettingsGroup
import com.example.audioplayer.ui.components.molecules.SliderRow
import com.example.audioplayer.ui.components.molecules.SwitchRow
import com.example.audioplayer.ui.components.templates.Page
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun EqualizerScreen() {
    val store = LocalActions.current.vm.equalizer
    val eq by store.settings.collectAsState()
    val supported by store.supported.collectAsState()

    // Mengubah preset/band langsung menyalakan equalizer supaya hasilnya langsung terdengar
    fun tune(transform: (EqualizerSettings) -> EqualizerSettings) =
        store.update { transform(it).copy(enabled = true) }

    Page("Equalizer", background = LocalIos.current.groupedBackground) {
        item {
            SettingsGroup(
                "EQUALIZER",
                footer = if (supported == false) "Perangkat ini tidak menyediakan equalizer, jadi pengaturan tidak berpengaruh."
                else "Berlaku untuk semua lagu yang diputar di aplikasi ini.",
            ) {
                SwitchRow("Aktifkan", eq.enabled) { on -> store.update { it.copy(enabled = on) } }
            }
        }
        item {
            Column(Modifier.alpha(if (eq.enabled) 1f else 0.5f)) {
                SettingsGroup("PRESET") {
                    PresetPicker(eq.presetId) { preset -> tune { it.withPreset(preset) } }
                }
                SettingsGroup("BAND") {
                    BandSliders(eq.gainsDb) { band, db -> tune { it.withBand(band, db) } }
                }
                SettingsGroup("BASS BOOST") {
                    SliderRow("Kekuatan", eq.bassBoost) { v -> tune { it.copy(bassBoost = v) } }
                }
            }
        }
    }
}
