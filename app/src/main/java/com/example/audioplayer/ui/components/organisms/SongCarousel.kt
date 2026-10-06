package com.example.audioplayer.ui.components.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.atoms.Artwork
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.bounce
import com.example.audioplayer.ui.songMenu
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun SongCarousel(songs: List<Song>) {
    val a = LocalActions.current
    val c = LocalIos.current
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        itemsIndexed(songs, key = { i, s -> "${s.id}#$i" }) { i, s ->
            Column(Modifier.width(150.dp).bounce(onLongClick = { a.songMenu(s) }) { a.vm.playSongs(songs, i) }) {
                Artwork(s, 150.dp, Modifier.size(150.dp), corner = 10.dp)
                Txt(s.title, IosType.footnote.copy(fontWeight = FontWeight.Medium), Modifier.padding(top = 6.dp))
                Txt(s.displayArtist, IosType.footnote, color = c.secondaryLabel)
            }
        }
    }
}
