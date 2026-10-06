package com.example.audioplayer.ui.components.atoms

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun Txt(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalIos.current.label,
    maxLines: Int = 1,
    align: TextAlign = TextAlign.Start,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(color = color, textAlign = align),
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines,
    )
}
