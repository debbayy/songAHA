package com.example.audioplayer.player.equalizer

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import kotlin.math.ln
import kotlin.math.roundToInt

/**
 * Equalizer & bass boost bawaan Android yang ditempel ke satu sesi audio.
 * Band aplikasi ([EQ_BANDS_HZ]) diinterpolasi ke band milik perangkat, karena jumlah dan
 * frekuensi band berbeda-beda tiap HP.
 */
class AudioEffects private constructor(
    private val equalizer: Equalizer,
    private val bassBoost: BassBoost?,
) {
    private val levelRange = equalizer.bandLevelRange.let { it[0].toInt()..it[1].toInt() }
    private val deviceBandsHz = (0 until equalizer.numberOfBands).map { equalizer.getCenterFreq(it.toShort()) / 1000f }

    fun apply(settings: EqualizerSettings) {
        equalizer.enabled = settings.enabled
        deviceBandsHz.forEachIndexed { band, hz ->
            val millibel = (gainAt(hz, settings.gainsDb) * 100).roundToInt().coerceIn(levelRange)
            equalizer.setBandLevel(band.toShort(), millibel.toShort())
        }
        bassBoost?.run {
            enabled = settings.enabled && settings.bassBoost > 0f
            setStrength((settings.bassBoost * 1000).roundToInt().coerceIn(0, 1000).toShort())
        }
    }

    fun release() {
        equalizer.release()
        bassBoost?.release()
    }

    companion object {
        /** null kalau perangkat (mis. sebagian emulator) tidak menyediakan efek audio. */
        fun create(audioSessionId: Int): AudioEffects? = runCatching {
            val bass = runCatching { BassBoost(0, audioSessionId).takeIf { it.strengthSupported } }.getOrNull()
            AudioEffects(Equalizer(0, audioSessionId), bass)
        }.getOrNull()

        /** Penguatan di frekuensi [hz], interpolasi linear pada skala log antar band aplikasi. */
        private fun gainAt(hz: Float, gains: List<Float>): Float {
            val bands = EQ_BANDS_HZ
            if (hz <= bands.first()) return gains.first()
            if (hz >= bands.last()) return gains.last()
            val upper = bands.indexOfFirst { it >= hz }
            val lower = upper - 1
            val t = (ln(hz) - ln(bands[lower].toFloat())) / (ln(bands[upper].toFloat()) - ln(bands[lower].toFloat()))
            return gains[lower] + (gains[upper] - gains[lower]) * t
        }
    }
}
