package com.example.audioplayer.ui.screens.nowplaying

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import kotlin.math.abs

/** Seberapa jauh (relatif lebar) cover harus digeser supaya lagu berganti. */
private const val SWIPE_THRESHOLD = 0.25f

/**
 * Cover yang bisa digeser: geser ke kanan = lagu berikutnya, ke kiri = lagu sebelumnya.
 * Cover ikut jari, lalu keluar layar dan cover lagu baru masuk dari sisi seberang.
 */
@Composable
internal fun SwipeableArtwork(
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(0f) }

    Box(
        modifier
            .pointerInput(Unit) {
                val width = size.width.toFloat()
                detectHorizontalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            val direction = when {
                                offset.value > width * SWIPE_THRESHOLD -> 1f
                                offset.value < -width * SWIPE_THRESHOLD -> -1f
                                else -> 0f
                            }
                            if (direction == 0f) {
                                offset.animateTo(0f, spring(dampingRatio = 0.7f))
                                return@launch
                            }
                            offset.animateTo(direction * width, tween(160))
                            if (direction > 0) onSwipeRight() else onSwipeLeft()
                            offset.snapTo(-direction * width * 0.6f)
                            offset.animateTo(0f, spring(dampingRatio = 0.8f))
                        }
                    },
                    onDragCancel = { scope.launch { offset.animateTo(0f) } },
                ) { change, dx ->
                    change.consume()
                    scope.launch { offset.snapTo(offset.value + dx) }
                }
            }
            .graphicsLayer {
                translationX = offset.value
                alpha = 1f - (abs(offset.value) / size.width).coerceIn(0f, 1f) * 0.6f
            },
        contentAlignment = Alignment.Center,
    ) { content() }
}
