package com.example.audioplayer.ui.screens.nowplaying

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.PlayerState
import com.example.audioplayer.ui.PlayerViewModel
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.rememberReorderState
import com.example.audioplayer.ui.components.foundation.reorderItem
import com.example.audioplayer.ui.components.foundation.reorderable
import com.example.audioplayer.ui.components.foundation.withStableKeys
import com.example.audioplayer.ui.songMenu
import com.example.audioplayer.ui.theme.IosType

/** Warna baris antrean saat sedang digeser (di atas latar gelap Now Playing). */
private val DraggedRowColor = Color(0xFF3A3A3C)

@Composable
internal fun QueuePanel(p: PlayerState, vm: PlayerViewModel, song: Song) {
    val a = LocalActions.current
    val listState = rememberLazyListState()
    // Saat diacak, urutan tampil bukan urutan asli di pemutar, jadi geser-urut dimatikan
    val entries = remember(p.upNext) { p.upNext.withStableKeys { it.song.id } }
    val reorder = rememberReorderState(entries, key = { it.key }, listState, enabled = !p.shuffle) { from, to ->
        vm.moveQueueItem(entries[from].value.index, entries[to].value.index)
    }

    Column {
        NowPlayingHeader(song) { a.songMenu(song) }
        Row(Modifier.padding(top = 14.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Txt("Selanjutnya", IosType.headline, color = Color.White)
                Txt(
                    if (p.shuffle) "Diacak" else "Tahan lalu geser untuk mengubah urutan",
                    IosType.footnote, color = White60,
                )
            }
            ToggleChip(Icons.Shuffle, p.shuffle) { vm.toggleShuffle() }
            Spacer(Modifier.width(8.dp))
            ToggleChip(if (p.repeatMode == Player.REPEAT_MODE_ONE) Icons.RepeatOne else Icons.Repeat, p.repeatMode != Player.REPEAT_MODE_OFF) { vm.cycleRepeat() }
        }
        if (p.upNext.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                Txt("Tidak ada lagu berikutnya", IosType.subhead, color = White60)
            }
            return@Column
        }
        LazyColumn(Modifier.weight(1f).reorderable(reorder), state = listState) {
            items(reorder.items, key = { it.key }) { (key, entry) ->
                QueueRow(
                    entry.song,
                    onPlay = { vm.playQueueIndex(entry.index) },
                    onRemove = { vm.removeFromQueue(entry.index) },
                    modifier = Modifier.animateItem().reorderItem(reorder, key, DraggedRowColor),
                )
            }
        }
    }
}
