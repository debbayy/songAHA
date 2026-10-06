package com.example.audioplayer.ui.components.molecules

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

/** Baris berisi label dan tombol (x) untuk menghapusnya, mis. daftar folder tambahan. */
@Composable
fun RemovableRow(label: String, onRemove: () -> Unit) {
    val c = LocalIos.current
    Row(
        Modifier.fillMaxWidth().height(48.dp).padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Txt(label, IosType.body, Modifier.weight(1f))
        Box(Modifier.size(44.dp).pressable(highlight = false, onClick = onRemove), Alignment.Center) {
            Ico(Icons.CloseCircle, c.tertiaryLabel, size = 22.dp)
        }
    }
}
