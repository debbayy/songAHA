package com.example.audioplayer.ui.screens.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.bounce
import com.example.audioplayer.ui.theme.IosType

/** Tampilan saat lagu belum punya lirik, dengan tombol untuk mengimpor file .lrc. */
@Composable
internal fun LyricsMissing(onImport: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Ico(Icons.Lyrics, White60, size = 44.dp)
        Txt("Lirik Belum Tersedia", IosType.title3, Modifier.padding(top = 12.dp), color = Color.White)
        Txt(
            "Impor file .lrc, atau simpan file .lrc dengan nama yang sama di samping file lagu.",
            IosType.subhead, Modifier.padding(top = 6.dp, bottom = 18.dp),
            color = White60, maxLines = 3, align = TextAlign.Center,
        )
        Box(
            Modifier.height(44.dp).clip(CircleShape).background(Color.White).bounce(onClick = onImport).padding(horizontal = 20.dp),
            Alignment.Center,
        ) { Txt("Impor File Lirik", IosType.headline, color = Color.Black) }
    }
}
