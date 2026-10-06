package com.example.audioplayer.player.equalizer

/** Frekuensi tengah 5 band equalizer aplikasi (Hz). Dipetakan ke band milik perangkat. */
val EQ_BANDS_HZ = listOf(60, 230, 910, 3_600, 14_000)

/** Batas penguatan tiap band, ± dB. */
const val EQ_MAX_DB = 12f

data class EqPreset(val id: String, val label: String, val gainsDb: List<Float>)

/** Preset bawaan. Nilai = penguatan (dB) untuk [EQ_BANDS_HZ]. */
val EQ_PRESETS = listOf(
    EqPreset("flat", "Datar", listOf(0f, 0f, 0f, 0f, 0f)),
    EqPreset("bass", "Bass", listOf(6f, 4f, 0f, -1f, 0f)),
    EqPreset("pop", "Pop", listOf(-1f, 2f, 4f, 2f, -1f)),
    EqPreset("rock", "Rock", listOf(4f, 2f, -2f, 2f, 4f)),
    EqPreset("vocal", "Vokal", listOf(-2f, -1f, 3f, 4f, 2f)),
    EqPreset("acoustic", "Akustik", listOf(3f, 1f, 1f, 2f, 3f)),
    EqPreset("electronic", "Elektronik", listOf(5f, 3f, 0f, 2f, 4f)),
)

const val EQ_CUSTOM_ID = "custom"

data class EqualizerSettings(
    val enabled: Boolean = false,
    /** id dari [EQ_PRESETS], atau [EQ_CUSTOM_ID] kalau band diatur manual. */
    val presetId: String = "flat",
    val gainsDb: List<Float> = EQ_PRESETS.first().gainsDb,
    /** Kekuatan bass boost 0..1. */
    val bassBoost: Float = 0f,
) {
    val presetLabel: String
        get() = EQ_PRESETS.firstOrNull { it.id == presetId }?.label ?: "Kustom"

    fun withPreset(preset: EqPreset) = copy(presetId = preset.id, gainsDb = preset.gainsDb)

    fun withBand(index: Int, db: Float) = copy(
        presetId = EQ_CUSTOM_ID,
        gainsDb = gainsDb.toMutableList().also { it[index] = db.coerceIn(-EQ_MAX_DB, EQ_MAX_DB) },
    )
}
