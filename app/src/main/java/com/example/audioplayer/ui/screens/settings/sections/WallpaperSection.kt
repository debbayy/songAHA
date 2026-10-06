package com.example.audioplayer.ui.screens.settings.sections

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.InsetHairline
import com.example.audioplayer.ui.components.atoms.Txt
import com.example.audioplayer.ui.components.molecules.ActionRow
import com.example.audioplayer.ui.components.molecules.SettingsGroup
import com.example.audioplayer.ui.components.molecules.SliderRow
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos
import com.example.audioplayer.ui.theme.LocalWallpaper

/** Wallpaper sendiri: gambar dibuat redup & buram ala iOS, card dan aksen ikut warnanya. */
@Composable
fun WallpaperSection() {
    val a = LocalActions.current
    val settings by a.vm.store.settings.collectAsState()
    val active = settings.wallpaper != 0L

    SettingsGroup(
        "LATAR BELAKANG",
        footer = if (active) "Kontras, kecerahan, dan warna gambar diatur otomatis supaya nyaman di mata dan teks selalu terbaca."
        else "Pakai foto sendiri sebagai latar. Warna card, kaca, dan aksen akan mengikuti gambar.",
    ) {
        if (!active) {
            ActionRow("Pilih Gambar…", Icons.Photo, onClick = a.pickWallpaper)
            return@SettingsGroup
        }
        WallpaperPreview()
        InsetHairline()
        SliderRow("Blur", settings.wallBlur) { v -> a.vm.store.updateSettings { it.copy(wallBlur = v) } }
        InsetHairline()
        ActionRow("Ganti Gambar…", Icons.Photo, onClick = a.pickWallpaper)
        InsetHairline()
        ActionRow("Hapus Gambar", Icons.Trash, destructive = true) { a.vm.clearWallpaper() }
    }
}

/** Miniatur layar dengan wallpaper aktif + titik-titik warna yang diambil dari gambar. */
@Composable
private fun WallpaperPreview() {
    val c = LocalIos.current
    val wallpaper = LocalWallpaper.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(width = 54.dp, height = 96.dp).clip(RoundedCornerShape(10.dp)).background(c.fill)) {
            if (wallpaper != null) {
                Image(
                    wallpaper.background, null, Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop, colorFilter = wallpaper.filter,
                )
                Box(Modifier.fillMaxSize().background(wallpaper.scrim))
                Box(Modifier.align(Alignment.Center).size(38.dp, 14.dp).clip(RoundedCornerShape(5.dp)).background(c.glassFill))
            }
        }
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Txt("Gambar Sendiri", IosType.body)
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(c.accent, c.cell.copy(alpha = 1f), c.placeholderTop).forEach { swatch ->
                    Box(Modifier.size(18.dp).clip(CircleShape).background(swatch))
                }
            }
        }
    }
}
