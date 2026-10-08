package com.example.audioplayer.ui.screens.nowplaying

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.PlayerState
import com.example.audioplayer.ui.PlayerViewModel
import com.example.audioplayer.ui.components.atoms.Artwork
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.songMenu
import com.example.audioplayer.ui.theme.IosType

@Composable
internal fun ArtworkPanel(p: PlayerState, vm: PlayerViewModel, song: Song) {
    val a = LocalActions.current
    val favs by vm.store.favorites.collectAsState()
    Column {
        SwipeableArtwork(
            onSwipeRight = { vm.next() },
            onSwipeLeft = { vm.previousSong() },
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) {
            // Cover mengecil saat pause, khas Apple Music
            val scale by animateFloatAsState(if (p.isPlaying) 1f else 0.82f, spring(dampingRatio = 0.62f, stiffness = 260f), label = "art")
            Artwork(
                song, 380.dp,
                Modifier
                    .aspectRatio(1f)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        shadowElevation = 28.dp.toPx() * scale
                        shape = RoundedCornerShape(14.dp)
                        clip = true
                    },
                corner = 14.dp, iconScale = 0.3f,
            )
        }
        Row(Modifier.padding(top = 18.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Txt(song.title, IosType.title3, color = Color.White)
                Txt(song.displayArtist, IosType.title3.copy(fontWeight = FontWeight.Normal), color = White60)
            }
            val fav = song.id in favs
            RoundButton(if (fav) Icons.HeartFill else Icons.Heart, { vm.store.toggleFavorite(song.id) }, tint = if (fav) Color.White else White85)
            Spacer(Modifier.width(10.dp))
            RoundButton(Icons.More, { a.songMenu(song) })
        }
    }
}
