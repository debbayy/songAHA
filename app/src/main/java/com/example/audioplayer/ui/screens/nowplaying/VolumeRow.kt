package com.example.audioplayer.ui.screens.nowplaying

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.PlayerViewModel
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.IosSlider

@Composable
internal fun VolumeRow(vm: PlayerViewModel) {
    val volume by vm.volume.collectAsState()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Ico(Icons.VolumeLow, White60, size = 18.dp)
        IosSlider(
            volume, { vm.setVolume(it) }, Modifier.weight(1f).padding(horizontal = 10.dp),
            active = White85, inactive = White25,
        )
        Ico(Icons.VolumeHigh, White60, size = 20.dp)
    }
}
