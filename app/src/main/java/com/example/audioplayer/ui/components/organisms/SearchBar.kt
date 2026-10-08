package com.example.audioplayer.ui.components.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.NavViewModel
import com.example.audioplayer.ui.components.atoms.Ico
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.foundation.bounce
import com.example.audioplayer.ui.components.foundation.glass
import com.example.audioplayer.ui.components.foundation.noRippleClick
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun SearchBar(nav: NavViewModel) {
    val c = LocalIos.current
    val focus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { focus.requestFocus() }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(54.dp).glass(CircleShape).bounce { nav.select(nav.previousTab) }, Alignment.Center) {
            Ico(nav.previousTab.icon, c.label, size = 24.dp)
        }
        Row(
            Modifier.weight(1f).height(54.dp).glass(CircleShape).padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Ico(Icons.Search, c.secondaryLabel, size = 20.dp)
            BasicTextField(
                value = nav.query,
                onValueChange = { nav.query = it },
                singleLine = true,
                // tombol "Cari" di keyboard menutup keyboard supaya hasil terlihat penuh
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    keyboard?.hide()
                    focusManager.clearFocus()
                }),
                textStyle = IosType.body.copy(color = c.label),
                cursorBrush = SolidColor(c.accent),
                modifier = Modifier.weight(1f).padding(start = 8.dp).focusRequester(focus),
                decorationBox = { inner ->
                    Box {
                        if (nav.query.isEmpty()) Txt("Lagu, album, artis", IosType.body, color = c.secondaryLabel)
                        inner()
                    }
                },
            )
            if (nav.query.isNotEmpty()) {
                Box(Modifier.size(36.dp).noRippleClick { nav.query = "" }, Alignment.Center) {
                    Ico(Icons.CloseCircle, c.tertiaryLabel, size = 20.dp)
                }
            }
        }
    }
}
