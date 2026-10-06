package com.example.audioplayer.ui.components.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.foundation.bounce
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun PillButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
) {
    val c = LocalIos.current
    val fg = if (filled) c.onAccent else c.accent
    Row(
        modifier
            .height(48.dp)
            .clip(CircleShape)
            .background(if (filled) c.accent else c.fill)
            .bounce(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Ico(icon, fg, size = 20.dp)
        Spacer(Modifier.width(6.dp))
        Txt(label, IosType.headline, color = fg)
    }
}
