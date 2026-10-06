package com.example.audioplayer.ui.screens.nowplaying

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.PlayerViewModel
import com.example.audioplayer.ui.components.atoms.IosSlider
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.util.formatTime

@Composable
internal fun Scrubber(vm: PlayerViewModel, durationMs: Long) {
    val position by vm.position.collectAsState()
    var drag by remember { mutableStateOf<Float?>(null) }
    val fraction = drag ?: if (durationMs > 0) position.toFloat() / durationMs else 0f
    val shown = drag?.let { (it * durationMs).toLong() } ?: position
    Column(Modifier.padding(top = 10.dp)) {
        IosSlider(
            fraction.coerceIn(0f, 1f),
            onValueChange = { drag = it },
            onValueChangeFinished = {
                drag?.let { vm.seekTo((it * durationMs).toLong()) }
                drag = null
            },
            modifier = Modifier.fillMaxWidth(),
            active = if (drag != null) Color.White else White85,
            inactive = White25,
        )
        Row(Modifier.fillMaxWidth()) {
            Txt(formatTime(shown), IosType.caption, Modifier.weight(1f), color = White60)
            Txt("-" + formatTime(durationMs - shown), IosType.caption, color = White60)
        }
    }
}
