// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // Samakan versi Kotlin bawaan AGP dengan plugin Compose (2.3.21); kalau beda,
    // task produceReleaseComposeMapping mencari artefak yang tidak ada.
    id("org.jetbrains.kotlin.android") version "2.3.21" apply false
}