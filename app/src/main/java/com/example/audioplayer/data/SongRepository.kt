package com.example.audioplayer.data

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Izin baca audio yang sesuai versi Android. */
val AUDIO_PERMISSION: String
    get() = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE

private val AUDIO_EXTENSIONS = setOf("mp3", "m4a", "aac", "flac", "wav", "ogg", "opus")

class SongRepository(private val context: Context) {

    /** Semua lagu di HP (butuh izin audio). Cepat karena pakai index MediaStore. */
    @Suppress("DEPRECATION") // kolom DATA tetap terisi di semua versi, dipakai untuk nama folder
    suspend fun loadFromDevice(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_ADDED,
        )
        val selection =
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 0"
        val sort = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        try {
            context.contentResolver.query(collection, projection, selection, null, sort)?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val trackCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val addedCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val path = c.getString(dataCol).orEmpty()
                    songs += Song(
                        id = "ms:$id",
                        title = c.getString(titleCol) ?: "Tanpa Judul",
                        artist = c.getString(artistCol)
                            ?.takeUnless { it == MediaStore.UNKNOWN_STRING }.orEmpty(),
                        album = c.getString(albumCol)
                            ?.takeUnless { it == MediaStore.UNKNOWN_STRING }.orEmpty(),
                        albumId = c.getLong(albumIdCol),
                        durationMs = c.getLong(durCol),
                        uri = ContentUris.withAppendedId(collection, id),
                        folder = path.substringBeforeLast('/', ""),
                        fileName = path.substringAfterLast('/'),
                        track = c.getInt(trackCol),
                        dateAdded = c.getLong(addedCol),
                    )
                }
            }
        } catch (e: SecurityException) {
            // izin belum diberikan
        }
        songs
    }

    /**
     * Scan satu folder (beserta subfolder) pilihan user lewat Storage Access Framework.
     * Tidak butuh izin storage. Judul diambil dari nama file (cepat); tag ID3 muncul saat diputar.
     */
    suspend fun loadFromFolder(treeUri: Uri): List<Song> = withContext(Dispatchers.IO) {
        val out = mutableListOf<Song>()
        try {
            walk(treeUri, DocumentsContract.getTreeDocumentId(treeUri), out)
        } catch (e: Exception) {
            // folder dihapus / izin dicabut -> hasil kosong
        }
        out
    }

    private fun walk(treeUri: Uri, parentDocId: String, out: MutableList<Song>) {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        )
        context.contentResolver.query(childrenUri, projection, null, null, null)?.use { c ->
            while (c.moveToNext()) {
                val docId = c.getString(0) ?: continue
                val name = c.getString(1) ?: continue
                val mime = c.getString(2).orEmpty()
                when {
                    mime == DocumentsContract.Document.MIME_TYPE_DIR -> walk(treeUri, docId, out)
                    mime.startsWith("audio/") ||
                        name.substringAfterLast('.', "").lowercase() in AUDIO_EXTENSIONS -> {
                        out += Song(
                            id = "saf:$docId",
                            title = name.substringBeforeLast('.'),
                            artist = "",
                            album = "",
                            albumId = 0L,
                            durationMs = 0L,
                            uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId),
                            folder = parentDocId,
                            dateAdded = c.getLong(3) / 1000,
                            fileName = name,
                        )
                    }
                }
            }
        }
    }
}
