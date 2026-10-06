package com.example.audioplayer.ui.components.organisms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.atoms.Artwork
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.bounce
import com.example.audioplayer.ui.components.foundation.glass
import com.example.audioplayer.ui.components.foundation.noRippleClick
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun MiniPlayer(onOpen: () -> Unit) {
    val a = LocalActions.current
    val c = LocalIos.current
    val p by a.vm.player.collectAsState()
    val song = p.current ?: return
    Row(
        Modifier.fillMaxWidth().height(58.dp).glass(CircleShape).noRippleClick(onOpen).padding(start = 8.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Artwork(song, 42.dp, Modifier.size(42.dp), corner = 21.dp)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Txt(song.title, IosType.subhead.copy(fontWeight = FontWeight.SemiBold))
            Txt(song.displayArtist, IosType.footnote, color = c.secondaryLabel)
        }
        Box(Modifier.size(44.dp).bounce { a.vm.togglePlay() }, Alignment.Center) {
            Ico(if (p.isPlaying) Icons.Pause else Icons.Play, c.label, size = 26.dp)
        }
        Spacer(Modifier.width(2.dp))
        Box(Modifier.size(44.dp).bounce { a.vm.next() }, Alignment.Center) {
            Ico(Icons.Next, c.label, size = 26.dp)
        }
    }
}
