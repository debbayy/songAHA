package com.example.audioplayer.ui.screens.nowplaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.NowPlayingPanel
import com.example.audioplayer.ui.Route
import com.example.audioplayer.ui.components.atoms.Icons

/** Baris paling bawah Now Playing: lirik, timer tidur, equalizer, antrean. */
@Composable
internal fun BottomRow() {
    val a = LocalActions.current
    val nav = a.nav
    val eq by a.vm.equalizer.settings.collectAsState()

    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleToggle(Icons.Lyrics, nav.panel == NowPlayingPanel.Lyrics) { nav.togglePanel(NowPlayingPanel.Lyrics) }
        SleepTimerChip()
        CircleToggle(Icons.Equalizer, eq.enabled) { nav.push(Route.Equalizer) }
        CircleToggle(Icons.Queue, nav.panel == NowPlayingPanel.Queue) { nav.togglePanel(NowPlayingPanel.Queue) }
    }
}
