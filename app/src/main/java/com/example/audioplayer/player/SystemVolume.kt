package com.example.audioplayer.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Volume musik sistem (0..1). Ikut berubah saat tombol volume fisik ditekan, jadi slider di
 * aplikasi dan tombol fisik selalu sinkron. Panggil [release] saat tidak dipakai lagi.
 */
class SystemVolume(context: Context) {

    private val app = context.applicationContext
    private val audio = app.getSystemService(AudioManager::class.java)
    private val maxSteps = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)

    private val _level = MutableStateFlow(read())
    val level: StateFlow<Float> = _level.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val v = read()
            // abaikan selisih pembulatan saat slider sedang digeser
            if (abs(v - _level.value) >= 1f / maxSteps) _level.value = v
        }
    }

    init {
        ContextCompat.registerReceiver(
            app, receiver, IntentFilter("android.media.VOLUME_CHANGED_ACTION"),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    fun set(fraction: Float) {
        val f = fraction.coerceIn(0f, 1f)
        _level.value = f
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, (f * maxSteps).roundToInt(), 0)
    }

    fun release() {
        runCatching { app.unregisterReceiver(receiver) }
    }

    private fun read() = audio.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxSteps
}
