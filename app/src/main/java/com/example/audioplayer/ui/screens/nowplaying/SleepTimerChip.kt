package com.example.audioplayer.ui.screens.nowplaying

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.bounce
import com.example.audioplayer.ui.sleepTimerMenu
import com.example.audioplayer.ui.theme.IosType
import kotlinx.coroutines.delay

/** Tombol timer tidur. Saat aktif berubah putih dan menampilkan sisa waktunya. */
@Composable
internal fun SleepTimerChip() {
    val a = LocalActions.current
    val sleep by a.vm.sleep.collectAsState()

    // Sisa menit diperbarui tiap 10 detik
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(sleep) {
        while (sleep != null) {
            now = SystemClock.elapsedRealtime()
            delay(10_000)
        }
    }
    val label = sleep?.let { t ->
        if (t.endOfTrack) "Akhir lagu" else "${((t.endsAt - now) / 60_000 + 1).coerceAtLeast(1)} mnt"
    }
    val fg = if (sleep != null) Color.Black else White85

    Row(
        Modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(if (sleep != null) Color.White else Color.Transparent)
            .bounce { a.sleepTimerMenu() }
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Ico(Icons.Moon, fg, size = 22.dp)
        if (label != null) Txt(label, IosType.footnote.copy(fontWeight = FontWeight.SemiBold), Modifier.padding(start = 6.dp), color = fg)
    }
}
