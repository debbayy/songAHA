package com.example.audioplayer.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.bounce
import com.example.audioplayer.ui.theme.IosType

private val CardShape = RoundedCornerShape(16.dp)

/**
 * Kartu kategori ala "Jelajahi" Apple Music: gradasi warna, judul di kiri atas, dan ikon besar
 * yang dimiringkan di kanan bawah (sebagian terpotong tepi kartu supaya terasa dinamis).
 */
@Composable
internal fun CategoryCard(
    label: String,
    icon: ImageVector,
    colors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .height(104.dp)
            .clip(CardShape)
            .background(Brush.linearGradient(colors))
            // kilau lembut di pojok kiri atas memberi kesan kedalaman
            .background(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.22f), Color.Transparent), center = Offset.Zero, radius = 320f))
            .bounce(onClick = onClick),
    ) {
        Txt(label, IosType.headline, Modifier.padding(12.dp), color = Color.White, maxLines = 2)
        Ico(
            icon, Color.White.copy(alpha = 0.9f),
            Modifier.align(Alignment.BottomEnd).offset(x = 6.dp, y = 6.dp).rotate(-18f),
            size = 60.dp,
        )
    }
}
