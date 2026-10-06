package com.example.audioplayer.ui.components.foundation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.audioplayer.ui.theme.LocalIos

/** Klik ala iOS: baris jadi sedikit abu-abu saat ditekan, tanpa ripple. */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.pressable(
    highlight: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val c = LocalIos.current
    this
        .then(if (highlight && pressed) Modifier.background(c.pressed) else Modifier)
        .combinedClickable(
            interactionSource = source,
            indication = null,
            onLongClick = onLongClick?.let { long ->
                {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    long()
                }
            },
            onClick = onClick,
        )
}

/** Tombol "membal": mengecil sedikit saat ditekan, seperti kontrol iOS 26/27. */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.bounce(onLongClick: (() -> Unit)? = null, onClick: () -> Unit): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        if (pressed) 0.92f else 1f,
        spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "bounce",
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            alpha = if (pressed) 0.8f else 1f
        }
        .combinedClickable(
            interactionSource = source,
            indication = null,
            onLongClick = onLongClick?.let { long ->
                {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    long()
                }
            },
            onClick = onClick,
        )
}

fun Modifier.noRippleClick(onClick: () -> Unit): Modifier = composed {
    clickable(remember { MutableInteractionSource() }, indication = null, onClick = onClick)
}
