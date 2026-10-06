package com.example.audioplayer.data.lyrics

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.LruCache
import com.example.audioplayer.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

/**
 * Mencari lirik sebuah lagu, berurutan dari:
 * 1. file .lrc yang diimpor user lewat aplikasi (disimpan di penyimpanan internal),
 * 2. file .lrc bernama sama di folder lagu,
 * 3. lirik yang tertanam di tag file audio.
 */
class LyricsRepository(context: Context) {

    private val app = context.applicationContext
    private val importDir = File(app.filesDir, "lyrics")

    /** Hasil (termasuk "tidak ada") disimpan supaya file tidak dibaca ulang tiap buka panel. */
    private val cache = LruCache<String, Found>(32)
    private class Found(val lyrics: Lyrics?)

    /** Naik setiap lirik diimpor/dihapus, supaya UI memuat ulang. */
    private val _revision = MutableStateFlow(0)
    val revision: StateFlow<Int> = _revision.asStateFlow()

    suspend fun load(song: Song): Lyrics? {
        cache.get(song.id)?.let { return it.lyrics }
        val lyrics = withContext(Dispatchers.IO) {
            fromImported(song) ?: fromSidecar(song) ?: fromEmbedded(song)
        }
        cache.put(song.id, Found(lyrics))
        return lyrics
    }

    fun hasImported(song: Song): Boolean = importedFile(song).exists()

    /** Salin file .lrc pilihan user untuk lagu ini. False kalau isinya bukan lirik. */
    suspend fun import(song: Song, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val text = runCatching {
            app.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
        }.getOrNull()
        if (text == null || LrcParser.parse(text) == null) return@withContext false
        importDir.mkdirs()
        importedFile(song).writeText(text)
        invalidate(song)
        true
    }

    fun removeImported(song: Song) {
        importedFile(song).delete()
        invalidate(song)
    }

    private fun invalidate(song: Song) {
        cache.remove(song.id)
        _revision.value++
    }

    // ---- Sumber lirik ------------------------------------------------------------------------

    private fun fromImported(song: Song): Lyrics? =
        importedFile(song).takeIf { it.exists() }?.readText()?.let(LrcParser::parse)

    /** File "Judul Lagu.lrc" di samping "Judul Lagu.mp3". */
    private fun fromSidecar(song: Song): Lyrics? {
        if (song.fileName.isEmpty()) return null
        val lrcName = song.fileName.substringBeforeLast('.') + ".lrc"
        val text = if (song.id.startsWith("saf:")) readSiblingDocument(song, lrcName)
        // Android 11+ membatasi baca file non-media, jadi ini bisa gagal; diam-diam dilewati
        else runCatching { File(song.folder, lrcName).takeIf { it.canRead() }?.readText() }.getOrNull()
        return text?.let(LrcParser::parse)
    }

    private fun fromEmbedded(song: Song): Lyrics? = runCatching {
        app.contentResolver.openInputStream(song.uri)?.use(EmbeddedLyricsReader::read)
    }.getOrNull()?.let(LrcParser::parse)

    /** Lagu dari folder pilihan (SAF): cari dokumen .lrc di folder induk yang sama. */
    private fun readSiblingDocument(song: Song, name: String): String? = runCatching {
        val tree = DocumentsContract.buildTreeDocumentUri(song.uri.authority, DocumentsContract.getTreeDocumentId(song.uri))
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, song.folder)
        val projection = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME)
        val docId = app.contentResolver.query(children, projection, null, null, null)?.use { c ->
            generateSequence { if (c.moveToNext()) c.getString(0) to c.getString(1) else null }
                .firstOrNull { (_, display) -> display.equals(name, ignoreCase = true) }?.first
        } ?: return null
        val uri = DocumentsContract.buildDocumentUriUsingTree(tree, docId)
        app.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
    }.getOrNull()

    private fun importedFile(song: Song) = File(importDir, sha1(song.id) + ".lrc")

    private fun sha1(text: String): String =
        MessageDigest.getInstance("SHA-1").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
}
