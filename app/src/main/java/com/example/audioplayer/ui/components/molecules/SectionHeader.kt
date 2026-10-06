package com.example.audioplayer.ui.components.molecules

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.noRippleClick
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun SectionHeader(title: String, onMore: (() -> Unit)? = null) {
    val c = LocalIos.current
    Row(
        Modifier
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 10.dp)
            .then(if (onMore != null) Modifier.noRippleClick(onMore) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Txt(title, IosType.title2)
        if (onMore != null) Ico(Icons.ChevronRight, c.secondaryLabel, size = 26.dp)
    }
}
