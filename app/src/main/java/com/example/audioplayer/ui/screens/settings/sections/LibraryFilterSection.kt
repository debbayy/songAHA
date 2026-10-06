package com.example.audioplayer.ui.screens.settings.sections

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.LibraryFilter
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.atoms.InsetHairline
import com.example.audioplayer.ui.components.atoms.Segmented
import com.example.audioplayer.ui.components.molecules.SettingRow
import com.example.audioplayer.ui.components.molecules.SettingsGroup
import com.example.audioplayer.ui.components.molecules.SwitchRow

/** Sembunyikan audio yang bukan musik: voice note, rekaman, nada dering, potongan pendek. */
@Composable
fun LibraryFilterSection() {
    val vm = LocalActions.current.vm
    val settings by vm.store.settings.collectAsState()
    val filter = settings.filter
    val hidden by vm.hiddenCount.collectAsState()

    fun update(transform: (LibraryFilter) -> LibraryFilter) =
        vm.store.updateSettings { it.copy(filter = transform(it.filter)) }

    val footer = if (hidden > 0) "$hidden file audio disembunyikan dari pustaka."
    else "Voice note WhatsApp, rekaman suara/telepon, dan nada dering tidak ditampilkan."

    SettingsGroup("SARING AUDIO", footer = footer) {
        SwitchRow("Sembunyikan Rekaman", filter.hideRecordings) { on -> update { it.copy(hideRecordings = on) } }
        InsetHairline()
        SettingRow("Durasi Minimum") {
            val options = LibraryFilter.DURATION_OPTIONS
            Segmented(
                options.map { if (it == 0) "Mati" else "$it dtk" },
                options.indexOf(filter.minDurationSec).coerceAtLeast(0),
                { i -> update { it.copy(minDurationSec = options[i]) } },
                Modifier.width(180.dp),
            )
        }
    }
}
