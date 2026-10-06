package com.example.audioplayer.ui.components.organisms

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.NavViewModel
import com.example.audioplayer.ui.Tab
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.bounce
import com.example.audioplayer.ui.components.foundation.glass
import com.example.audioplayer.ui.components.foundation.noRippleClick
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun TabBar(nav: NavViewModel) {
    val c = LocalIos.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.weight(1f).height(62.dp).glass(CircleShape).padding(4.dp)) {
            listOf(Tab.Home, Tab.Library, Tab.Playlists).forEach { t ->
                val selected = nav.tab == t
                val bg by animateColorAsState(if (selected) c.fill else Color.Transparent, tween(200), label = "tab")
                val fg = if (selected) c.accent else c.label
                Column(
                    Modifier.weight(1f).fillMaxHeight().clip(CircleShape).background(bg).noRippleClick { nav.select(t) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Ico(t.icon, fg, size = 24.dp)
                    Txt(t.label, IosType.caption2, color = fg)
                }
            }
        }
        Box(Modifier.size(62.dp).glass(CircleShape).bounce { nav.select(Tab.Search) }, Alignment.Center) {
            Ico(Icons.Search, c.label, size = 26.dp)
        }
    }
}
