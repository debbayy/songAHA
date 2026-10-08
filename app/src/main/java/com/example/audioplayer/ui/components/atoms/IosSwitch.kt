package com.example.audioplayer.ui.components.atoms

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.foundation.noRippleClick
import com.example.audioplayer.ui.theme.LocalIos

private val TrackWidth = 51.dp
private val TrackHeight = 31.dp
private val ThumbSize = 27.dp
private val ThumbPadding = 2.dp

/** Saklar on/off ala iOS dengan kenop yang meluncur. */
@Composable
fun IosSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalIos.current
    val track by animateColorAsState(if (checked) c.accent else c.fill, label = "track")
    // Spring sedikit memantul melewati ujungnya (bisa < 0), jadi kenop digeser dengan offset,
    // bukan padding: padding negatif membuat Compose crash
    val thumbX by animateDpAsState(
        if (checked) TrackWidth - ThumbSize - ThumbPadding * 2 else 0.dp,
        spring(dampingRatio = 0.7f),
        label = "thumb",
    )
    Box(
        modifier
            .size(TrackWidth, TrackHeight)
            .clip(CircleShape)
            .background(track)
            .noRippleClick { onCheckedChange(!checked) }
            .padding(ThumbPadding)
    ) {
        Box(
            Modifier
                .offset { IntOffset(thumbX.roundToPx(), 0) }
                .size(ThumbSize)
                .shadow(2.dp, CircleShape)
                .background(Color.White, CircleShape)
        )
    }
}
