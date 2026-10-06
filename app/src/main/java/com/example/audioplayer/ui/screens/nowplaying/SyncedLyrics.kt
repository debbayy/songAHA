package com.example.audioplayer.ui.screens.nowplaying

import android.os.SystemClock
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.lyrics.Lyrics
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.foundation.fadingEdges

/** Jeda auto-scroll setelah user menggeser lirik sendiri, seperti Apple Music. */
private const val MANUAL_SCROLL_PAUSE_MS = 3_000L

/**
 * Lirik tersinkron: baris yang sedang dinyanyikan menyala dan otomatis digulir ke atas.
 * Ketuk sebuah baris untuk lompat ke bagian lagu itu.
 */
@Composable
internal fun SyncedLyrics(lyrics: Lyrics, isPlaying: Boolean, modifier: Modifier = Modifier) {
    val vm = LocalActions.current.vm
    val position by rememberPlaybackPosition(vm, isPlaying)
    val current by remember(lyrics) { derivedStateOf { lyrics.indexAt(position) } }
    val listState = rememberLazyListState()
    var autoScrollAfter by remember { mutableLongStateOf(0L) }

    // Saat user menggeser daftar, auto-scroll berhenti sebentar
    LaunchedEffect(listState) {
        listState.interactionSource.interactions.collect { interaction ->
            autoScrollAfter = when (interaction) {
                is DragInteraction.Start -> Long.MAX_VALUE
                is DragInteraction.Stop, is DragInteraction.Cancel -> SystemClock.uptimeMillis() + MANUAL_SCROLL_PAUSE_MS
                else -> autoScrollAfter
            }
        }
    }
    LaunchedEffect(current) {
        if (current >= 0 && SystemClock.uptimeMillis() >= autoScrollAfter) listState.animateScrollToItem(current)
    }

    LazyColumn(modifier.fadingEdges(top = 48.dp, bottom = 96.dp), state = listState, contentPadding = PaddingValues(top = 72.dp, bottom = 320.dp)) {
        itemsIndexed(lyrics.lines) { index, line ->
            LyricLineText(
                text = line.text,
                state = when {
                    index == current -> LyricLineState.Active
                    index < current -> LyricLineState.Sung
                    else -> LyricLineState.Upcoming
                },
                onClick = { vm.seekTo(line.timeMs) },
            )
        }
    }
}
