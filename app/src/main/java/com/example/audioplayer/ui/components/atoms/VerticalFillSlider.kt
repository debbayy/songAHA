package com.example.audioplayer.ui.components.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Slider tegak gaya Control Center iOS: isian naik dari bawah dan digeser relatif terhadap
 * jari (tidak melompat ke titik sentuh). [content] digambar di atasnya, mis. ikon speaker.
 */
@Composable
fun VerticalFillSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    fill: Color,
    track: Color,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val change by rememberUpdatedState(onValueChange)
    val current by rememberUpdatedState(value)
    Box(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(track)
            .pointerInput(Unit) {
                var level = 0f
                detectVerticalDragGestures(onDragStart = { level = current }) { pointer, dy ->
                    pointer.consume()
                    level = (level - dy / size.height).coerceIn(0f, 1f)
                    change(level)
                }
            }
    ) {
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(value.coerceIn(0f, 1f)).background(fill))
        content()
    }
}
