package com.example.audioplayer.ui.components.foundation

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp

/** Isi yang digulir memudar di tepi atas & bawah, bukan terpotong tajam. */
fun Modifier.fadingEdges(top: Dp, bottom: Dp): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        val topFraction = (top.toPx() / size.height).coerceIn(0f, 0.5f)
        val bottomFraction = (bottom.toPx() / size.height).coerceIn(0f, 0.5f)
        drawRect(
            Brush.verticalGradient(
                0f to Color.Transparent,
                topFraction to Color.Black,
                1f - bottomFraction to Color.Black,
                1f to Color.Transparent,
            ),
            blendMode = BlendMode.DstIn,
        )
    }
