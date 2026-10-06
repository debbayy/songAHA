package com.example.audioplayer.ui.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.bounce
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

/** Pil pilihan (mis. preset equalizer). Yang terpilih terisi warna aksen. */
@Composable
fun Chip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalIos.current
    Box(
        modifier
            .height(34.dp)
            .clip(CircleShape)
            .background(if (selected) c.accent else c.fill)
            .bounce(onClick = onClick)
            .padding(horizontal = 14.dp),
        Alignment.Center,
    ) {
        Txt(
            label, IosType.subhead.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium),
            color = if (selected) c.onAccent else c.label,
        )
    }
}
