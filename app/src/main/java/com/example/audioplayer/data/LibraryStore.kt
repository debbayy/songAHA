package com.example.audioplayer.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Playlist(val id: String, val name: String, val songIds: List<String>)

data class AppSettings(
    /** 0 = ikut sistem, 1 = terang, 2 = gelap */
    val theme: Int = 0,
    /** false = kaca bening, true = kaca berwarna (lebih pekat) */
    val glassTinted: Boolean = false,
)

/**
 * Data milik user: favorit, playlist, riwayat, folder SAF, dan pengaturan.
 * Disimpan di SharedPreferences (JSON) supaya tidak perlu library database.
 */
class LibraryStore(context: Context) {

    private val prefs = context.getSharedPreferences("library", Context.MODE_PRIVATE)

    private val _favorites = MutableStateFlow(prefs.getStringSet(KEY_FAV, emptySet())!!.toSet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _playlists = MutableStateFlow(readPlaylists())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _recent = MutableStateFlow(readStrings(KEY_RECENT))
    val recent: StateFlow<List<String>> = _recent.asStateFlow()

    private val _safFolders = MutableStateFlow(prefs.getStringSet(KEY_SAF, emptySet())!!.toSet())
    val safFolders: StateFlow<Set<String>> = _safFolders.asStateFlow()

    private val _settings = MutableStateFlow(
        AppSettings(prefs.getInt(KEY_THEME, 0), prefs.getBoolean(KEY_GLASS, false))
    )
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        // Pindahkan folder dari versi lama app (satu folder di prefs "settings")
        val legacy = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        legacy.getString("folder_uri", null)?.let {
            addSafFolder(it)
            legacy.edit().remove("folder_uri").apply()
        }
    }

    // ---- Favorit ----

    fun toggleFavorite(id: String) {
        _favorites.update { if (id in it) it - id else it + id }
        prefs.edit().putStringSet(KEY_FAV, _favorites.value).apply()
    }

    // ---- Playlist ----

    fun createPlaylist(name: String, songIds: List<String> = emptyList()): Playlist {
        val p = Playlist(UUID.randomUUID().toString(), name, songIds)
        savePlaylists(_playlists.value + p)
        return p
    }

    fun renamePlaylist(id: String, name: String) =
        savePlaylists(_playlists.value.map { if (it.id == id) it.copy(name = name) else it })

    fun deletePlaylist(id: String) = savePlaylists(_playlists.value.filterNot { it.id == id })

    fun addToPlaylist(id: String, songIds: List<String>) =
        savePlaylists(_playlists.value.map { if (it.id == id) it.copy(songIds = it.songIds + songIds) else it })

    fun removeFromPlaylist(id: String, index: Int) =
        savePlaylists(_playlists.value.map {
            if (it.id == id && index in it.songIds.indices) it.copy(songIds = it.songIds.filterIndexed { i, _ -> i != index })
            else it
        })

    // ---- Riwayat ----

    fun addRecent(id: String) {
        val list = (listOf(id) + _recent.value.filterNot { it == id }).take(MAX_RECENT)
        _recent.value = list
        prefs.edit().putString(KEY_RECENT, JSONArray(list).toString()).apply()
    }

    // ---- Folder SAF ----

    fun addSafFolder(uri: String) {
        _safFolders.update { it + uri }
        prefs.edit().putStringSet(KEY_SAF, _safFolders.value).apply()
    }

    fun removeSafFolder(uri: String) {
        _safFolders.update { it - uri }
        prefs.edit().putStringSet(KEY_SAF, _safFolders.value).apply()
    }

    // ---- Pengaturan ----

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val s = transform(_settings.value)
        _settings.value = s
        prefs.edit().putInt(KEY_THEME, s.theme).putBoolean(KEY_GLASS, s.glassTinted).apply()
    }

    // ---- Penyimpanan ----

    private fun savePlaylists(list: List<Playlist>) {
        _playlists.value = list
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(JSONObject().put("id", p.id).put("name", p.name).put("songs", JSONArray(p.songIds)))
        }
        prefs.edit().putString(KEY_PLAYLISTS, arr.toString()).apply()
    }

    private fun readPlaylists(): List<Playlist> = runCatching {
        val arr = JSONArray(prefs.getString(KEY_PLAYLISTS, "[]"))
        List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            val songs = o.getJSONArray("songs")
            Playlist(o.getString("id"), o.getString("name"), List(songs.length()) { songs.getString(it) })
        }
    }.getOrDefault(emptyList())

    private fun readStrings(key: String): List<String> = runCatching {
        val arr = JSONArray(prefs.getString(key, "[]"))
        List(arr.length()) { arr.getString(it) }
    }.getOrDefault(emptyList())

    private companion object {
        const val KEY_FAV = "favorites"
        const val KEY_PLAYLISTS = "playlists"
        const val KEY_RECENT = "recent"
        const val KEY_SAF = "saf_folders"
        const val KEY_THEME = "theme"
        const val KEY_GLASS = "glass_tinted"
        const val MAX_RECENT = 30
    }
}
