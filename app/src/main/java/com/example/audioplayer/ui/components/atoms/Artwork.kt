package com.example.audioplayer.ui.components.atoms

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.ArtworkLoader
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun Artwork(
    song: Song?,
    sizeHint: Dp,
    modifier: Modifier = Modifier,
    corner: Dp = 6.dp,
    iconScale: Float = 0.42f,
) {
    val c = LocalIos.current
    val context = LocalContext.current
    val px = with(LocalDensity.current) { sizeHint.roundToPx() }
    val bitmap by produceState<Bitmap?>(
        initialValue = song?.let { ArtworkLoader.cached(it, px) },
        key1 = song?.artKey,
        key2 = px,
    ) {
        if (value == null && song != null) value = ArtworkLoader.load(context, song, px)
    }
    Box(
        modifier
            .clip(RoundedCornerShape(corner))
            .background(Brush.verticalGradient(listOf(c.placeholderTop, c.placeholderBottom))),
        contentAlignment = Alignment.Center,
    ) {
        val bmp = bitmap
        if (bmp != null) {
            val image = remember(bmp) { bmp.asImageBitmap() }
            Image(image, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Ico(Icons.Note, c.secondaryLabel.copy(alpha = 0.5f), Modifier.fillMaxSize(iconScale), size = sizeHint * iconScale)
        }
    }
}
