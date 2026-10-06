package com.example.audioplayer.data.lyrics

/**
 * Pengurai format LRC:
 * ```
 * [ar:Artis]            <- tag info, diabaikan
 * [offset:+250]         <- geser semua waktu (ms)
 * [00:12.34]Baris lirik
 * [00:20.00][01:20.00]Baris yang diulang
 * ```
 * Teks tanpa penanda waktu dianggap lirik biasa (tidak tersinkron).
 */
object LrcParser {

    /** Penanda UTF-8 (BOM) yang sering ada di awal file .lrc buatan Windows. */
    private val BYTE_ORDER_MARK = Char(0xFEFF)

    private val LEADING_TIME = Regex("""^\[(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?]""")
    private val OFFSET_TAG = Regex("""^\[offset:\s*([+-]?\d+)\s*]$""", RegexOption.IGNORE_CASE)
    private val INFO_TAG = Regex("""^\[[a-zA-Z#]+:.*]$""")
    /** Penanda waktu per kata (Enhanced LRC), mis. <00:12.50>. Tidak dipakai, cukup dibuang. */
    private val WORD_TIME = Regex("""<\d{1,3}:\d{1,2}(?:[.:]\d{1,3})?>""")

    fun parse(raw: String): Lyrics? {
        var offsetMs = 0L
        val synced = mutableListOf<LyricLine>()
        val plain = mutableListOf<String>()

        for (rawLine in raw.trimStart(BYTE_ORDER_MARK).lineSequence()) {
            var rest = rawLine.trim()
            OFFSET_TAG.find(rest)?.let { offsetMs = it.groupValues[1].toLong(); continue }

            val times = mutableListOf<Long>()
            while (true) {
                val match = LEADING_TIME.find(rest) ?: break
                times += toMillis(match)
                rest = rest.substring(match.range.last + 1)
            }

            when {
                times.isNotEmpty() -> {
                    val text = WORD_TIME.replace(rest, "").trim()
                    times.forEach { synced += LyricLine(it, text) }
                }
                INFO_TAG.matches(rest) -> Unit
                else -> plain += rest
            }
        }

        if (synced.isNotEmpty()) {
            // offset positif = lirik tampil lebih cepat
            val lines = synced.map { it.copy(timeMs = (it.timeMs - offsetMs).coerceAtLeast(0)) }.sortedBy { it.timeMs }
            return Lyrics(lines, synced = true)
        }
        val text = plain.dropWhile { it.isBlank() }.dropLastWhile { it.isBlank() }
        return if (text.isEmpty()) null else Lyrics(text.map { LyricLine(0, it) }, synced = false)
    }

    private fun toMillis(match: MatchResult): Long {
        val (min, sec, fraction) = match.destructured
        // "5" = 500 ms, "50" = 500 ms, "500" = 500 ms
        val ms = fraction.padEnd(3, '0').take(3).ifEmpty { "0" }.toLong()
        return min.toLong() * 60_000 + sec.toLong() * 1_000 + ms
    }
}
