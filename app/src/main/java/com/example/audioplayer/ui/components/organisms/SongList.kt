package com.example.audioplayer.ui.components.organisms

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.AppActions
import com.example.audioplayer.ui.PlayerState
import com.example.audioplayer.ui.components.molecules.SongRow
import com.example.audioplayer.ui.songMenu

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
