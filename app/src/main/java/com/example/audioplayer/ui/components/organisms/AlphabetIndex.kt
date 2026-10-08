package com.example.audioplayer.ui.components.organisms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.glass
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.Normalizer

/** Huruf yang ditampilkan di indeks, seperti iOS: A–Z lalu # untuk angka & simbol. */
private val LETTERS: List<Char> = ('A'..'Z') + '#'

/** Indeks hilang sendiri setelah daftar berhenti digulir selama ini. */
private const val HIDE_AFTER_MS = 1_500L

/**
 * Huruf awal setiap bagian → indeks item pertamanya di LazyColumn.
 * @param firstIndex indeks item pertama daftar (setelah header/tombol di atasnya).
 * @param perItem    berapa data per item LazyColumn (2 untuk grid album dua kolom).
 */
fun <T> alphabetSections(items: List<T>, firstIndex: Int, perItem: Int = 1, label: (T) -> String): Map<Char, Int> {
    val sections = LinkedHashMap<Char, Int>()
    items.forEachIndexed { i, item -> sections.getOrPut(letterOf(label(item))) { firstIndex + i / perItem } }
    return sections
}

/** "Éclair" → 'E', "99 Luftballons" → '#'. */
private fun letterOf(text: String): Char {
    val first = Normalizer.normalize(text.trim(), Normalizer.Form.NFD).firstOrNull()?.uppercaseChar() ?: return '#'
    return if (first in 'A'..'Z') first else '#'
}

/**
 * Indeks abjad di tepi kanan daftar (seperti aplikasi Musik & Kontak iOS).
 * Muncul saat daftar digulir; sentuh atau geser hurufnya untuk lompat ke bagian itu.
 * Letakkan di dalam Box yang sama dengan LazyColumn-nya (lihat parameter `overlay` di Page).
 */
@Composable
fun BoxScope.AlphabetIndex(listState: LazyListState, sections: Map<Char, Int>) {
    if (sections.size < 2) return
    val c = LocalIos.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var touchedLetter by remember { mutableStateOf<Char?>(null) }
    var shown by remember { mutableStateOf(false) }
    val active = listState.isScrollInProgress || touchedLetter != null
    LaunchedEffect(active) {
        if (active) shown = true
        else {
            delay(HIDE_AFTER_MS)
            shown = false
        }
    }
    val alpha by animateFloatAsState(if (shown) 1f else 0f, tween(250), label = "index")

    // Huruf bagian yang sedang terlihat di atas layar
    val currentLetter by remember(sections) {
        derivedStateOf { sections.entries.lastOrNull { it.value <= listState.firstVisibleItemIndex }?.key }
    }

    fun jumpTo(letter: Char) {
        if (letter == touchedLetter) return
        touchedLetter = letter
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        // huruf tanpa isi diarahkan ke bagian berikutnya yang ada (atau terakhir)
        val target = LETTERS.drop(LETTERS.indexOf(letter)).firstNotNullOfOrNull { sections[it] }
            ?: sections.values.last()
        scope.launch { listState.scrollToItem(target) }
    }

    Column(
        Modifier
            .align(Alignment.CenterEnd)
            .padding(top = 110.dp, bottom = 170.dp, end = 2.dp)
            .heightIn(max = 520.dp)
            .fillMaxHeight()
            .width(22.dp)
            .graphicsLayer { this.alpha = alpha }
            .clip(CircleShape)
            .background(if (touchedLetter != null) c.fill else Color.Transparent)
            // hanya menangkap sentuhan saat terlihat, supaya tombol "…" di tepi kanan baris
            // tetap bisa ditekan ketika indeks tersembunyi
            .then(if (!shown) Modifier else Modifier.pointerInput(sections) {
                fun letterAt(y: Float) = LETTERS[(y / size.height * LETTERS.size).toInt().coerceIn(LETTERS.indices)]
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    jumpTo(letterAt(down.position.y))
                    while (true) {
                        val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        change.consume()
                        jumpTo(letterAt(change.position.y))
                    }
                    touchedLetter = null
                }
            }),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LETTERS.forEach { letter ->
            val highlighted = letter == (touchedLetter ?: currentLetter)
            Txt(
                letter.toString(),
                IosType.caption2.copy(fontWeight = if (highlighted) FontWeight.Bold else FontWeight.SemiBold),
                color = if (highlighted) c.accent else c.secondaryLabel,
                align = TextAlign.Center,
            )
        }
    }

    // Gelembung huruf besar di tengah layar selama indeks disentuh
    AnimatedVisibility(
        touchedLetter != null,
        Modifier.align(Alignment.Center),
        enter = fadeIn(tween(120)) + scaleIn(initialScale = 0.8f),
        exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.9f),
    ) {
        var lastLetter by remember { mutableStateOf('A') }
        touchedLetter?.let { lastLetter = it }
        Box(Modifier.size(84.dp).glass(RoundedCornerShape(22.dp)), Alignment.Center) {
            Txt(lastLetter.toString(), IosType.largeTitle, color = c.accent)
        }
    }
}
