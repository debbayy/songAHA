package com.example.audioplayer.ui.screens.nowplaying

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import com.example.audioplayer.ui.PlayerViewModel
import kotlinx.coroutines.delay

/**
 * Posisi lagu yang diperbarui tiap 100 ms. ViewModel hanya melapor tiap 0,5 detik (cukup untuk
 * slider), jadi di antaranya posisi diperkirakan dari jam sistem supaya lirik pindah tepat waktu.
 */
@Composable
internal fun rememberPlaybackPosition(vm: PlayerViewModel, isPlaying: Boolean): State<Long> {
    val reported by vm.position.collectAsState()
    return produceState(reported, reported, isPlaying) {
        val reportedAt = SystemClock.elapsedRealtime()
        value = reported
        while (isPlaying) {
            delay(100)
            value = reported + (SystemClock.elapsedRealtime() - reportedAt)
        }
    }
}
