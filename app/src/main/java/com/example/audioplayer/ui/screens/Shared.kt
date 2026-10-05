package com.example.audioplayer.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.Album
import com.example.audioplayer.data.ArtworkLoader
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.AppActions
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.PlayerState
import com.example.audioplayer.ui.Route
import com.example.audioplayer.ui.components.Artwork
import com.example.audioplayer.ui.components.Hairline
import com.example.audioplayer.ui.components.Ico
import com.example.audioplayer.ui.components.Icons
import com.example.audioplayer.ui.components.PillButton
import com.example.audioplayer.ui.components.SheetAction
import com.example.audioplayer.ui.components.SongRow
import com.example.audioplayer.ui.components.Txt
import com.example.audioplayer.ui.components.bounce
import com.example.audioplayer.ui.components.pressable
import com.example.audioplayer.ui.songMenu
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

/** Daftar lagu standar. Tap = putar daftar ini mulai dari lagu tsb; tahan / "…" = menu. */
fun LazyListScope.songList(
    songs: List<Song>,
    player: PlayerState,
    actions: AppActions,
    numbered: Boolean = false,
    subtitle: (Song) -> String = { it.displayArtist },
    extra: (Song, Int) -> List<SheetAction> = { _, _ -> emptyList() },
) {
    itemsIndexed(songs, key = { i, s -> "${s.id}#$i" }) { i, s ->
        SongRow(
            song = s,
            subtitle = subtitle(s),
            isCurrent = s.id == player.current?.id,
            isPlaying = player.isPlaying,
            number = if (numbered) (s.track % 1000).takeIf { it > 0 } ?: (i + 1) else null,
            onClick = { actions.vm.playSongs(songs, i) },
            onMore = { actions.songMenu(s, extra(s, i)) },
        )
    }
}

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

@Composable
fun AlbumCarousel(albums: List<Album>) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(albums, key = { it.key }) { AlbumTile(it, width = 160.dp) }
    }
}

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

/** Baris navigasi dengan ikon & chevron (menu Pustaka, Folder, dll.). */
@Composable
fun NavRow(
    label: String,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    leading: (@Composable () -> Unit)? = null,
    subtitle: String? = null,
    big: Boolean = false,
) {
    val c = LocalIos.current
    Row(Modifier.fillMaxWidth().pressable(onClick = onClick).padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        when {
            leading != null -> leading()
            icon != null -> Ico(icon, c.accent, size = 26.dp)
        }
        Box(Modifier.weight(1f).padding(start = 14.dp)) {
            Row(
                Modifier.fillMaxWidth().height(if (subtitle != null) 64.dp else if (big) 54.dp else 48.dp).padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Txt(label, if (big) IosType.title3.copy(fontWeight = FontWeight.Normal) else IosType.body)
                    if (subtitle != null) Txt(subtitle, IosType.subhead, color = c.secondaryLabel)
                }
                Ico(Icons.ChevronRight, c.tertiaryLabel, size = 22.dp)
            }
            Hairline(Modifier.align(Alignment.BottomStart))
        }
    }
}

@Composable
fun PermissionCard() {
    val a = LocalActions.current
    val c = LocalIos.current
    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(c.fill)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Ico(Icons.Note, c.accent, size = 40.dp)
        Spacer(Modifier.height(8.dp))
        Txt("Izinkan Akses Musik", IosType.headline)
        Txt(
            "AudioPlayer butuh izin untuk membaca lagu di perangkat kamu. Atau pilih folder tertentu saja.",
            IosType.subhead, Modifier.padding(top = 4.dp, bottom = 14.dp),
            color = c.secondaryLabel, maxLines = 3,
            align = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PillButton("Izinkan", Icons.Check, a.requestAudioPermission, Modifier.weight(1f), filled = true)
            PillButton("Folder", Icons.Folder, a.pickFolder, Modifier.weight(1f))
        }
    }
}

/** Warna dominan cover, dianimasikan. Dipakai untuk mewarnai halaman album/artis (gaya iOS 27). */
@Composable
fun rememberArtTint(song: Song?, fallback: Color): Color {
    val context = LocalContext.current
    val color by produceState<Color?>(null, song?.artKey) {
        value = song?.let { ArtworkLoader.dominantColor(context, it) }?.let { Color(it) }
    }
    val animated by animateColorAsState(color ?: fallback, tween(500), label = "tint")
    return animated
}
