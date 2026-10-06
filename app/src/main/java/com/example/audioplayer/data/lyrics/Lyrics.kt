package com.example.audioplayer.data.lyrics

/** Satu baris lirik. [timeMs] = kapan baris ini mulai dinyanyikan (0 untuk lirik tanpa waktu). */
data class LyricLine(val timeMs: Long, val text: String)

/** Lirik sebuah lagu. [synced] = punya penanda waktu per baris (format LRC). */
data class Lyrics(val lines: List<LyricLine>, val synced: Boolean) {

    /** Indeks baris yang sedang dinyanyikan pada [positionMs], atau -1 sebelum baris pertama. */
    fun indexAt(positionMs: Long): Int {
        if (!synced) return -1
        // pencarian biner: baris terakhir yang waktunya <= posisi
        var low = 0
        var high = lines.lastIndex
        var found = -1
        while (low <= high) {
            val mid = (low + high) ushr 1
            if (lines[mid].timeMs <= positionMs) {
                found = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return found
    }
}
