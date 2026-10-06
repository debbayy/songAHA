package com.example.audioplayer.ui.components.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.PillButton

@Composable
fun PlayShuffleButtons(onPlay: () -> Unit, onShuffle: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PillButton("Putar", Icons.Play, onPlay, Modifier.weight(1f))
        PillButton("Acak", Icons.Shuffle, onShuffle, Modifier.weight(1f))
    }
}
