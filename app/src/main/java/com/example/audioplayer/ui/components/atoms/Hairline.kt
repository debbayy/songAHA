package com.example.audioplayer.ui.components.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.theme.LocalIos

/** Garis pemisah tipis selebar penuh. */
@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(0.5.dp).background(LocalIos.current.separator))
}

/** Pemisah antarbaris di dalam card pengaturan (menjorok dari kiri seperti iOS). */
@Composable
fun InsetHairline() = Hairline(Modifier.padding(start = 16.dp))
