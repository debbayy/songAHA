package com.example.audioplayer.ui.screens.nowplaying

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import com.example.audioplayer.data.Song
import com.example.audioplayer.data.lyrics.Lyrics
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.PlayerState
import com.example.audioplayer.ui.lyricsMenu

/** Status pemuatan lirik lagu yang sedang diputar. */
private sealed interface LyricsState {
    data object Loading : LyricsState
    data object Missing : LyricsState
    data class Ready(val lyrics: Lyrics) : LyricsState
}

/** Panel lirik di Now Playing: tersinkron (LRC), teks biasa, atau ajakan impor file .lrc. */
@Composable
internal fun LyricsPanel(p: PlayerState, song: Song) {
    val a = LocalActions.current
    val revision by a.vm.lyrics.revision.collectAsState()
    val state by produceState<LyricsState>(LyricsState.Loading, song.id, revision) {
        value = a.vm.lyrics.load(song)?.let { LyricsState.Ready(it) } ?: LyricsState.Missing
    }

    Column {
        NowPlayingHeader(song) { a.lyricsMenu(song) }
        val body = Modifier.weight(1f).fillMaxWidth()
        when (val s = state) {
            LyricsState.Loading -> Box(body)
            LyricsState.Missing -> LyricsMissing(onImport = { a.pickLyrics(song) }, modifier = body)
            is LyricsState.Ready ->
                if (s.lyrics.synced) SyncedLyrics(s.lyrics, p.isPlaying, body)
                else PlainLyrics(s.lyrics, body)
        }
    }
}
