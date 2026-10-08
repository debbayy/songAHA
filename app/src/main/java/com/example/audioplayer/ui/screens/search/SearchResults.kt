package com.example.audioplayer.ui.screens.search

import com.example.audioplayer.data.Album
import com.example.audioplayer.data.Artist
import com.example.audioplayer.data.Library
import com.example.audioplayer.data.Song

internal data class SearchResults(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
) {
    val isEmpty: Boolean get() = songs.isEmpty() && albums.isEmpty() && artists.isEmpty()
}

/** Cari judul, artis, dan album yang mengandung [query] (tidak peka huruf besar/kecil). */
internal fun search(lib: Library, query: String): SearchResults {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return SearchResults()
    fun String.matches() = lowercase().contains(q)
    return SearchResults(
        songs = lib.songs.filter { it.title.matches() || it.artist.matches() || it.album.matches() }.take(60),
        albums = lib.albums.filter { it.title.matches() || it.artist.matches() }.take(20),
        artists = lib.artists.filter { it.name.matches() }.take(8),
    )
}
