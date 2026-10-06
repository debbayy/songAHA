package com.example.audioplayer.ui.components.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.components.foundation.noRippleClick
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

/** Segmented control iOS. */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalIos.current
    Row(
        modifier.height(32.dp).clip(RoundedCornerShape(9.dp)).background(c.fill).padding(2.dp)
    ) {
        options.forEachIndexed { i, label ->
            val sel = i == selected
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(
                        if (sel) Modifier
                            .shadow(2.dp, RoundedCornerShape(7.dp))
                            .background(if (c.isDark) Color(0xFF636366) else Color.White, RoundedCornerShape(7.dp))
                        else Modifier
                    )
                    .noRippleClick { onSelect(i) },
                Alignment.Center,
            ) {
                Txt(
                    label,
                    IosType.footnote.copy(fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Medium),
                )
            }
        }
    }
}
