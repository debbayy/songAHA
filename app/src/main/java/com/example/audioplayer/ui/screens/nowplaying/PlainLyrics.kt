package com.example.audioplayer.ui.screens.nowplaying

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.lyrics.Lyrics
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.fadingEdges
import com.example.audioplayer.ui.theme.IosType

/** Lirik tanpa penanda waktu: cukup ditampilkan sebagai teks yang bisa digulir. */
@Composable
internal fun PlainLyrics(lyrics: Lyrics, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fadingEdges(top = 24.dp, bottom = 48.dp), contentPadding = PaddingValues(vertical = 24.dp)) {
        items(lyrics.lines) { line ->
            Txt(line.text, IosType.title3, Modifier.padding(vertical = 3.dp), color = White85, maxLines = Int.MAX_VALUE)
        }
    }
}
