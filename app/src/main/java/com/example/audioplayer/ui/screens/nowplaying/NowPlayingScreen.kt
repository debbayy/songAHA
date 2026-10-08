package com.example.audioplayer.ui.screens.nowplaying

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.ArtworkLoader
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.NowPlayingPanel
import kotlinx.coroutines.launch

@Composable
fun NowPlayingScreen() {
    val a = LocalActions.current
    val vm = a.vm
    val nav = a.nav
    val p by vm.player.collectAsState()
    val song = p.current ?: return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dismissPx = with(LocalDensity.current) { 140.dp.toPx() }
    val offset = remember { Animatable(0f) }
    var volumeOpen by remember { mutableStateOf(false) }

    // Latar: warna dominan cover + cover 8x8 yang di-stretch (blur murah, aman untuk Android 8)
    val tintRaw by produceState<Color?>(null, song.artKey) {
        value = ArtworkLoader.dominantColor(context, song)?.let { Color(it) }
    }
    val tint by animateColorAsState(tintRaw ?: Color(0xFF48484A), tween(600), label = "np")
    val tiny by produceState<Bitmap?>(null, song.artKey) { value = ArtworkLoader.tiny(context, song) }

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { translationY = offset.value }
            .clip(RoundedCornerShape(if (offset.value > 0f) 28.dp else 0.dp))
            .background(lerp(tint, Color.Black, 0.35f))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (offset.value > dismissPx) nav.nowPlayingOpen = false
                        else scope.launch { offset.animateTo(0f, spring(dampingRatio = 0.8f)) }
                    },
                    onDragCancel = { scope.launch { offset.animateTo(0f) } },
                ) { change, dy ->
                    change.consume()
                    scope.launch { offset.snapTo((offset.value + dy).coerceAtLeast(0f)) }
                }
            }
    ) {
        tiny?.let { bmp ->
            val image = remember(bmp) { bmp.asImageBitmap() }
            Image(
                image, null, Modifier.fillMaxSize().graphicsLayer { alpha = 0.75f },
                contentScale = ContentScale.Crop, filterQuality = FilterQuality.High,
            )
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x1A000000), Color(0x99000000)))))

        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 26.dp)
        ) {
            NowPlayingTopBar(volumeOpen) { volumeOpen = !volumeOpen }

            AnimatedContent(
                nav.panel, Modifier.weight(1f),
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                label = "panel",
            ) { panel ->
                when (panel) {
                    NowPlayingPanel.Artwork -> ArtworkPanel(p, vm, song)
                    NowPlayingPanel.Lyrics -> LyricsPanel(p, song)
                    NowPlayingPanel.Queue -> QueuePanel(p, vm, song)
                }
            }

            Scrubber(vm, p.durationMs)
            PlaybackControls(p, vm)
            BottomRow()
        }
        VolumePanel(volumeOpen) { volumeOpen = false }
    }
}
