package com.example.audioplayer.ui

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.example.audioplayer.data.AUDIO_PERMISSION
import com.example.audioplayer.data.Library
import com.example.audioplayer.data.LibraryStore
import com.example.audioplayer.data.Song
import com.example.audioplayer.data.SongRepository
import com.example.audioplayer.data.WallpaperFile
import com.example.audioplayer.data.lyrics.LyricsRepository
import com.example.audioplayer.data.toMediaItem
import com.example.audioplayer.player.PlaybackService
import com.example.audioplayer.player.equalizer.EqualizerStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class QueueEntry(val index: Int, val song: Song)

data class PlayerState(
    val current: Song? = null,
    val isPlaying: Boolean = false,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val upNext: List<QueueEntry> = emptyList(),
)

/** endsAt memakai SystemClock.elapsedRealtime(); endOfTrack = berhenti saat lagu selesai. */
data class SleepTimer(val endsAt: Long = 0L, val endOfTrack: Boolean = false)

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SongRepository(app)
    val store = LibraryStore(app)
    val equalizer = EqualizerStore.get(app)
    val lyrics = LyricsRepository(app)
    private var controller: MediaController? = null

    private val _library = MutableStateFlow(Library.EMPTY)
    val library: StateFlow<Library> = _library.asStateFlow()

    /** Semua audio hasil scan, sebelum disaring [LibraryFilter]. */
    private var allSongs: List<Song> = emptyList()

    /** Jumlah audio yang disembunyikan filter (ditampilkan di Pengaturan). */
    private val _hiddenCount = MutableStateFlow(0)
    val hiddenCount: StateFlow<Int> = _hiddenCount.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _hasPermission = MutableStateFlow(hasAudioPermission())
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private val _player = MutableStateFlow(PlayerState())
    val player: StateFlow<PlayerState> = _player.asStateFlow()

    /** Dipisah dari [player] supaya update tiap 0,5 detik hanya me-render ulang slider. */
    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position.asStateFlow()

    private val _sleep = MutableStateFlow<SleepTimer?>(null)
    val sleep: StateFlow<SleepTimer?> = _sleep.asStateFlow()

    // ---- Pantau perubahan file musik di HP ----
    private var reloadJob: Job? = null
    private val mediaObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            reloadJob?.cancel()
            reloadJob = viewModelScope.launch { delay(1500); reload() }
        }
    }

    private var lastRecentId: String? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            val queueChanged = events.containsAny(
                Player.EVENT_TIMELINE_CHANGED,
                Player.EVENT_MEDIA_ITEM_TRANSITION,
                Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED,
            )
            syncFromController(rebuildQueue = queueChanged)
        }
    }

    init {
        val token = SessionToken(app, ComponentName(app, PlaybackService::class.java))
        val future = MediaController.Builder(app, token).buildAsync()
        future.addListener({
            try {
                controller = future.get().also { it.addListener(listener) }
                syncFromController(rebuildQueue = true)
            } catch (e: Exception) {
                // gagal konek ke service
            }
        }, ContextCompat.getMainExecutor(app))

        app.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, mediaObserver
        )

        viewModelScope.launch {
            while (true) {
                delay(500)
                val c = controller ?: continue
                if (c.isPlaying) _position.value = c.currentPosition
                _sleep.value?.let { t ->
                    if (!t.endOfTrack && SystemClock.elapsedRealtime() >= t.endsAt) _sleep.value = null
                }
            }
        }

        // Pustaka disaring ulang setiap aturan filter diubah, tanpa perlu scan ulang
        viewModelScope.launch {
            store.settings.map { it.filter }.distinctUntilChanged().drop(1).collect { rebuildLibrary() }
        }

        reload()
    }

    // ---- Pustaka -----------------------------------------------------------------------

    fun reload() {
        viewModelScope.launch {
            _loading.value = true
            val granted = hasAudioPermission()
            _hasPermission.value = granted
            val device = if (granted) repo.loadFromDevice() else emptyList()
            val folders = store.safFolders.value.flatMap { repo.loadFromFolder(Uri.parse(it)) }
            allSongs = withContext(Dispatchers.Default) { (device + folders).sortedBy { it.title.lowercase() } }
            rebuildLibrary()
            _loading.value = false
        }
    }

    private suspend fun rebuildLibrary() {
        val filter = store.settings.value.filter
        val songs = allSongs
        val visible = withContext(Dispatchers.Default) { songs.filter(filter::accepts) }
        _library.value = withContext(Dispatchers.Default) { Library(visible) }
        _hiddenCount.value = songs.size - visible.size
        syncFromController(rebuildQueue = true)
    }

    fun setWallpaper(uri: Uri, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = WallpaperFile.import(getApplication(), uri)
            if (ok) store.updateSettings { it.copy(wallpaper = System.currentTimeMillis()) }
            onDone(ok)
        }
    }

    fun importLyrics(song: Song, uri: Uri, onDone: (Boolean) -> Unit) {
        viewModelScope.launch { onDone(lyrics.import(song, uri)) }
    }

    fun clearWallpaper() {
        store.updateSettings { it.copy(wallpaper = 0L) }
        WallpaperFile.delete(getApplication())
    }

    fun addFolder(uri: Uri) {
        try {
            getApplication<Application>().contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            // tidak bisa dipersist, tetap dipakai untuk sesi ini
        }
        store.addSafFolder(uri.toString())
        reload()
    }

    fun removeFolder(uri: String) {
        runCatching {
            getApplication<Application>().contentResolver.releasePersistableUriPermission(
                Uri.parse(uri), Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        store.removeSafFolder(uri)
        reload()
    }

    private fun hasAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(getApplication(), AUDIO_PERMISSION) ==
            PackageManager.PERMISSION_GRANTED

    // ---- Kontrol pemutar ---------------------------------------------------------------

    fun playSongs(songs: List<Song>, startIndex: Int = 0, shuffle: Boolean = false) {
        val c = controller ?: return
        if (songs.isEmpty()) return
        c.shuffleModeEnabled = shuffle
        c.setMediaItems(songs.map { it.toMediaItem() }, startIndex.coerceIn(songs.indices), 0L)
        c.prepare()
        c.play()
    }

    fun shuffle(songs: List<Song>) {
        if (songs.isNotEmpty()) playSongs(songs, songs.indices.random(), shuffle = true)
    }

    fun playNext(song: Song) {
        val c = controller ?: return
        if (c.mediaItemCount == 0) return playSongs(listOf(song))
        c.addMediaItem(c.nextMediaItemIndex.takeIf { it != C.INDEX_UNSET } ?: c.mediaItemCount, song.toMediaItem())
    }

    fun addToQueue(songs: List<Song>) {
        val c = controller ?: return
        if (c.mediaItemCount == 0) return playSongs(songs)
        c.addMediaItems(songs.map { it.toMediaItem() })
    }

    fun togglePlay() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else {
            if (c.playbackState == Player.STATE_IDLE) c.prepare()
            c.play()
        }
    }

    fun next() { controller?.seekToNext() }

    /** Seperti iOS: mundur ke awal lagu dulu kalau sudah lewat 3 detik. */
    fun previous() { controller?.seekToPrevious() }

    /** Langsung ke lagu sebelumnya (tanpa aturan "mundur ke awal lagu" seperti tombol ⏮). */
    fun previousSong() { controller?.seekToPreviousMediaItem() }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        _position.value = positionMs
    }

    fun playQueueIndex(index: Int) {
        controller?.run { seekTo(index, 0L); play() }
    }

    /** Pindahkan lagu di antrean dari indeks [from] ke [to] (indeks media di pemutar). */
    fun moveQueueItem(from: Int, to: Int) { controller?.moveMediaItem(from, to) }

    fun removeFromQueue(index: Int) { controller?.removeMediaItem(index) }

    fun toggleShuffle() { controller?.run { shuffleModeEnabled = !shuffleModeEnabled } }

    fun cycleRepeat() {
        controller?.run {
            repeatMode = when (repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    /** minutes > 0: menit, < 0: akhir lagu, 0: matikan. */
    fun setSleepTimer(minutes: Int) {
        controller?.sendCustomCommand(
            SessionCommand(PlaybackService.CMD_SLEEP, Bundle.EMPTY),
            Bundle().apply { putInt(PlaybackService.ARG_MINUTES, minutes) },
        )
        _sleep.value = when {
            minutes > 0 -> SleepTimer(endsAt = SystemClock.elapsedRealtime() + minutes * 60_000L)
            minutes < 0 -> SleepTimer(endOfTrack = true)
            else -> null
        }
    }

    // ---- Sinkronisasi state ------------------------------------------------------------

    private fun resolve(item: MediaItem): Song =
        _library.value.byId[item.mediaId]
            ?: Song.fromMediaItem(item)
            ?: Song(
                id = item.mediaId,
                title = item.mediaMetadata.title?.toString().orEmpty(),
                artist = item.mediaMetadata.artist?.toString().orEmpty(),
                album = item.mediaMetadata.albumTitle?.toString().orEmpty(),
                albumId = 0L, durationMs = 0L, uri = Uri.EMPTY, folder = "",
            )

    private fun buildUpNext(c: MediaController): List<QueueEntry> {
        val timeline = c.currentTimeline
        if (timeline.isEmpty) return emptyList()
        val out = mutableListOf<QueueEntry>()
        var i = timeline.getNextWindowIndex(c.currentMediaItemIndex, Player.REPEAT_MODE_OFF, c.shuffleModeEnabled)
        while (i != C.INDEX_UNSET && out.size < MAX_UP_NEXT) {
            out += QueueEntry(i, resolve(c.getMediaItemAt(i)))
            i = timeline.getNextWindowIndex(i, Player.REPEAT_MODE_OFF, c.shuffleModeEnabled)
        }
        return out
    }

    private fun syncFromController(rebuildQueue: Boolean) {
        val c = controller ?: return
        val current = c.currentMediaItem?.let { resolve(it) }
        val wasPlaying = _player.value.isPlaying
        _player.value = PlayerState(
            current = current,
            isPlaying = c.isPlaying,
            durationMs = c.duration.takeIf { it > 0 } ?: current?.durationMs ?: 0L,
            shuffle = c.shuffleModeEnabled,
            repeatMode = c.repeatMode,
            upNext = if (rebuildQueue) buildUpNext(c) else _player.value.upNext,
        )
        _position.value = c.currentPosition

        if (current != null && c.isPlaying && current.id != lastRecentId) {
            lastRecentId = current.id
            store.addRecent(current.id)
        }
        // Timer "akhir lagu" selesai (atau dibatalkan user dengan pause)
        if (wasPlaying && !c.isPlaying && _sleep.value?.endOfTrack == true) setSleepTimer(0)
    }

    override fun onCleared() {
        val app = getApplication<Application>()
        app.contentResolver.unregisterContentObserver(mediaObserver)
        controller?.removeListener(listener)
        controller?.release()
        controller = null
        super.onCleared()
    }

    private companion object {
        const val MAX_UP_NEXT = 200
    }
}
