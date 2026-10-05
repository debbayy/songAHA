package com.example.audioplayer.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.ArtworkLoader
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos
import java.util.Locale

// ---- Teks & interaksi --------------------------------------------------------------------

@Composable
fun Txt(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalIos.current.label,
    maxLines: Int = 1,
    align: TextAlign = TextAlign.Start,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(color = color, textAlign = align),
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines,
    )
}

/** Klik ala iOS: baris jadi sedikit abu-abu saat ditekan, tanpa ripple. */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.pressable(
    highlight: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val c = LocalIos.current
    this
        .then(if (highlight && pressed) Modifier.background(c.pressed) else Modifier)
        .combinedClickable(
            interactionSource = source,
            indication = null,
            onLongClick = onLongClick?.let { long ->
                {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    long()
                }
            },
            onClick = onClick,
        )
}

/** Tombol "membal": mengecil sedikit saat ditekan, seperti kontrol iOS 26/27. */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.bounce(onLongClick: (() -> Unit)? = null, onClick: () -> Unit): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        if (pressed) 0.92f else 1f,
        spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "bounce",
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            alpha = if (pressed) 0.8f else 1f
        }
        .combinedClickable(
            interactionSource = source,
            indication = null,
            onLongClick = onLongClick?.let { long ->
                {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    long()
                }
            },
            onClick = onClick,
        )
}

fun Modifier.noRippleClick(onClick: () -> Unit): Modifier = composed {
    clickable(remember { MutableInteractionSource() }, indication = null, onClick = onClick)
}

// ---- Material kaca -----------------------------------------------------------------------

/**
 * Tiruan Liquid Glass yang jalan di Android 8: isian tembus pandang + kilau di atas
 * + garis tepi bergradasi. Tidak memakai blur real-time (tidak tersedia sebelum Android 12).
 */
fun Modifier.glass(shape: Shape, elevation: Dp = 8.dp): Modifier = composed {
    val c = LocalIos.current
    this
        .shadow(elevation, shape, clip = false, ambientColor = Color(0x22000000), spotColor = Color(0x33000000))
        .clip(shape)
        .background(c.glassFill)
        .background(Brush.verticalGradient(listOf(c.glassSheen, Color.Transparent)))
        .border(0.8.dp, Brush.verticalGradient(listOf(c.glassEdgeTop, c.glassEdgeBottom)), shape)
}

@Composable
fun GlassIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tint: Color = LocalIos.current.label,
) {
    Box(modifier.size(size).glass(CircleShape, 4.dp).bounce(onClick = onClick), Alignment.Center) {
        Ico(icon, tint, size = size * 0.5f)
    }
}

@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(0.5.dp).background(LocalIos.current.separator))
}

// ---- Tombol ------------------------------------------------------------------------------

@Composable
fun PillButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
) {
    val c = LocalIos.current
    val fg = if (filled) Color.White else c.accent
    Row(
        modifier
            .height(48.dp)
            .clip(CircleShape)
            .background(if (filled) c.accent else c.fill)
            .bounce(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Ico(icon, fg, size = 20.dp)
        Spacer(Modifier.width(6.dp))
        Txt(label, IosType.headline, color = fg)
    }
}

@Composable
fun PlayShuffleButtons(onPlay: () -> Unit, onShuffle: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PillButton("Putar", Icons.Play, onPlay, Modifier.weight(1f))
        PillButton("Acak", Icons.Shuffle, onShuffle, Modifier.weight(1f))
    }
}

/** Segmented control iOS. */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalIos.current
    Row(
        modifier.height(32.dp).clip(RoundedCornerShape(9.dp)).background(c.fill).padding(2.dp)
    ) {
        options.forEachIndexed { i, label ->
            val sel = i == selected
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(
                        if (sel) Modifier
                            .shadow(2.dp, RoundedCornerShape(7.dp))
                            .background(if (c.isDark) Color(0xFF636366) else Color.White, RoundedCornerShape(7.dp))
                        else Modifier
                    )
                    .noRippleClick { onSelect(i) },
                Alignment.Center,
            ) {
                Txt(
                    label,
                    IosType.footnote.copy(fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Medium),
                )
            }
        }
    }
}

// ---- Slider ala iOS ----------------------------------------------------------------------

/** Slider tanpa knob yang menebal saat disentuh (scrubber & volume di Now Playing iOS). */
@Composable
fun IosSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: () -> Unit = {},
    active: Color,
    inactive: Color,
) {
    val change by rememberUpdatedState(onValueChange)
    val finished by rememberUpdatedState(onValueChangeFinished)
    var dragging by remember { mutableStateOf(false) }
    val thickness by animateDpAsState(if (dragging) 12.dp else 7.dp, tween(150), label = "track")
    val scaleX by animateFloatAsState(if (dragging) 1.03f else 1f, tween(150), label = "grow")

    Box(
        modifier
            .height(32.dp)
            .graphicsLayer { this.scaleX = scaleX }
            .pointerInput(Unit) {
                fun frac(x: Float) = (x / size.width).coerceIn(0f, 1f)
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    dragging = true
                    change(frac(down.position.x))
                    while (true) {
                        val event = awaitPointerEvent()
                        val c = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!c.pressed) break
                        change(frac(c.position.x))
                        c.consume()
                    }
                    dragging = false
                    finished()
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.fillMaxWidth().height(thickness).clip(CircleShape).background(inactive)) {
            Box(Modifier.fillMaxWidth(value.coerceIn(0f, 1f)).fillMaxHeight().background(active))
        }
    }
}

