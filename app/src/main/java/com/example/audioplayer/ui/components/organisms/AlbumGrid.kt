package com.example.audioplayer.ui.components.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.Album
import com.example.audioplayer.ui.components.molecules.AlbumTile

/** Grid album 2 kolom, dibangun per baris supaya tetap lazy di dalam LazyColumn. */
fun LazyListScope.albumGrid(albums: List<Album>) {
    items(albums.chunked(2), key = { row -> row.first().key }) { row ->
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            row.forEach { AlbumTile(it, Modifier.weight(1f)) }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}
