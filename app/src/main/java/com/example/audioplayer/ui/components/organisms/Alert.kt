package com.example.audioplayer.ui.components.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.audioplayer.ui.components.atoms.Hairline
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.pressable
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos
import kotlinx.coroutines.delay

data class AlertSpec(
    val title: String,
    val message: String? = null,
    val input: Boolean = true,
    val initial: String = "",
    val placeholder: String = "",
    val confirm: String = "OK",
    val destructive: Boolean = false,
    val onConfirm: (String) -> Unit,
)

/** Alert iOS (opsional dengan kolom teks), dipakai untuk buat/ubah nama playlist & konfirmasi. */
@Composable
fun AlertHost(spec: AlertSpec?, onDismiss: () -> Unit) {
    if (spec == null) return
    val c = LocalIos.current
    Dialog(onDismissRequest = onDismiss) {
        var text by remember(spec) { mutableStateOf(TextFieldValue(spec.initial, TextRange(spec.initial.length))) }
        val focus = remember { FocusRequester() }
        val canConfirm = !spec.input || text.text.isNotBlank()
        Column(Modifier.width(280.dp).clip(RoundedCornerShape(16.dp)).background(c.elevated)) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Txt(spec.title, IosType.headline, align = TextAlign.Center, maxLines = 2)
                spec.message?.let {
                    Txt(it, IosType.footnote, Modifier.padding(top = 4.dp), align = TextAlign.Center, maxLines = 5)
                }
                if (spec.input) {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        singleLine = true,
                        textStyle = IosType.subhead.copy(color = c.label),
                        cursorBrush = SolidColor(c.accent),
                        modifier = Modifier
                            .padding(top = 14.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(c.fill)
                            .padding(horizontal = 10.dp, vertical = 9.dp)
                            .focusRequester(focus),
                        decorationBox = { inner ->
                            Box {
                                if (text.text.isEmpty()) Txt(spec.placeholder, IosType.subhead, color = c.tertiaryLabel)
                                inner()
                            }
                        },
                    )
                    LaunchedEffect(spec) {
                        delay(150)
                        focus.requestFocus()
                    }
                }
            }
            Hairline()
            Row(Modifier.height(46.dp)) {
                Box(Modifier.weight(1f).fillMaxHeight().pressable(onClick = onDismiss), Alignment.Center) {
                    Txt("Batal", IosType.body, color = c.accent)
                }
                Box(Modifier.width(0.5.dp).fillMaxHeight().background(c.separator))
                Box(
                    Modifier.weight(1f).fillMaxHeight().pressable {
                        if (canConfirm) {
                            onDismiss()
                            spec.onConfirm(text.text.trim())
                        }
                    },
                    Alignment.Center,
                ) {
                    Txt(
                        spec.confirm, IosType.headline,
                        color = when {
                            !canConfirm -> c.tertiaryLabel
                            spec.destructive -> c.red
                            else -> c.accent
                        },
                    )
                }
            }
        }
    }
}
