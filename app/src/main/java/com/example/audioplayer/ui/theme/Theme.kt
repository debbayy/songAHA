package com.example.audioplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.audioplayer.data.AppSettings

/** Palet warna sistem iOS (label, fill, separator, dst.) + material kaca. */
@Immutable
data class IosColors(
    val isDark: Boolean,
    val background: Color,
    val groupedBackground: Color,
    val cell: Color,
    val elevated: Color,
    val label: Color,
    val secondaryLabel: Color,
    val tertiaryLabel: Color,
    val separator: Color,
    val fill: Color,
    val pressed: Color,
    val accent: Color,
    val red: Color,
    val placeholderTop: Color,
    val placeholderBottom: Color,
    val glassFill: Color,
    val glassSheen: Color,
    val glassEdgeTop: Color,
    val glassEdgeBottom: Color,
)

fun iosColors(dark: Boolean, tinted: Boolean): IosColors =
    if (!dark) IosColors(
        isDark = false,
        background = Color.White,
        groupedBackground = Color(0xFFF2F2F7),
        cell = Color.White,
        elevated = Color(0xFFF9F9F9),
        label = Color.Black,
        secondaryLabel = Color(0x993C3C43),
        tertiaryLabel = Color(0x4D3C3C43),
        separator = Color(0x4A3C3C43),
        fill = Color(0x1F767680),
        pressed = Color(0x14000000),
        accent = Color(0xFFFA2D48),
        red = Color(0xFFFF3B30),
        placeholderTop = Color(0xFFE9E9EE),
        placeholderBottom = Color(0xFFD1D1D6),
        glassFill = if (tinted) Color(0xF5F7F7F8) else Color(0xB8FFFFFF),
        glassSheen = if (tinted) Color(0x33FFFFFF) else Color(0x80FFFFFF),
        glassEdgeTop = Color(0xE6FFFFFF),
        glassEdgeBottom = Color(0x1A000000),
    ) else IosColors(
        isDark = true,
        background = Color.Black,
        groupedBackground = Color.Black,
        cell = Color(0xFF1C1C1E),
        elevated = Color(0xFF2C2C2E),
        label = Color.White,
        secondaryLabel = Color(0x99EBEBF5),
        tertiaryLabel = Color(0x4DEBEBF5),
        separator = Color(0x99545458),
        fill = Color(0x3D767680),
        pressed = Color(0x1FFFFFFF),
        accent = Color(0xFFFF375F),
        red = Color(0xFFFF453A),
        placeholderTop = Color(0xFF3A3A3C),
        placeholderBottom = Color(0xFF242426),
        glassFill = if (tinted) Color(0xF21C1C1E) else Color(0xA82C2C2E),
        glassSheen = if (tinted) Color(0x0FFFFFFF) else Color(0x24FFFFFF),
        glassEdgeTop = Color(0x4DFFFFFF),
        glassEdgeBottom = Color(0x14FFFFFF),
    )

val LocalIos = staticCompositionLocalOf { iosColors(dark = false, tinted = false) }

/**
 * Skala tipografi iOS (Large Title, Headline, Body, ...). Font SF Pro tidak boleh dibundel,
 * jadi pakai font sistem dengan letter-spacing sedikit dirapatkan supaya mirip.
 */
object IosType {
    val largeTitle = TextStyle(fontSize = 34.sp, lineHeight = 41.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp)
    val title1 = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp)
    val title2 = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp)
    val title3 = TextStyle(fontSize = 20.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp)
    val headline = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp)
    val body = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, letterSpacing = (-0.3).sp)
    val callout = TextStyle(fontSize = 16.sp, lineHeight = 21.sp, letterSpacing = (-0.2).sp)
    val subhead = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = (-0.2).sp)
    val footnote = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = (-0.1).sp)
    val caption = TextStyle(fontSize = 12.sp, lineHeight = 16.sp)
    val caption2 = TextStyle(fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.1.sp)
}

@Composable
fun AppTheme(settings: AppSettings, content: @Composable () -> Unit) {
    val dark = when (settings.theme) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    val colors = remember(dark, settings.glassTinted) { iosColors(dark, settings.glassTinted) }
    CompositionLocalProvider(LocalIos provides colors, content = content)
}
