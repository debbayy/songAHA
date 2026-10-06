package com.example.audioplayer.ui.screens.nowplaying

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.components.atoms.Artwork
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.theme.IosType

/** Ringkasan lagu di atas panel lirik & antrean: cover kecil, judul, artis, tombol "…". */
@Composable
internal fun NowPlayingHeader(song: Song, onMore: () -> Unit) {
    Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Artwork(song, 64.dp, Modifier.size(64.dp), corner = 8.dp)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Txt(song.title, IosType.headline, color = Color.White)
            Txt(song.displayArtist, IosType.subhead, color = White60)
        }
        RoundButton(Icons.More, onMore)
    }
}
