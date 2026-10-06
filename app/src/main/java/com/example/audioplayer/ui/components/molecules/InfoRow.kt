package com.example.audioplayer.ui.components.molecules

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun InfoRow(label: String, value: String) {
    val c = LocalIos.current
    Row(Modifier.fillMaxWidth().height(46.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Txt(label, IosType.body, Modifier.weight(1f))
        Txt(value, IosType.body, color = c.secondaryLabel)
    }
}
