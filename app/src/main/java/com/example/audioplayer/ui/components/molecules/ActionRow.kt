package com.example.audioplayer.ui.components.molecules

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.pressable
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

/** Baris tombol berwarna aksen (atau merah kalau [destructive]) dengan ikon di kanan. */
@Composable
fun ActionRow(label: String, icon: ImageVector, destructive: Boolean = false, onClick: () -> Unit) {
    val c = LocalIos.current
    val color = if (destructive) c.red else c.accent
    Row(
        Modifier.fillMaxWidth().height(48.dp).pressable(onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Txt(label, IosType.body, Modifier.weight(1f), color = color)
        Ico(icon, color, size = 22.dp)
    }
}
