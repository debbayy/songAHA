package com.example.audioplayer.ui.components.organisms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.components.atoms.Artwork
import com.example.audioplayer.ui.components.atoms.Hairline
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.noRippleClick
import com.example.audioplayer.ui.components.foundation.pressable
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

data class SheetAction(
    val label: String,
    val icon: ImageVector? = null,
    val destructive: Boolean = false,
    val onClick: () -> Unit,
)

data class SheetSpec(
    val actions: List<SheetAction>,
    val song: Song? = null,
    val title: String? = null,
    val subtitle: String? = null,
)

/** Action sheet / context menu ala iOS yang muncul dari bawah. */
@Composable
fun BoxScope.ActionSheetHost(spec: SheetSpec?, onDismiss: () -> Unit) {
    val c = LocalIos.current
    var last by remember { mutableStateOf(spec) }
    if (spec != null) last = spec

    AnimatedVisibility(spec != null, Modifier.matchParentSize(), enter = fadeIn(tween(200)), exit = fadeOut(tween(200))) {
        Box(Modifier.fillMaxSize().background(Color(0x66000000)).noRippleClick(onDismiss))
    }
    AnimatedVisibility(
        spec != null,
        Modifier.align(Alignment.BottomCenter),
        enter = slideInVertically(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow, visibilityThreshold = IntOffset.VisibilityThreshold)) { it },
        exit = slideOutVertically(tween(220)) { it },
    ) {
        val s = last ?: return@AnimatedVisibility
        Column(Modifier.navigationBarsPadding().padding(8.dp)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(c.elevated)
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (s.song != null || s.title != null) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (s.song != null) {
                            Artwork(s.song, 48.dp, Modifier.size(48.dp))
                            Spacer(Modifier.width(12.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Txt(s.title ?: s.song!!.title, IosType.headline)
                            val sub = s.subtitle ?: s.song?.let { song ->
                                listOf(song.displayArtist, song.album).filter { it.isNotBlank() }.joinToString(" · ")
                            }
                            if (!sub.isNullOrBlank()) Txt(sub, IosType.subhead, color = c.secondaryLabel)
                        }
                    }
                    Hairline()
                }
                s.actions.forEachIndexed { i, a ->
                    val color = if (a.destructive) c.red else c.label
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .pressable {
                                onDismiss()
                                a.onClick()
                            }
                            .padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Txt(a.label, IosType.body, Modifier.weight(1f), color = color)
                        a.icon?.let { Ico(it, color, size = 22.dp) }
                    }
                    if (i < s.actions.lastIndex) Hairline()
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(c.elevated)
                    .pressable(onClick = onDismiss),
                Alignment.Center,
            ) { Txt("Batal", IosType.headline, color = c.accent) }
        }
    }
}
