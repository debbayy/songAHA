package com.example.audioplayer.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.Route
import com.example.audioplayer.ui.addToPlaylist
import com.example.audioplayer.ui.components.atoms.Artwork
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.noRippleClick
import com.example.audioplayer.ui.components.molecules.EmptyState
import com.example.audioplayer.ui.components.molecules.GlassIconButton
import com.example.audioplayer.ui.components.molecules.PlayShuffleButtons
import com.example.audioplayer.ui.components.organisms.songList
import com.example.audioplayer.ui.components.templates.Page
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos
import com.example.audioplayer.ui.theme.rememberArtTint
import com.example.audioplayer.ui.util.songCountLabel

@Composable
fun AlbumDetailScreen(key: String) {
    val a = LocalActions.current
    val c = LocalIos.current
    val lib by a.vm.library.collectAsState()
    val player by a.vm.player.collectAsState()
    val album = lib.albumByKey[key]
    if (album == null) {
        Page("Album") { item { EmptyState("Album Tidak Ditemukan", "Album ini sudah tidak ada di perangkat.") } }
        return
    }
    val tint = rememberArtTint(album.cover, c.fill)
    Page(
        album.title,
        actions = { GlassIconButton(Icons.More, { a.addToPlaylist(album.songs) }) },
        header = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(tint.copy(alpha = 0.55f), c.background)))
                    .statusBarsPadding()
                    .padding(top = 64.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Artwork(
                    album.cover, 250.dp,
                    Modifier.size(250.dp).shadow(18.dp, RoundedCornerShape(12.dp)),
                    corner = 12.dp,
                )
                Txt(album.title, IosType.title2, Modifier.padding(top = 18.dp, start = 24.dp, end = 24.dp), align = TextAlign.Center, maxLines = 2)
                Txt(
                    album.artist, IosType.title3.copy(fontWeight = FontWeight.Normal),
                    Modifier.padding(top = 2.dp).noRippleClick { a.nav.push(Route.ArtistDetail(album.artist)) },
                    color = c.accent,
                )
                Txt("Album · ${songCountLabel(album.songs)}", IosType.footnote, Modifier.padding(top = 4.dp, bottom = 16.dp), color = c.secondaryLabel)
                PlayShuffleButtons({ a.vm.playSongs(album.songs) }, { a.vm.shuffle(album.songs) })
            }
        },
    ) {
        songList(
            album.songs, player, a, numbered = true,
            subtitle = { if (it.displayArtist != album.artist) it.displayArtist else "" },
        )
    }
}
