package com.example.audioplayer.ui.screens.nowplaying

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.PlayerState
import com.example.audioplayer.ui.PlayerViewModel
import com.example.audioplayer.ui.components.atoms.Artwork
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.pressable
import com.example.audioplayer.ui.songMenu
import com.example.audioplayer.ui.theme.IosType

@Composable
internal fun QueuePanel(p: PlayerState, vm: PlayerViewModel, song: Song) {
    val a = LocalActions.current
    Column {
        NowPlayingHeader(song) { a.songMenu(song) }
        Row(Modifier.padding(top = 14.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Txt("Selanjutnya", IosType.headline, color = Color.White)
                Txt(if (p.shuffle) "Diacak" else "Dari antrean", IosType.footnote, color = White60)
            }
            ToggleChip(Icons.Shuffle, p.shuffle) { vm.toggleShuffle() }
            Spacer(Modifier.width(8.dp))
            ToggleChip(if (p.repeatMode == Player.REPEAT_MODE_ONE) Icons.RepeatOne else Icons.Repeat, p.repeatMode != Player.REPEAT_MODE_OFF) { vm.cycleRepeat() }
        }
        if (p.upNext.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                Txt("Tidak ada lagu berikutnya", IosType.subhead, color = White60)
            }
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(p.upNext, key = { it.index }) { e ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).pressable { vm.playQueueIndex(e.index) }.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Artwork(e.song, 44.dp, Modifier.size(44.dp), corner = 6.dp)
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Txt(e.song.title, IosType.body, color = Color.White)
                            Txt(e.song.displayArtist, IosType.footnote, color = White60)
                        }
                        Box(Modifier.size(40.dp).pressable(highlight = false) { vm.removeFromQueue(e.index) }, Alignment.Center) {
                            Ico(Icons.CloseCircle, White60, size = 20.dp)
                        }
                    }
                }
            }
        }
    }
}
