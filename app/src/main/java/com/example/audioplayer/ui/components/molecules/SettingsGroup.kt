package com.example.audioplayer.ui.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos
import com.example.audioplayer.ui.theme.backdrop

@Composable
fun SettingsGroup(title: String, footer: String? = null, content: @Composable () -> Unit) {
    val c = LocalIos.current
    Column(Modifier.padding(horizontal = 16.dp)) {
        Txt(title, IosType.footnote, Modifier.padding(start = 16.dp, top = 22.dp, bottom = 6.dp), color = c.secondaryLabel)
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).backdrop(frosted = true).background(c.cell)) { content() }
        if (footer != null) Txt(footer, IosType.footnote, Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp), color = c.secondaryLabel, maxLines = 4)
    }
}
