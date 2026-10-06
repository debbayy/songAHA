package com.example.audioplayer.ui.components.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.theme.LocalIos
import com.example.audioplayer.ui.theme.backdrop

/**
 * Tiruan Liquid Glass yang jalan di Android 8: isian tembus pandang + kilau di atas
 * + garis tepi bergradasi. Tidak memakai blur real-time (tidak tersedia sebelum Android 12);
 * kalau ada wallpaper, potongan wallpaper yang sudah diblur digambar di belakangnya.
 */
fun Modifier.glass(shape: Shape, elevation: Dp = 8.dp): Modifier = composed {
    val c = LocalIos.current
    this
        .shadow(elevation, shape, clip = false, ambientColor = Color(0x22000000), spotColor = Color(0x33000000))
        .clip(shape)
        .backdrop(frosted = true)
        .background(c.glassFill)
        .background(Brush.verticalGradient(listOf(c.glassSheen, Color.Transparent)))
        .border(0.8.dp, Brush.verticalGradient(listOf(c.glassEdgeTop, c.glassEdgeBottom)), shape)
}
