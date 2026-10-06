package com.example.audioplayer.ui.screens.settings.sections

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.atoms.InsetHairline
import com.example.audioplayer.ui.components.atoms.Segmented
import com.example.audioplayer.ui.components.molecules.SettingRow
import com.example.audioplayer.ui.components.molecules.SettingsGroup

/** Tema terang/gelap dan gaya kaca. */
@Composable
fun AppearanceSection() {
    val store = LocalActions.current.vm.store
    val settings by store.settings.collectAsState()

    SettingsGroup("TAMPILAN", footer = "Kaca \"Berwarna\" lebih pekat dan kontras, seperti pilihan tampilan Liquid Glass di iOS 27.") {
        SettingRow("Tema") {
            Segmented(
                listOf("Otomatis", "Terang", "Gelap"), settings.theme,
                { theme -> store.updateSettings { it.copy(theme = theme) } }, Modifier.width(210.dp),
            )
        }
        InsetHairline()
        SettingRow("Kaca") {
            Segmented(
                listOf("Bening", "Berwarna"), if (settings.glassTinted) 1 else 0,
                { i -> store.updateSettings { it.copy(glassTinted = i == 1) } }, Modifier.width(170.dp),
            )
        }
    }
}
