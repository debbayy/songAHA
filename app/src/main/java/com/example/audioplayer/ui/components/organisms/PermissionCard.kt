package com.example.audioplayer.ui.components.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.PillButton
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun PermissionCard() {
    val a = LocalActions.current
    val c = LocalIos.current
    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(c.fill)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Ico(Icons.Note, c.accent, size = 40.dp)
        Spacer(Modifier.height(8.dp))
        Txt("Izinkan Akses Musik", IosType.headline)
        Txt(
            "songAHA butuh izin untuk membaca lagu di perangkat kamu. Atau pilih folder tertentu saja.",
            IosType.subhead, Modifier.padding(top = 4.dp, bottom = 14.dp),
            color = c.secondaryLabel, maxLines = 3,
            align = TextAlign.Center,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PillButton("Izinkan", Icons.Check, a.requestAudioPermission, Modifier.weight(1f), filled = true)
            PillButton("Folder", Icons.Folder, a.pickFolder, Modifier.weight(1f))
        }
    }
}
