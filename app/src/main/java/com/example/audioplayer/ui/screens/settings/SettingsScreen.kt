package com.example.audioplayer.ui.screens.settings

import androidx.compose.runtime.Composable
import com.example.audioplayer.ui.components.templates.Page
import com.example.audioplayer.ui.screens.settings.sections.AboutSection
import com.example.audioplayer.ui.screens.settings.sections.AppearanceSection
import com.example.audioplayer.ui.screens.settings.sections.LibraryFilterSection
import com.example.audioplayer.ui.screens.settings.sections.LibrarySection
import com.example.audioplayer.ui.screens.settings.sections.SoundSection
import com.example.audioplayer.ui.screens.settings.sections.WallpaperSection
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun SettingsScreen() {
    Page("Pengaturan", background = LocalIos.current.groupedBackground) {
        item { AppearanceSection() }
        item { WallpaperSection() }
        item { SoundSection() }
        item { LibrarySection() }
        item { LibraryFilterSection() }
        item { AboutSection() }
    }
}
