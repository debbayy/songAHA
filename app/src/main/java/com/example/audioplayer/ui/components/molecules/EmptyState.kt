package com.example.audioplayer.ui.components.molecules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier) {
    val c = LocalIos.current
    Column(
        modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Ico(Icons.Note, c.tertiaryLabel, size = 56.dp)
        Spacer(Modifier.height(12.dp))
        Txt(title, IosType.title3, align = TextAlign.Center, maxLines = 2)
        Spacer(Modifier.height(4.dp))
        Txt(message, IosType.subhead, color = c.secondaryLabel, align = TextAlign.Center, maxLines = 4)
    }
}
