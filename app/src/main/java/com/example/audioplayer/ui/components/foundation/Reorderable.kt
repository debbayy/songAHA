package com.example.audioplayer.ui.components.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Mengurutkan ulang item LazyColumn dengan menahan lalu menggeser (seperti iOS).
 *
 * Selama digeser, urutan baru hanya disimpan di sini ([items]) supaya animasinya mulus; begitu
 * dilepas, satu perpindahan dikirim lewat `onMove(dari, ke)` ke pemilik datanya.
 *
 * Cara pakai:
 * ```
 * val reorder = rememberReorderState(lagu, key = { it.id }, listState) { from, to -> simpan(from, to) }
 * LazyColumn(Modifier.reorderable(reorder), state = listState) {
 *     items(reorder.items, key = { it.id }) { Row(Modifier.reorderItem(reorder, it.id)) }
 * }
 * ```
 */
@Stable
class ReorderState<T> internal constructor(
    private val listState: LazyListState,
    private val scope: CoroutineScope,
) {
    /**
     * Data asli. Harus berupa state: konten LazyColumn dibaca di luar composition, jadi tanpa
     * state perubahan daftar (mis. antrean maju ke lagu berikutnya) tidak akan terlihat.
     */
    internal var source by mutableStateOf<List<T>>(emptyList())
    internal var keyOf: (T) -> Any = { it as Any }
    internal var onMove: (from: Int, to: Int) -> Unit = { _, _ -> }
    internal var onDragStart: () -> Unit = {}
    internal var enabled = true
    internal var edgePx = 0f

    /** Urutan selama/sesudah digeser; null = ikuti [source]. */
    private var dragOrder by mutableStateOf<List<T>?>(null)
    /** Sumber saat geseran dimulai, untuk tahu kapan data baru sudah tersimpan. */
    private var sourceAtDragStart: List<T>? = null

    /** Urutan yang harus ditampilkan. */
    val items: List<T>
        get() = dragOrder?.takeIf { draggingKey != null || source === sourceAtDragStart } ?: source

    var draggingKey by mutableStateOf<Any?>(null)
        private set
    internal var dragOffset by mutableFloatStateOf(0f)
        private set

    private var startIndex = -1
    private var autoScroll: Job? = null

    internal fun start(y: Float) {
        if (!enabled) return
        val hit = visible().firstOrNull { y.toInt() in it.offset until it.offset + it.size } ?: return
        val index = source.indexOfFirst { keyOf(it) == hit.key }
        if (index < 0) return
        sourceAtDragStart = source
        dragOrder = source
        startIndex = index
        draggingKey = hit.key
        dragOffset = 0f
        onDragStart()
        autoScroll = scope.launch { autoScrollNearEdges() }
    }

    internal fun drag(dy: Float) {
        val key = draggingKey ?: return
        val order = dragOrder ?: return
        dragOffset += dy
        val current = visible().firstOrNull { it.key == key } ?: return
        val center = current.offset + dragOffset + current.size / 2f
        val target = visible().firstOrNull { info ->
            info.key != key && center.toInt() in info.offset until info.offset + info.size &&
                order.any { keyOf(it) == info.key }
        } ?: return

        val from = order.indexOfFirst { keyOf(it) == key }
        val to = order.indexOfFirst { keyOf(it) == target.key }
        dragOrder = order.toMutableList().apply { add(to, removeAt(from)) }
        // posisi slot berpindah, jadi geser balik supaya item tetap di bawah jari
        dragOffset += current.offset - target.offset
    }

    internal fun end() {
        val key = draggingKey ?: return
        val endIndex = dragOrder?.indexOfFirst { keyOf(it) == key } ?: -1
        draggingKey = null
        dragOffset = 0f
        autoScroll?.cancel()
        if (endIndex >= 0 && endIndex != startIndex) onMove(startIndex, endIndex)
        else dragOrder = null
    }

    /** Gulir otomatis saat item yang digeser mendekati tepi atas/bawah daftar. */
    private suspend fun autoScrollNearEdges() {
        while (draggingKey != null) {
            val info = visible().firstOrNull { it.key == draggingKey }
            if (info != null && edgePx > 0f) {
                val layout = listState.layoutInfo
                val top = info.offset + dragOffset - layout.viewportStartOffset
                val bottom = layout.viewportEndOffset - (info.offset + dragOffset + info.size)
                val speed = when {
                    top < edgePx -> -(1f - top / edgePx).coerceIn(0f, 1f) * MAX_SCROLL_PER_FRAME
                    bottom < edgePx -> (1f - bottom / edgePx).coerceIn(0f, 1f) * MAX_SCROLL_PER_FRAME
                    else -> 0f
                }
                if (speed != 0f) {
                    val scrolled = listState.scrollBy(speed)
                    dragOffset += scrolled
                    drag(0f)
                }
            }
            delay(16)
        }
    }

    private fun visible(): List<LazyListItemInfo> = listState.layoutInfo.visibleItemsInfo

    private companion object {
        const val MAX_SCROLL_PER_FRAME = 24f
    }
}

