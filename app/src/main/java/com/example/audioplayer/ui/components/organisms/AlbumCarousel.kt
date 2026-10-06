package com.example.audioplayer.ui.components.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.Album
import com.example.audioplayer.ui.components.molecules.AlbumTile

@Composable
fun AlbumCarousel(albums: List<Album>) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(albums, key = { it.key }) { AlbumTile(it, width = 160.dp) }
    }
}
