package com.example.audioplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos
import kotlinx.coroutines.delay

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

data class AlertSpec(
    val title: String,
    val message: String? = null,
    val input: Boolean = true,
    val initial: String = "",
    val placeholder: String = "",
    val confirm: String = "OK",
    val destructive: Boolean = false,
    val onConfirm: (String) -> Unit,
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
                                listOf(song.displayArtist, song.album).filter { it.isNotBlank() }.joinToString(" Â· ")
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

/** Alert iOS (opsional dengan kolom teks), dipakai untuk buat/ubah nama playlist & konfirmasi. */
@Composable
fun AlertHost(spec: AlertSpec?, onDismiss: () -> Unit) {
    if (spec == null) return
    val c = LocalIos.current
    Dialog(onDismissRequest = onDismiss) {
        var text by remember(spec) { mutableStateOf(TextFieldValue(spec.initial, TextRange(spec.initial.length))) }
        val focus = remember { FocusRequester() }
        val canConfirm = !spec.input || text.text.isNotBlank()
        Column(Modifier.width(280.dp).clip(RoundedCornerShape(16.dp)).background(c.elevated)) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Txt(spec.title, IosType.headline, align = TextAlign.Center, maxLines = 2)
                spec.message?.let {
                    Txt(it, IosType.footnote, Modifier.padding(top = 4.dp), align = TextAlign.Center, maxLines = 5)
                }
                if (spec.input) {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        singleLine = true,
                        textStyle = IosType.subhead.copy(color = c.label),
                        cursorBrush = SolidColor(c.accent),
                        modifier = Modifier
                            .padding(top = 14.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(c.fill)
                            .padding(horizontal = 10.dp, vertical = 9.dp)
                            .focusRequester(focus),
                        decorationBox = { inner ->
                            Box {
                                if (text.text.isEmpty()) Txt(spec.placeholder, IosType.subhead, color = c.tertiaryLabel)
                                inner()
                            }
                        },
                    )
                    LaunchedEffect(spec) {
                        delay(150)
                        focus.requestFocus()
                    }
                }
            }
            Hairline()
            Row(Modifier.height(46.dp)) {
                Box(Modifier.weight(1f).fillMaxHeight().pressable(onClick = onDismiss), Alignment.Center) {
                    Txt("Batal", IosType.body, color = c.accent)
                }
                Box(Modifier.width(0.5.dp).fillMaxHeight().background(c.separator))
                Box(
                    Modifier.weight(1f).fillMaxHeight().pressable {
                        if (canConfirm) {
                            onDismiss()
                            spec.onConfirm(text.text.trim())
                        }
                    },
                    Alignment.Center,
                ) {
                    Txt(
                        spec.confirm, IosType.headline,
                        color = when {
                            !canConfirm -> c.tertiaryLabel
                            spec.destructive -> c.red
                            else -> c.accent
                        },
                    )
                }
            }
        }
    }
}

/** HUD konfirmasi singkat di tengah layar (mis. "Ditambahkan ke Playlist"). */
@Composable
fun BoxScope.ToastHost(message: String?, onDone: () -> Unit) {
    val c = LocalIos.current
    var last by remember { mutableStateOf(message) }
    if (message != null) last = message
    LaunchedEffect(message) {
        if (message != null) {
            delay(1400)
            onDone()
        }
    }
    AnimatedVisibility(
        message != null,
        Modifier.align(Alignment.Center),
        enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.85f),
        exit = fadeOut(tween(250)) + scaleOut(targetScale = 0.9f),
    ) {
        Column(
            Modifier.width(170.dp).glass(RoundedCornerShape(22.dp)).padding(vertical = 22.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Ico(Icons.Check, c.label, size = 40.dp)
            Spacer(Modifier.height(8.dp))
            Txt(last.orEmpty(), IosType.subhead, align = TextAlign.Center, maxLines = 3)
        }
    }
}
