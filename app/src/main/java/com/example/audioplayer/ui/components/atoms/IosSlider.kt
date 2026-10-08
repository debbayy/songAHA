package com.example.audioplayer.ui.components.atoms

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/** Slider tanpa knob yang menebal saat disentuh (scrubber Now Playing iOS). */
@Composable
fun IosSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: () -> Unit = {},
    active: Color,
    inactive: Color,
) {
    val change by rememberUpdatedState(onValueChange)
    val finished by rememberUpdatedState(onValueChangeFinished)
    var dragging by remember { mutableStateOf(false) }
    val thickness by animateDpAsState(if (dragging) 12.dp else 7.dp, tween(150), label = "track")
    val scaleX by animateFloatAsState(if (dragging) 1.03f else 1f, tween(150), label = "grow")

    Box(
        modifier
            .height(32.dp)
            .graphicsLayer { this.scaleX = scaleX }
            .pointerInput(Unit) {
                fun frac(x: Float) = (x / size.width).coerceIn(0f, 1f)
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    dragging = true
                    change(frac(down.position.x))
                    while (true) {
                        val event = awaitPointerEvent()
                        val c = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!c.pressed) break
                        change(frac(c.position.x))
                        c.consume()
                    }
                    dragging = false
                    finished()
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.fillMaxWidth().height(thickness).clip(CircleShape).background(inactive)) {
            Box(Modifier.fillMaxWidth(value.coerceIn(0f, 1f)).fillMaxHeight().background(active))
        }
    }
}
