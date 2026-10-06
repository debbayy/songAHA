package com.example.audioplayer.ui.components.molecules

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.pressable
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

/** Baris pengaturan yang membuka halaman lain: label, nilai saat ini, dan chevron. */
@Composable
fun LinkRow(label: String, value: String, onClick: () -> Unit) {
    val c = LocalIos.current
    Row(
        Modifier.fillMaxWidth().height(48.dp).pressable(onClick = onClick).padding(start = 16.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Txt(label, IosType.body, Modifier.weight(1f))
        Txt(value, IosType.body, Modifier.padding(end = 4.dp), color = c.secondaryLabel)
        Ico(Icons.ChevronRight, c.tertiaryLabel, size = 22.dp)
    }
}
