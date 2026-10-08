package com.example.audioplayer.ui.screens.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.foundation.noRippleClick

/** Baris atas Now Playing: garis untuk menutup (tengah) dan tombol volume (kanan). */
@Composable
internal fun NowPlayingTopBar(volumeOpen: Boolean, onToggleVolume: () -> Unit) {
    val a = LocalActions.current
    val level by a.vm.volume.level.collectAsState()

    Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.size(40.dp)) // penyeimbang supaya garis tetap di tengah
        Box(
            Modifier.weight(1f).height(40.dp).noRippleClick { a.nav.nowPlayingOpen = false },
            Alignment.Center,
        ) {
            Box(Modifier.size(38.dp, 5.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.45f)))
        }
        CircleToggle(volumeIcon(level), volumeOpen, onToggleVolume)
    }
}
