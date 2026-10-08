package com.example.audioplayer.ui.screens.nowplaying

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.atoms.VerticalFillSlider
import com.example.audioplayer.ui.theme.IosType
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** Panel menutup sendiri setelah sekian lama tanpa perubahan volume. */
private const val AUTO_HIDE_MS = 3_000L

/**
 * Panel volume yang meluncur masuk dari kanan, mirip panel volume sistem saat tombol fisik
 * ditekan. Geser naik/turun untuk mengatur; tombol fisik juga ikut menggerakkannya.
 */
@Composable
internal fun BoxScope.VolumePanel(visible: Boolean, onDismiss: () -> Unit) {
    val volume = LocalActions.current.vm.volume
    val level by volume.level.collectAsState()

    // hitungan mundur diulang setiap volume berubah (dari slider maupun tombol fisik)
    LaunchedEffect(visible, level) {
        if (visible) {
            delay(AUTO_HIDE_MS)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible,
        Modifier.align(Alignment.CenterEnd).padding(end = 14.dp),
        enter = slideInHorizontally { it * 2 } + fadeIn(),
        exit = slideOutHorizontally { it * 2 } + fadeOut(),
    ) {
        Column(
            Modifier.clip(RoundedCornerShape(30.dp)).background(Color(0x59000000)).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Txt("${(level * 100).roundToInt()}", IosType.caption, Modifier.padding(vertical = 4.dp), color = White85)
            VerticalFillSlider(
                level, volume::set, Modifier.size(width = 52.dp, height = 210.dp),
                fill = Color.White, track = White25,
            ) {
                // ikon gelap saat isian putih sudah menutupi dasar slider
                Ico(
                    volumeIcon(level), if (level > 0.12f) Color(0xB3000000) else White85,
                    Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp), size = 24.dp,
                )
            }
        }
    }
}

/** Ikon speaker sesuai tingkat volume. */
internal fun volumeIcon(level: Float): ImageVector = when {
    level <= 0f -> Icons.VolumeOff
    level < 0.5f -> Icons.VolumeLow
    else -> Icons.VolumeHigh
}
