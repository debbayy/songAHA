package com.example.audioplayer.ui.screens

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.example.audioplayer.data.ArtworkLoader
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.PlayerState
import com.example.audioplayer.ui.PlayerViewModel
import com.example.audioplayer.ui.components.Artwork
import com.example.audioplayer.ui.components.Ico
import com.example.audioplayer.ui.components.Icons
import com.example.audioplayer.ui.components.IosSlider
import com.example.audioplayer.ui.components.Txt
import com.example.audioplayer.ui.components.bounce
import com.example.audioplayer.ui.components.formatTime
import com.example.audioplayer.ui.components.noRippleClick
import com.example.audioplayer.ui.components.pressable
import com.example.audioplayer.ui.sleepTimerMenu
import com.example.audioplayer.ui.songMenu
import com.example.audioplayer.ui.theme.IosType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val White60 = Color.White.copy(alpha = 0.6f)
private val White85 = Color.White.copy(alpha = 0.85f)
private val White25 = Color.White.copy(alpha = 0.25f)
private val White15 = Color.White.copy(alpha = 0.15f)

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
            // grabber
            Box(Modifier.fillMaxWidth().padding(vertical = 10.dp).noRippleClick { nav.nowPlayingOpen = false }, Alignment.Center) {
                Box(Modifier.size(38.dp, 5.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.45f)))
            }

            AnimatedContent(nav.queueOpen, Modifier.weight(1f), transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) }, label = "mode") { queue ->
                if (queue) QueuePanel(p, vm, song) else ArtworkPanel(p, vm, song)
            }

            Scrubber(vm, p.durationMs)
            Controls(p, vm)
            VolumeRow(vm)
            BottomRow()
        }
    }
}

@Composable
private fun ArtworkPanel(p: PlayerState, vm: PlayerViewModel, song: Song) {
    val a = LocalActions.current
    val favs by vm.store.favorites.collectAsState()
    Column {
        Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
            // Cover mengecil saat pause, khas Apple Music
            val scale by animateFloatAsState(if (p.isPlaying) 1f else 0.82f, spring(dampingRatio = 0.62f, stiffness = 260f), label = "art")
            Artwork(
                song, 380.dp,
                Modifier
                    .aspectRatio(1f)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        shadowElevation = 28.dp.toPx() * scale
                        shape = RoundedCornerShape(14.dp)
                        clip = true
                    },
                corner = 14.dp, iconScale = 0.3f,
            )
        }
        Row(Modifier.padding(top = 18.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Txt(song.title, IosType.title3, color = Color.White)
                Txt(song.displayArtist, IosType.title3.copy(fontWeight = FontWeight.Normal), color = White60)
            }
            val fav = song.id in favs
            RoundButton(if (fav) Icons.HeartFill else Icons.Heart, { vm.store.toggleFavorite(song.id) }, tint = if (fav) Color.White else White85)
            Spacer(Modifier.width(10.dp))
            RoundButton(Icons.More, { a.songMenu(song) })
        }
    }
}

@Composable
private fun QueuePanel(p: PlayerState, vm: PlayerViewModel, song: Song) {
    val a = LocalActions.current
    Column {
        Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Artwork(song, 64.dp, Modifier.size(64.dp), corner = 8.dp)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Txt(song.title, IosType.headline, color = Color.White)
                Txt(song.displayArtist, IosType.subhead, color = White60)
            }
            RoundButton(Icons.More, { a.songMenu(song) })
        }
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

@Composable
private fun Scrubber(vm: PlayerViewModel, durationMs: Long) {
    val position by vm.position.collectAsState()
    var drag by remember { mutableStateOf<Float?>(null) }
    val fraction = drag ?: if (durationMs > 0) position.toFloat() / durationMs else 0f
    val shown = drag?.let { (it * durationMs).toLong() } ?: position
    Column(Modifier.padding(top = 10.dp)) {
        IosSlider(
            fraction.coerceIn(0f, 1f),
            onValueChange = { drag = it },
            onValueChangeFinished = {
                drag?.let { vm.seekTo((it * durationMs).toLong()) }
                drag = null
            },
            modifier = Modifier.fillMaxWidth(),
            active = if (drag != null) Color.White else White85,
            inactive = White25,
        )
        Row(Modifier.fillMaxWidth()) {
            Txt(formatTime(shown), IosType.caption, Modifier.weight(1f), color = White60)
            Txt("-" + formatTime(durationMs - shown), IosType.caption, color = White60)
        }
    }
}

@Composable
private fun Controls(p: PlayerState, vm: PlayerViewModel) {
    Row(
        Modifier.fillMaxWidth().height(104.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ControlButton(Icons.Previous, 42.dp) { vm.previous() }
        ControlButton(if (p.isPlaying) Icons.Pause else Icons.Play, 58.dp) { vm.togglePlay() }
        ControlButton(Icons.Next, 42.dp) { vm.next() }
    }
}

@Composable
private fun ControlButton(icon: androidx.compose.ui.graphics.vector.ImageVector, size: Dp, onClick: () -> Unit) {
    Box(Modifier.size(size + 30.dp).clip(CircleShape).bounce(onClick = onClick), Alignment.Center) {
        Ico(icon, Color.White, size = size)
    }
}

@Composable
private fun VolumeRow(vm: PlayerViewModel) {
    val volume by vm.volume.collectAsState()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Ico(Icons.VolumeLow, White60, size = 18.dp)
        IosSlider(
            volume, { vm.setVolume(it) }, Modifier.weight(1f).padding(horizontal = 10.dp),
            active = White85, inactive = White25,
        )
        Ico(Icons.VolumeHigh, White60, size = 20.dp)
    }
}

@Composable
private fun BottomRow() {
    val a = LocalActions.current
    val sleep by a.vm.sleep.collectAsState()
    // Hitung sisa menit timer, diperbarui tiap 10 detik
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(sleep) {
        while (sleep != null) {
            now = SystemClock.elapsedRealtime()
            delay(10_000)
        }
    }
    val sleepLabel = sleep?.let { t ->
        if (t.endOfTrack) "Akhir lagu" else "${((t.endsAt - now) / 60_000 + 1).coerceAtLeast(1)} mnt"
    }
    Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier
                .height(40.dp)
                .clip(CircleShape)
                .background(if (sleep != null) Color.White else Color.Transparent)
                .bounce { a.sleepTimerMenu() }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val fg = if (sleep != null) Color.Black else White85
            Ico(Icons.Moon, fg, size = 22.dp)
            if (sleepLabel != null) Txt(sleepLabel, IosType.footnote.copy(fontWeight = FontWeight.SemiBold), Modifier.padding(start = 6.dp), color = fg)
        }
        Spacer(Modifier.weight(1f))
        val queueOn = a.nav.queueOpen
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(if (queueOn) Color.White else Color.Transparent)
                .bounce { a.nav.queueOpen = !a.nav.queueOpen },
            Alignment.Center,
        ) { Ico(Icons.Queue, if (queueOn) Color.Black else White85, size = 24.dp) }
    }
}

@Composable
private fun RoundButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, tint: Color = White85) {
    Box(Modifier.size(36.dp).clip(CircleShape).background(White15).bounce(onClick = onClick), Alignment.Center) {
        Ico(icon, tint, size = 20.dp)
    }
}

@Composable
private fun ToggleChip(icon: androidx.compose.ui.graphics.vector.ImageVector, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(width = 52.dp, height = 34.dp)
            .clip(CircleShape)
            .background(if (active) Color.White else White15)
            .bounce(onClick = onClick),
        Alignment.Center,
    ) { Ico(icon, if (active) Color.Black else White85, size = 20.dp) }
}
