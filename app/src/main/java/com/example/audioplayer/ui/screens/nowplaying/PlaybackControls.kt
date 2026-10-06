package com.example.audioplayer.ui.screens.nowplaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.PlayerState
import com.example.audioplayer.ui.PlayerViewModel
import com.example.audioplayer.ui.components.atoms.Icons

@Composable
internal fun PlaybackControls(p: PlayerState, vm: PlayerViewModel) {
    Row(
        Modifier.fillMaxWidth().height(104.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ControlButton(Icons.Previous, 42.dp) { vm.previous() }
        ControlButton(if (p.isPlaying) Icons.Pause else Icons.Play, 58.dp) { vm.togglePlay() }
        ControlButton(Icons.Next, 42.dp) { vm.next() }
    }
}