/**
 * @param items  data asli (mis. lagu di playlist); dipakai lagi setelah perubahan tersimpan.
 * @param key    kunci unik & stabil per item, sama dengan key di `items(...)` LazyColumn.
 * @param onMove dipanggil sekali saat item dilepas: pindahkan item di posisi `from` ke `to`.
 */
@Composable
fun <T> rememberReorderState(
    items: List<T>,
    key: (T) -> Any,
    listState: LazyListState,
    enabled: Boolean = true,
    onMove: (from: Int, to: Int) -> Unit,
): ReorderState<T> {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val edgePx = with(LocalDensity.current) { 72.dp.toPx() }
    val state = remember(listState) { ReorderState<T>(listState, scope) }
    state.source = items
    SideEffect {
        state.keyOf = key
        state.onMove = onMove
        state.enabled = enabled
        state.edgePx = edgePx
        state.onDragStart = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
    }
    return state
}

/**
 * Pasang di LazyColumn: tahan sebuah item lalu geser untuk memindahkannya.
 *
 * Setelah tahan-lama terdeteksi, gerakan jari dibaca di pass `Initial` (dari luar ke dalam) dan
 * langsung dikonsumsi. Tanpa itu, scroll milik LazyColumn yang berada lebih dalam akan
 * "mencuri" gerakan pertama dan daftar malah tergulir, bukan itemnya yang tergeser.
 */
fun Modifier.reorderable(state: ReorderState<*>): Modifier = pointerInput(state) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val longPress = awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
        state.start(longPress.position.y)
        if (state.draggingKey == null) return@awaitEachGesture
        longPress.consume()
        try {
            while (true) {
                val change = awaitPointerEvent(PointerEventPass.Initial).changes
                    .firstOrNull { it.id == longPress.id } ?: break
                if (!change.pressed) {
                    change.consume()
                    break
                }
                state.drag(change.positionChange().y)
                change.consume()
            }
        } finally {
            state.end()
        }
    }
}

/**
 * Pasang di setiap item: item yang sedang digeser mengikuti jari, sedikit membesar dan terangkat.
 * [background] mengisi item itu supaya tidak tembus ke baris di bawahnya.
 */
fun Modifier.reorderItem(state: ReorderState<*>, key: Any, background: Color): Modifier =
    if (state.draggingKey != key) this
    else this
        .zIndex(1f)
        .graphicsLayer {
            translationY = state.dragOffset
            scaleX = 1.03f
            scaleY = 1.03f
            shadowElevation = 16.dp.toPx()
            shape = RoundedCornerShape(12.dp)
            clip = true
        }
        .background(background)

/** Item dengan kunci yang tidak berubah saat urutannya dipindah-pindah. */
data class Keyed<T>(val key: String, val value: T)

/**
 * Beri kunci "id#ke-n" pada setiap item. Berbeda dengan indeks, kunci ini tetap menempel pada
 * itemnya setelah dipindah, dan tetap unik walau item yang sama muncul dua kali (mis. di playlist).
 */
fun <T> List<T>.withStableKeys(id: (T) -> Any): List<Keyed<T>> {
    val seen = HashMap<Any, Int>()
    return map { item ->
        val itemId = id(item)
        val occurrence = seen.merge(itemId, 1, Int::plus)!!
        Keyed("$itemId#$occurrence", item)
    }
}
