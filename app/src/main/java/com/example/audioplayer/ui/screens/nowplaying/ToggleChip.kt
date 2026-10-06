package com.example.audioplayer.ui.screens.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.foundation.bounce

@Composable
internal fun ToggleChip(icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(width = 52.dp, height = 34.dp)
            .clip(CircleShape)
            .background(if (active) Color.White else White15)
            .bounce(onClick = onClick),
        Alignment.Center,
    ) { Ico(icon, if (active) Color.Black else White85, size = 20.dp) }
}
