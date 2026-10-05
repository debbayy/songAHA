package com.example.audioplayer.player

import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.audioplayer.MainActivity
import com.example.audioplayer.data.Song
import com.example.audioplayer.data.toMediaItem
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import org.json.JSONArray

/**
 * Service pemutar. Media3 otomatis membuat notifikasi (play/pause/next) dan
 * menangani headset, tombol Bluetooth, serta lockscreen lewat MediaSession.
 * Antrean & posisi disimpan supaya bisa dilanjutkan setelah app ditutup.
 */
class PlaybackService : MediaSessionService() {

    private var session: MediaSession? = null
    private lateinit var player: ExoPlayer
    private lateinit var prefs: SharedPreferences
    private val handler = Handler(Looper.getMainLooper())
    private val sleepRunnable = Runnable { player.pause() }

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("playback", MODE_PRIVATE)
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setHandleAudioBecomingNoisy(true) // pause saat headset dicabut
            .setWakeMode(C.WAKE_MODE_LOCAL)    // CPU tetap hidup saat layar mati
            .build()

        player.addListener(object : Player.Listener {
            override fun onEvents(p: Player, events: Player.Events) {
                if (events.contains(Player.EVENT_TIMELINE_CHANGED)) saveQueue()
                if (events.containsAny(
                        Player.EVENT_MEDIA_ITEM_TRANSITION,
                        Player.EVENT_IS_PLAYING_CHANGED,
                        Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED,
                        Player.EVENT_REPEAT_MODE_CHANGED,
                    )
                ) savePosition()
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                // Timer "akhir lagu" cukup sekali
                if (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM) {
                    player.pauseAtEndOfMediaItems = false
                }
            }
        })
        restoreQueue()

        val openApp = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        session = MediaSession.Builder(this, player)
            .setSessionActivity(openApp)
            .setCallback(SessionCallback())
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        savePosition()
        if (!player.playWhenReady || player.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        savePosition()
        handler.removeCallbacks(sleepRunnable)
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }

    // ---- Sleep timer ----

    private fun setSleepTimer(minutes: Int) {
        handler.removeCallbacks(sleepRunnable)
        player.pauseAtEndOfMediaItems = minutes < 0
        if (minutes > 0) handler.postDelayed(sleepRunnable, minutes * 60_000L)
    }

    private inner class SessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult =
            MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(
                    MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                        .add(SessionCommand(CMD_SLEEP, Bundle.EMPTY))
                        .build()
                )
                .build()

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == CMD_SLEEP) {
                setSleepTimer(args.getInt(ARG_MINUTES))
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }
    }

    // ---- Simpan / pulihkan antrean ----

    private fun saveQueue() {
        val arr = JSONArray()
        for (i in 0 until player.mediaItemCount) {
            Song.fromMediaItem(player.getMediaItemAt(i))?.let { arr.put(it.toJson()) }
        }
        prefs.edit().putString(KEY_QUEUE, arr.toString()).apply()
        savePosition()
    }

    private fun savePosition() {
        prefs.edit()
            .putInt(KEY_INDEX, player.currentMediaItemIndex)
            .putLong(KEY_POSITION, player.currentPosition)
            .putBoolean(KEY_SHUFFLE, player.shuffleModeEnabled)
            .putInt(KEY_REPEAT, player.repeatMode)
            .apply()
    }

    private fun restoreQueue() {
        val songs = runCatching {
            val arr = JSONArray(prefs.getString(KEY_QUEUE, "[]"))
            List(arr.length()) { Song.fromJson(arr.getJSONObject(it)) }
        }.getOrDefault(emptyList())
        if (songs.isEmpty()) return
        val index = prefs.getInt(KEY_INDEX, 0).coerceIn(0, songs.lastIndex)
        player.shuffleModeEnabled = prefs.getBoolean(KEY_SHUFFLE, false)
        player.repeatMode = prefs.getInt(KEY_REPEAT, Player.REPEAT_MODE_OFF)
        player.setMediaItems(songs.map { it.toMediaItem() }, index, prefs.getLong(KEY_POSITION, 0L))
        player.prepare()
    }

    companion object {
        const val CMD_SLEEP = "sleep_timer"
        /** > 0: menit, < 0: berhenti di akhir lagu, 0: matikan timer */
        const val ARG_MINUTES = "minutes"

        private const val KEY_QUEUE = "queue"
        private const val KEY_INDEX = "index"
        private const val KEY_POSITION = "position"
        private const val KEY_SHUFFLE = "shuffle"
        private const val KEY_REPEAT = "repeat"
    }
}
