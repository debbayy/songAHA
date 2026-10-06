package com.example.audioplayer.ui.screens.nowplaying

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.foundation.bounce

@Composable
internal fun ControlButton(icon: ImageVector, size: Dp, onClick: () -> Unit) {
    Box(Modifier.size(size + 30.dp).clip(CircleShape).bounce(onClick = onClick), Alignment.Center) {
        Ico(icon, Color.White, size = size)
    }
}
