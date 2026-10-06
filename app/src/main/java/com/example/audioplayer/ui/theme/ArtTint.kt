package com.example.audioplayer.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.audioplayer.data.ArtworkLoader
import com.example.audioplayer.data.Song

/** Warna dominan cover, dianimasikan. Dipakai untuk mewarnai halaman album/artis (gaya iOS 27). */
@Composable
fun rememberArtTint(song: Song?, fallback: Color): Color {
    val context = LocalContext.current
    val color by produceState<Color?>(null, song?.artKey) {
        value = song?.let { ArtworkLoader.dominantColor(context, it) }?.let { Color(it) }
    }
    val animated by animateColorAsState(color ?: fallback, tween(500), label = "tint")
    return animated
}
