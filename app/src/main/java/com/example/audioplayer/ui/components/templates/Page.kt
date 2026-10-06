package com.example.audioplayer.ui.components.templates

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.molecules.GlassIconButton
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos
import com.example.audioplayer.ui.theme.backdrop
import com.example.audioplayer.ui.theme.edgeFill

/**
 * Kerangka halaman iOS: Large Title yang berubah jadi judul kecil di tengah saat discroll,
 * tombol kembali & aksi berbentuk lingkaran kaca, dan efek fade di tepi atas.
 *
 * @param header ganti Large Title dengan header custom (mis. cover album).
 * @param immersive header boleh menembus status bar (halaman artis).
 */
@Composable
fun Page(
    title: String,
    modifier: Modifier = Modifier,
    background: Color = LocalIos.current.background,
    actions: @Composable RowScope.() -> Unit = {},
    header: (@Composable () -> Unit)? = null,
    state: LazyListState = rememberLazyListState(),
    content: LazyListScope.() -> Unit,
) {
    val c = LocalIos.current
    val nav = LocalActions.current.nav
    // dihitung sekali saat halaman muncul, supaya tombol back tidak berkedip saat animasi pindah
    val canGoBack = remember { nav.stack().size > 1 }
    val threshold = with(LocalDensity.current) { 48.dp.toPx() }
    val scrolled by remember {
        derivedStateOf { state.firstVisibleItemIndex > 0 || state.firstVisibleItemScrollOffset > threshold }
    }
    val barAlpha by animateFloatAsState(if (scrolled) 1f else 0f, tween(180), label = "bar")

    // backdrop: dengan wallpaper, tiap halaman menggambar wallpaper sendiri supaya halaman
    // yang sedang bergeser saat navigasi tetap menutupi halaman di belakangnya
    Box(modifier.fillMaxSize().backdrop(frosted = false).background(background)) {
        LazyColumn(Modifier.fillMaxSize(), state = state, contentPadding = PaddingValues(bottom = 190.dp)) {
            item(key = "__header") {
                if (header != null) header()
                else Txt(
                    title, IosType.largeTitle,
                    Modifier.statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 6.dp),
                )
            }
            content()
        }

        // Efek tepi atas (scroll edge) + judul kecil
        Box(
            Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = barAlpha }
                .edgeFill(background, 0f to 1f, 0.6f to 0.92f, 1f to 0f)
                .statusBarsPadding()
                .height(76.dp)
        )
        Box(Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 12.dp)) {
            if (canGoBack) {
                GlassIconButton(Icons.ChevronLeft, onClick = { nav.pop() }, Modifier.align(Alignment.CenterStart))
            }
            Txt(
                title, IosType.headline,
                Modifier.align(Alignment.Center).padding(horizontal = 64.dp).graphicsLayer { alpha = barAlpha },
                color = c.label,
            )
            Row(
                Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
    }
}
