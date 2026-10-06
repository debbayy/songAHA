package com.example.audioplayer.ui.util

import com.example.audioplayer.data.Song
import java.util.Locale

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
