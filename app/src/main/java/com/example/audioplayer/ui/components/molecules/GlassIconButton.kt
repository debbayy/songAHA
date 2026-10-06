package com.example.audioplayer.ui.components.molecules

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.foundation.bounce
import com.example.audioplayer.ui.components.foundation.glass
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun GlassIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tint: Color = LocalIos.current.label,
) {
    Box(modifier.size(size).glass(CircleShape, 4.dp).bounce(onClick = onClick), Alignment.Center) {
        Ico(icon, tint, size = size * 0.5f)
    }
}
