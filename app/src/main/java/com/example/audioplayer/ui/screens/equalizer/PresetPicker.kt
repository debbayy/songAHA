package com.example.audioplayer.ui.screens.equalizer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.audioplayer.player.equalizer.EQ_CUSTOM_ID
import com.example.audioplayer.player.equalizer.EQ_PRESETS
import com.example.audioplayer.player.equalizer.EqPreset
import com.example.audioplayer.ui.components.molecules.Chip

/** Deretan preset yang bisa digeser. "Kustom" muncul saat band diatur manual. */
@Composable
fun PresetPicker(selectedId: String, onSelect: (EqPreset) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (selectedId == EQ_CUSTOM_ID) item { Chip("Kustom", selected = true, onClick = {}) }
        items(EQ_PRESETS, key = { it.id }) { preset ->
            Chip(preset.label, selected = preset.id == selectedId, onClick = { onSelect(preset) })
        }
    }
}
