package com.example.audioplayer.ui.components.molecules

import androidx.compose.runtime.Composable
import com.example.audioplayer.ui.components.atoms.IosSwitch

/** Baris pengaturan dengan saklar di kanan. */
@Composable
fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    SettingRow(label) { IosSwitch(checked, onCheckedChange) }
}
