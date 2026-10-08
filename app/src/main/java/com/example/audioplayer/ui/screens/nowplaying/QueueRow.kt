package com.example.audioplayer.ui.screens.nowplaying

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.components.atoms.Artwork
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.pressable
import com.example.audioplayer.ui.theme.IosType

/** Satu lagu di antrean "Selanjutnya": ketuk untuk memutar, (x) untuk mengeluarkan dari antrean. */
@Composable
internal fun QueueRow(song: Song, onPlay: () -> Unit, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).pressable(onClick = onPlay).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Artwork(song, 44.dp, Modifier.size(44.dp), corner = 6.dp)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Txt(song.title, IosType.body, color = Color.White)
            Txt(song.displayArtist, IosType.footnote, color = White60)
        }
        Box(Modifier.size(40.dp).pressable(highlight = false, onClick = onRemove), Alignment.Center) {
            Ico(Icons.CloseCircle, White60, size = 20.dp)
        }
    }
}