// ---- Cover album -------------------------------------------------------------------------

@Composable
fun Artwork(
    song: Song?,
    sizeHint: Dp,
    modifier: Modifier = Modifier,
    corner: Dp = 6.dp,
    iconScale: Float = 0.42f,
) {
    val c = LocalIos.current
    val context = LocalContext.current
    val px = with(LocalDensity.current) { sizeHint.roundToPx() }
    val bitmap by produceState<Bitmap?>(
        initialValue = song?.let { ArtworkLoader.cached(it, px) },
        key1 = song?.artKey,
        key2 = px,
    ) {
        if (value == null && song != null) value = ArtworkLoader.load(context, song, px)
    }
    Box(
        modifier
            .clip(RoundedCornerShape(corner))
            .background(Brush.verticalGradient(listOf(c.placeholderTop, c.placeholderBottom))),
        contentAlignment = Alignment.Center,
    ) {
        val bmp = bitmap
        if (bmp != null) {
            val image = remember(bmp) { bmp.asImageBitmap() }
            Image(image, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Ico(Icons.Note, c.secondaryLabel.copy(alpha = 0.5f), Modifier.fillMaxSize(iconScale), size = sizeHint * iconScale)
        }
    }
}

// ---- Baris lagu --------------------------------------------------------------------------

@Composable
fun SongRow(
    song: Song,
    subtitle: String,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMore: () -> Unit,
    number: Int? = null,
    separator: Boolean = true,
) {
    val c = LocalIos.current
    Row(
        Modifier.fillMaxWidth().pressable(onLongClick = onMore, onClick = onClick).padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (number != null) {
            Box(Modifier.width(26.dp), Alignment.CenterStart) {
                if (isCurrent) EqualizerBars(isPlaying, c.accent, Modifier.size(14.dp))
                else Txt("$number", IosType.body, color = c.secondaryLabel)
            }
        } else {
            Box(Modifier.size(48.dp)) {
                Artwork(song, 48.dp, Modifier.fillMaxSize())
                if (isCurrent) {
                    Box(
                        Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp)).background(Color(0x66000000)),
                        Alignment.Center,
                    ) { EqualizerBars(isPlaying, Color.White, Modifier.size(16.dp)) }
                }
            }
        }
        Box(Modifier.weight(1f).padding(start = 12.dp)) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = if (number != null) 50.dp else 64.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                    Txt(song.title, IosType.body, color = if (isCurrent) c.accent else c.label)
                    if (subtitle.isNotBlank()) Txt(subtitle, IosType.subhead, color = c.secondaryLabel)
                }
                Box(Modifier.size(44.dp).pressable(highlight = false, onClick = onMore), Alignment.Center) {
                    Ico(Icons.More, c.label, size = 20.dp)
                }
            }
            if (separator) Hairline(Modifier.align(Alignment.BottomStart))
        }
    }
}

/** Ikon equalizer kecil untuk lagu yang sedang diputar. Diam (tanpa animasi) saat pause. */
@Composable
fun EqualizerBars(playing: Boolean, color: Color, modifier: Modifier = Modifier) {
    if (playing) {
        val t = rememberInfiniteTransition(label = "eq")
        val a by t.animateFloat(0.25f, 1f, infiniteRepeatable(tween(420), RepeatMode.Reverse), label = "a")
        val b by t.animateFloat(1f, 0.3f, infiniteRepeatable(tween(560), RepeatMode.Reverse), label = "b")
        val d by t.animateFloat(0.4f, 0.9f, infiniteRepeatable(tween(360), RepeatMode.Reverse), label = "d")
        Bars(listOf(a, b, d), color, modifier)
    } else {
        Bars(listOf(0.35f, 0.6f, 0.45f), color, modifier)
    }
}

@Composable
private fun Bars(levels: List<Float>, color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width / 5f
        levels.forEachIndexed { i, v ->
            val h = size.height * v
            drawRoundRect(color, Offset(i * 2 * w, size.height - h), Size(w, h), CornerRadius(w / 2))
        }
    }
}

// ---- Lain-lain ---------------------------------------------------------------------------

@Composable
fun SectionHeader(title: String, onMore: (() -> Unit)? = null) {
    val c = LocalIos.current
    Row(
        Modifier
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 10.dp)
            .then(if (onMore != null) Modifier.noRippleClick(onMore) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Txt(title, IosType.title2)
        if (onMore != null) Ico(Icons.ChevronRight, c.secondaryLabel, size = 26.dp)
    }
}

@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier) {
    val c = LocalIos.current
    Column(
        modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Ico(Icons.Note, c.tertiaryLabel, size = 56.dp)
        Spacer(Modifier.height(12.dp))
        Txt(title, IosType.title3, align = TextAlign.Center, maxLines = 2)
        Spacer(Modifier.height(4.dp))
        Txt(message, IosType.subhead, color = c.secondaryLabel, align = TextAlign.Center, maxLines = 4)
    }
}

fun formatTime(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    val h = total / 3600
    return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, (total % 3600) / 60, total % 60)
    else String.format(Locale.US, "%d:%02d", total / 60, total % 60)
}

fun songCountLabel(songs: List<Song>): String {
    val minutes = songs.sumOf { it.durationMs } / 60_000
    return if (minutes > 0) "${songs.size} lagu, $minutes menit" else "${songs.size} lagu"
}
