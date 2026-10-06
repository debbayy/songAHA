package com.example.audioplayer.ui.components.molecules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.Album
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.Route
import com.example.audioplayer.ui.components.atoms.Artwork
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.bounce
import com.example.audioplayer.ui.songMenu
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun AlbumTile(album: Album, modifier: Modifier = Modifier, width: Dp? = null) {
    val a = LocalActions.current
    val c = LocalIos.current
    Column(
        modifier
            .then(if (width != null) Modifier.width(width) else Modifier)
            .bounce(onLongClick = { a.songMenu(album.cover) }) { a.nav.push(Route.AlbumDetail(album.key)) }
    ) {
        Artwork(album.cover, width ?: 200.dp, Modifier.fillMaxWidth().aspectRatio(1f), corner = 10.dp)
        Txt(album.title, IosType.footnote.copy(fontWeight = FontWeight.Medium), Modifier.padding(top = 6.dp))
        Txt(album.artist, IosType.footnote, color = c.secondaryLabel)
    }
}
