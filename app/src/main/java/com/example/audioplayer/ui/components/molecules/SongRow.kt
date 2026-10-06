package com.example.audioplayer.ui.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.components.atoms.Artwork
import com.example.audioplayer.ui.components.atoms.EqualizerBars
import com.example.audioplayer.ui.components.atoms.Hairline
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.pressable
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun SongRow(
    song: Song,
    subtitle: String,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMore: () -> Unit,
    number: Int? = null,
    separator: Boolean = true,
) {
    val c = LocalIos.current
    Row(
        Modifier.fillMaxWidth().pressable(onLongClick = onMore, onClick = onClick).padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (number != null) {
            Box(Modifier.width(26.dp), Alignment.CenterStart) {
                if (isCurrent) EqualizerBars(isPlaying, c.accent, Modifier.size(14.dp))
                else Txt("$number", IosType.body, color = c.secondaryLabel)
            }
        } else {
            Box(Modifier.size(48.dp)) {
                Artwork(song, 48.dp, Modifier.fillMaxSize())
                if (isCurrent) {
                    Box(
                        Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp)).background(Color(0x66000000)),
                        Alignment.Center,
                    ) { EqualizerBars(isPlaying, Color.White, Modifier.size(16.dp)) }
                }
            }
        }
        Box(Modifier.weight(1f).padding(start = 12.dp)) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = if (number != null) 50.dp else 64.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                    Txt(song.title, IosType.body, color = if (isCurrent) c.accent else c.label)
                    if (subtitle.isNotBlank()) Txt(subtitle, IosType.subhead, color = c.secondaryLabel)
                }
                Box(Modifier.size(44.dp).pressable(highlight = false, onClick = onMore), Alignment.Center) {
                    Ico(Icons.More, c.label, size = 20.dp)
                }
            }
            if (separator) Hairline(Modifier.align(Alignment.BottomStart))
        }
    }
}
