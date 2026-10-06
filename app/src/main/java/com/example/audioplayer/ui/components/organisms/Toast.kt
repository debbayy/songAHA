package com.example.audioplayer.ui.components.organisms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.glass
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos
import kotlinx.coroutines.delay

/** HUD konfirmasi singkat di tengah layar (mis. "Ditambahkan ke Playlist"). */
@Composable
fun BoxScope.ToastHost(message: String?, onDone: () -> Unit) {
    val c = LocalIos.current
    var last by remember { mutableStateOf(message) }
    if (message != null) last = message
    LaunchedEffect(message) {
        if (message != null) {
            delay(1400)
            onDone()
        }
    }
    AnimatedVisibility(
        message != null,
        Modifier.align(Alignment.Center),
        enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.85f),
        exit = fadeOut(tween(250)) + scaleOut(targetScale = 0.9f),
    ) {
        Column(
            Modifier.width(170.dp).glass(RoundedCornerShape(22.dp)).padding(vertical = 22.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Ico(Icons.Check, c.label, size = 40.dp)
            Spacer(Modifier.height(8.dp))
            Txt(last.orEmpty(), IosType.subhead, align = TextAlign.Center, maxLines = 3)
        }
    }
}
