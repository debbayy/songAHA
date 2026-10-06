package com.example.audioplayer.ui.screens.nowplaying

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.pressable

internal enum class LyricLineState { Sung, Active, Upcoming }

/** Gaya besar & tebal seperti lirik Apple Music. */
private val LyricStyle = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp)

/** Satu baris lirik. Baris instrumental (kosong) ditampilkan sebagai ♪. */
@Composable
internal fun LyricLineText(text: String, state: LyricLineState, onClick: () -> Unit) {
    val color by animateColorAsState(
        when (state) {
            LyricLineState.Active -> Color.White
            LyricLineState.Sung -> White25
            LyricLineState.Upcoming -> White40
        },
        tween(300),
        label = "lyric",
    )
    Txt(
        text.ifBlank { "♪" },
        LyricStyle,
        Modifier.fillMaxWidth().pressable(highlight = false, onClick = onClick).padding(vertical = 10.dp),
        color = color,
        maxLines = Int.MAX_VALUE,
    )
}
