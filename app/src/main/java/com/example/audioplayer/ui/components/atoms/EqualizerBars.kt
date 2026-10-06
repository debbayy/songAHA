package com.example.audioplayer.ui.components.atoms

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

/** Ikon equalizer kecil untuk lagu yang sedang diputar. Diam (tanpa animasi) saat pause. */
@Composable
fun EqualizerBars(playing: Boolean, color: Color, modifier: Modifier = Modifier) {
    if (playing) {
        val t = rememberInfiniteTransition(label = "eq")
        val a by t.animateFloat(0.25f, 1f, infiniteRepeatable(tween(420), RepeatMode.Reverse), label = "a")
        val b by t.animateFloat(1f, 0.3f, infiniteRepeatable(tween(560), RepeatMode.Reverse), label = "b")
        val d by t.animateFloat(0.4f, 0.9f, infiniteRepeatable(tween(360), RepeatMode.Reverse), label = "d")
        Bars(listOf(a, b, d), color, modifier)
    } else {
        Bars(listOf(0.35f, 0.6f, 0.45f), color, modifier)
    }
}

@Composable
private fun Bars(levels: List<Float>, color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width / 5f
        levels.forEachIndexed { i, v ->
            val h = size.height * v
            drawRoundRect(color, Offset(i * 2 * w, size.height - h), Size(w, h), CornerRadius(w / 2))
        }
    }
}
