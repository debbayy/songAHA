package com.example.audioplayer.player.equalizer

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sumber tunggal pengaturan equalizer, dipakai bersama oleh layar (mengubah) dan
 * [com.example.audioplayer.player.PlaybackService] (menerapkan ke audio). Keduanya berjalan
 * di proses yang sama, jadi cukup satu instance per aplikasi.
 */
class EqualizerStore private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("equalizer", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<EqualizerSettings> = _settings.asStateFlow()

    /** null = belum diketahui (service belum jalan), false = perangkat tidak mendukung. */
    private val _supported = MutableStateFlow<Boolean?>(null)
    val supported: StateFlow<Boolean?> = _supported.asStateFlow()

    fun update(transform: (EqualizerSettings) -> EqualizerSettings) {
        val s = transform(_settings.value)
        _settings.value = s
        prefs.edit()
            .putBoolean(KEY_ENABLED, s.enabled)
            .putString(KEY_PRESET, s.presetId)
            .putString(KEY_GAINS, s.gainsDb.joinToString(","))
            .putFloat(KEY_BASS, s.bassBoost)
            .apply()
    }

    internal fun reportSupported(supported: Boolean) {
        _supported.value = supported
    }

    private fun read(): EqualizerSettings {
        val default = EqualizerSettings()
        val gains = prefs.getString(KEY_GAINS, null)
            ?.split(",")?.mapNotNull { it.toFloatOrNull() }
            ?.takeIf { it.size == EQ_BANDS_HZ.size }
            ?: default.gainsDb
        return EqualizerSettings(
            enabled = prefs.getBoolean(KEY_ENABLED, default.enabled),
            presetId = prefs.getString(KEY_PRESET, null) ?: default.presetId,
            gainsDb = gains,
            bassBoost = prefs.getFloat(KEY_BASS, default.bassBoost),
        )
    }

    companion object {
        private const val KEY_ENABLED = "enabled"
        private const val KEY_PRESET = "preset"
        private const val KEY_GAINS = "gains"
        private const val KEY_BASS = "bass"

        @Volatile private var instance: EqualizerStore? = null

        fun get(context: Context): EqualizerStore =
            instance ?: synchronized(this) {
                instance ?: EqualizerStore(context.applicationContext).also { instance = it }
            }
    }
}
