package com.example.audioplayer.data

/**
 * Aturan untuk menyembunyikan audio yang bukan musik: voice note, rekaman suara/telepon,
 * nada dering, dan potongan audio pendek. Lagu tidak dihapus, hanya tidak ditampilkan.
 */
data class LibraryFilter(
    /** Audio yang lebih pendek dari ini disembunyikan. 0 = tidak dibatasi. */
    val minDurationSec: Int = 30,
    /** Sembunyikan isi folder rekaman, voice note WhatsApp, nada dering, dll. */
    val hideRecordings: Boolean = true,
) {
    fun accepts(song: Song): Boolean {
        // Durasi 0 = belum diketahui (lagu dari folder pilihan), jadi jangan disembunyikan
        val tooShort = minDurationSec > 0 && song.durationMs in 1 until minDurationSec * 1000L
        val isRecording = hideRecordings && isRecordingFolder(song.folder)
        return !tooShort && !isRecording
    }

    companion object {
        /** Pilihan durasi minimum di Pengaturan, dalam detik. */
        val DURATION_OPTIONS = listOf(0, 30, 60)

        /** Nama folder (bukan path lengkap) yang isinya hampir pasti bukan musik. */
        private val RECORDING_FOLDER = Regex(
            "whatsapp( business)? voice notes|voice ?recorder|sound_?recorder|recordings?|" +
                "call ?recordings?|call|ringtones|notifications|alarms",
            RegexOption.IGNORE_CASE,
        )

        private fun isRecordingFolder(path: String): Boolean =
            path.split('/', ':').any { RECORDING_FOLDER.matches(it.trim()) }
    }
}
