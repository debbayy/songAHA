package com.example.audioplayer.ui.components.atoms

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Slider tegak dua arah (-1..1) dengan titik nol di tengah, untuk band equalizer.
 * Isian berwarna tumbuh dari tengah ke arah nilai, kenop bulat mengikuti jari.
 */
@Composable
fun VerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    active: Color,
    inactive: Color,
    thumb: Color = Color.White,
) {
    val change by rememberUpdatedState(onValueChange)
    Canvas(
        modifier
            .width(36.dp)
            .pointerInput(Unit) {
                // posisi y (atas = +1, bawah = -1), dengan jarak kenop di tepi
                fun valueAt(y: Float): Float {
                    val inset = 12.dp.toPx()
                    val t = ((y - inset) / (size.height - inset * 2)).coerceIn(0f, 1f)
                    return 1f - t * 2f
                }
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    change(valueAt(down.position.y))
                    while (true) {
                        val pointer = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                        if (!pointer.pressed) break
                        change(valueAt(pointer.position.y))
                        pointer.consume()
                    }
                }
            }
    ) {
        val inset = 12.dp.toPx()
        val trackWidth = 4.dp.toPx()
        val x = size.width / 2
        val top = inset
        val bottom = size.height - inset
        val center = (top + bottom) / 2
        val y = center - value.coerceIn(-1f, 1f) * (bottom - top) / 2
        val radius = CornerRadius(trackWidth / 2)

        drawRoundRect(inactive, Offset(x - trackWidth / 2, top), Size(trackWidth, bottom - top), radius)
        drawRoundRect(active, Offset(x - trackWidth / 2, min(y, center)), Size(trackWidth, max(abs(center - y), 1f)), radius)
        drawLine(inactive, Offset(x - 8.dp.toPx(), center), Offset(x + 8.dp.toPx(), center), strokeWidth = 1.dp.toPx())
        drawCircle(Color(0x33000000), radius = 11.dp.toPx(), center = Offset(x, y + 1.dp.toPx()))
        drawCircle(thumb, radius = 10.dp.toPx(), center = Offset(x, y))
    }
}
