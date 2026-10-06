package com.example.audioplayer.ui.components.molecules

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.atoms.Hairline
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.pressable
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

/** Baris navigasi dengan ikon & chevron (menu Pustaka, Folder, dll.). */
@Composable
fun NavRow(
    label: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    leading: (@Composable () -> Unit)? = null,
    subtitle: String? = null,
    big: Boolean = false,
) {
    val c = LocalIos.current
    Row(Modifier.fillMaxWidth().pressable(onClick = onClick).padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        when {
            leading != null -> leading()
            icon != null -> Ico(icon, c.accent, size = 26.dp)
        }
        Box(Modifier.weight(1f).padding(start = 14.dp)) {
            Row(
                Modifier.fillMaxWidth().height(if (subtitle != null) 64.dp else if (big) 54.dp else 48.dp).padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Txt(label, if (big) IosType.title3.copy(fontWeight = FontWeight.Normal) else IosType.body)
                    if (subtitle != null) Txt(subtitle, IosType.subhead, color = c.secondaryLabel)
                }
                Ico(Icons.ChevronRight, c.tertiaryLabel, size = 22.dp)
            }
            Hairline(Modifier.align(Alignment.BottomStart))
        }
    }
}
