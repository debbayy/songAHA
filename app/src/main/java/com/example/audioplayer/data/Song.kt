package com.example.audioplayer.data

import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import org.json.JSONObject

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val uri: Uri,
    val folder: String,
    val track: Int = 0,
    val dateAdded: Long = 0L,
    /** Nama file beserta ekstensi, dipakai untuk mencari file lirik .lrc di sebelahnya. */
    val fileName: String = "",
) {
    val displayArtist: String get() = artist.ifBlank { UNKNOWN_ARTIST }

    /** Kunci pengelompokan album. Lagu dari folder SAF tidak punya album. */
    val albumKey: String
        get() = if (albumId > 0) "id:$albumId" else "name:${album.lowercase()}|${artist.lowercase()}"

    /** Kunci cache cover: satu cover per album, atau per file untuk lagu SAF. */
    val artKey: String get() = if (albumId > 0) "a$albumId" else uri.toString()

    /** Cover album dari MediaStore (dipakai juga oleh notifikasi Media3). */
    val artworkUri: Uri? get() = if (albumId > 0) ContentUris.withAppendedId(ALBUM_ART, albumId) else null

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("artist", artist)
        .put("album", album)
        .put("albumId", albumId)
        .put("duration", durationMs)
        .put("uri", uri.toString())
        .put("folder", folder)
        .put("track", track)
        .put("added", dateAdded)
        .put("file", fileName)

    companion object {
        const val UNKNOWN_ARTIST = "Artis Tidak Dikenal"
        private const val EXTRA_SONG = "song"
        private val ALBUM_ART: Uri = Uri.parse("content://media/external/audio/albumart")

        fun fromJson(o: JSONObject) = Song(
            id = o.getString("id"),
            title = o.optString("title"),
            artist = o.optString("artist"),
            album = o.optString("album"),
            albumId = o.optLong("albumId"),
            durationMs = o.optLong("duration"),
            uri = Uri.parse(o.getString("uri")),
            folder = o.optString("folder"),
            track = o.optInt("track"),
            dateAdded = o.optLong("added"),
            fileName = o.optString("file"),
        )

        /** Ambil kembali Song yang disisipkan di extras MediaItem. */
        fun fromMediaItem(item: MediaItem): Song? =
            item.mediaMetadata.extras?.getString(EXTRA_SONG)?.let {
                runCatching { fromJson(JSONObject(it)) }.getOrNull()
            }

        internal fun extrasOf(song: Song) = Bundle().apply {
            putString(EXTRA_SONG, song.toJson().toString())
        }
    }
}

fun Song.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setMediaId(id)
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist.ifBlank { null })
                .setAlbumTitle(album.ifBlank { null })
                .setArtworkUri(artworkUri)
                .setExtras(Song.extrasOf(this))
                .build()
        )
        .build()

data class Album(val key: String, val title: String, val artist: String, val songs: List<Song>) {
    val cover: Song get() = songs.first()
}

data class Artist(val name: String, val songs: List<Song>, val albums: List<Album>)

data class FolderGroup(val path: String, val name: String, val songs: List<Song>)

/** Snapshot pustaka yang sudah dikelompokkan. Dibangun sekali di background thread. */
class Library(val songs: List<Song>) {
    val byId: Map<String, Song> = songs.associateBy { it.id }

    val albums: List<Album> = songs
        .filter { it.album.isNotBlank() }
        .groupBy { it.albumKey }
        .map { (key, list) ->
            val sorted = list.sortedWith(compareBy({ it.track % 1000 }, { it.title.lowercase() }))
            val artists = sorted.map { it.displayArtist }.distinct()
            Album(key, sorted.first().album, artists.singleOrNull() ?: "Berbagai Artis", sorted)
        }
        .sortedBy { it.title.lowercase() }

    val albumByKey: Map<String, Album> = albums.associateBy { it.key }

    val artists: List<Artist> = run {
        val albumsByArtist = albums.groupBy { it.artist }
        songs.groupBy { it.displayArtist }
            .map { (name, list) -> Artist(name, list, albumsByArtist[name].orEmpty()) }
            .sortedBy { it.name.lowercase() }
    }

    val artistByName: Map<String, Artist> = artists.associateBy { it.name }

    val folders: List<FolderGroup> = songs
        .filter { it.folder.isNotBlank() }
        .groupBy { it.folder }
        .map { (path, list) ->
            FolderGroup(path, path.substringAfterLast('/').substringAfterLast(':').ifBlank { "Penyimpanan" }, list)
        }
        .sortedBy { it.name.lowercase() }

    val folderByPath: Map<String, FolderGroup> = folders.associateBy { it.path }

    val recentAlbums: List<Album> = albums.sortedByDescending { a -> a.songs.maxOf { it.dateAdded } }.take(12)

    companion object {
        val EMPTY = Library(emptyList())
    }
}
