package com.example.audioplayer.ui.screens.equalizer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.player.equalizer.EQ_BANDS_HZ

/** Lima slider band equalizer berjajar. */
@Composable
fun BandSliders(gainsDb: List<Float>, onBandChange: (band: Int, db: Float) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        EQ_BANDS_HZ.forEachIndexed { band, hz ->
            EqBand(hz, gainsDb[band]) { db -> onBandChange(band, db) }
        }
    }
}
