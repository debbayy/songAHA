package com.example.audioplayer.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.atoms.Artwork
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.molecules.EmptyState
import com.example.audioplayer.ui.components.molecules.PlayShuffleButtons
import com.example.audioplayer.ui.components.molecules.SectionHeader
import com.example.audioplayer.ui.components.organisms.AlbumCarousel
import com.example.audioplayer.ui.components.organisms.songList
import com.example.audioplayer.ui.components.templates.Page
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos
import com.example.audioplayer.ui.theme.rememberArtTint
import com.example.audioplayer.ui.util.songCountLabel

@Composable
fun ArtistDetailScreen(name: String) {
    val a = LocalActions.current
    val c = LocalIos.current
    val lib by a.vm.library.collectAsState()
    val player by a.vm.player.collectAsState()
    val artist = lib.artistByName[name]
    if (artist == null) {
        Page(name) { item { EmptyState("Artis Tidak Ditemukan", "Artis ini sudah tidak ada di perangkat.") } }
        return
    }
    val cover = artist.albums.firstOrNull()?.cover ?: artist.songs.first()
    val tint = rememberArtTint(cover, c.background)
    val pageBg = if (c.background.alpha < 1f) tint.copy(alpha = if (c.isDark) 0.22f else 0.14f) // di atas wallpaper
    else lerp(c.background, tint, if (c.isDark) 0.22f else 0.14f)

    Page(
        artist.name,
        background = pageBg,
        header = {
            Column {
                // Foto artis menyatu dengan konten di bawahnya
                Box(Modifier.fillMaxWidth().aspectRatio(1.05f)) {
                    Artwork(cover, 420.dp, Modifier.fillMaxSize(), corner = 0.dp, iconScale = 0.3f)
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(0f to Color(0x33000000), 0.25f to Color.Transparent, 0.6f to Color.Transparent, 1f to pageBg)
                        )
                    )
                    Txt(artist.name, IosType.largeTitle, Modifier.align(Alignment.BottomStart).padding(16.dp), maxLines = 2)
                }
                Txt(
                    songCountLabel(artist.songs) + if (artist.albums.isNotEmpty()) " · ${artist.albums.size} album" else "",
                    IosType.subhead, Modifier.padding(start = 16.dp, bottom = 14.dp), color = c.secondaryLabel,
                )
                PlayShuffleButtons({ a.vm.playSongs(artist.songs) }, { a.vm.shuffle(artist.songs) })
                SectionHeader("Lagu")
            }
        },
    ) {
        songList(artist.songs, player, a, subtitle = { it.album })
        if (artist.albums.isNotEmpty()) {
            item { SectionHeader("Album") }
            item { AlbumCarousel(artist.albums) }
        }
    }
}
