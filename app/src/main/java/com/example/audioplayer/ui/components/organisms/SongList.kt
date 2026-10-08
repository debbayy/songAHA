package com.example.audioplayer.ui.components.organisms

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Modifier
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.AppActions
import com.example.audioplayer.ui.PlayerState
import com.example.audioplayer.ui.components.foundation.ReorderState
import com.example.audioplayer.ui.components.foundation.reorderItem
import com.example.audioplayer.ui.components.molecules.SongRow
import com.example.audioplayer.ui.songMenu
import com.example.audioplayer.ui.theme.LocalIos

/**
 * Daftar lagu standar. Tap = putar daftar ini mulai dari lagu tsb; tahan / "…" = menu.
 *
 * Dengan [reorder], tahan lalu geser untuk mengubah urutan (menu tetap lewat tombol "…").
 * [key] harus sama dengan kunci yang dipakai [reorder].
 */
fun LazyListScope.songList(
    songs: List<Song>,
    player: PlayerState,
    actions: AppActions,
    numbered: Boolean = false,
    subtitle: (Song) -> String = { it.displayArtist },
    extra: (Song, Int) -> List<SheetAction> = { _, _ -> emptyList() },
    key: (index: Int, song: Song) -> Any = { i, s -> "${s.id}#$i" },
    reorder: ReorderState<*>? = null,
) {
    itemsIndexed(songs, key = key) { i, s ->
        val itemModifier = if (reorder == null) Modifier
        else Modifier.animateItem().reorderItem(reorder, key(i, s), LocalIos.current.elevated)
        SongRow(
            song = s,
            subtitle = subtitle(s),
            isCurrent = s.id == player.current?.id,
            isPlaying = player.isPlaying,
            number = if (numbered) (s.track % 1000).takeIf { it > 0 } ?: (i + 1) else null,
            onClick = { actions.vm.playSongs(songs, i) },
            onMore = { actions.songMenu(s, extra(s, i)) },
            menuOnLongPress = reorder == null,
            modifier = itemModifier,
        )
    }
}
