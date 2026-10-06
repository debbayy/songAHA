package com.example.audioplayer.ui.screens.equalizer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.player.equalizer.EQ_MAX_DB
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.atoms.VerticalSlider
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos
import kotlin.math.roundToInt

/** Satu band: nilai dB di atas, slider tegak, frekuensi di bawah. */
@Composable
fun EqBand(frequencyHz: Int, gainDb: Float, onChange: (Float) -> Unit) {
    val c = LocalIos.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Txt(formatDb(gainDb), IosType.caption, color = c.secondaryLabel)
        VerticalSlider(
            value = gainDb / EQ_MAX_DB,
            onValueChange = { onChange(snapToHalfDb(it * EQ_MAX_DB)) },
            modifier = Modifier.height(190.dp).padding(vertical = 6.dp),
            active = c.accent,
            inactive = c.fill,
        )
        Txt(formatHz(frequencyHz), IosType.caption2, color = c.secondaryLabel)
    }
}

private fun snapToHalfDb(db: Float) = (db * 2).roundToInt() / 2f

private fun formatDb(db: Float): String {
    val rounded = db.roundToInt()
    return if (rounded > 0) "+$rounded" else "$rounded"
}

/** 60 → "60", 3600 → "3.6k", 14000 → "14k" */
private fun formatHz(hz: Int): String = when {
    hz < 1000 -> "$hz"
    hz % 1000 == 0 -> "${hz / 1000}k"
    else -> "${hz / 100 / 10.0}k"
}
