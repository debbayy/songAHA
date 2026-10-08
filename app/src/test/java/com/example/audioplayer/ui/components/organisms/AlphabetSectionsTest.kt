package com.example.audioplayer.ui.components.organisms

import org.junit.Assert.assertEquals
import org.junit.Test

class AlphabetSectionsTest {

    @Test
    fun `huruf awal dipetakan ke item pertamanya`() {
        val songs = listOf("99 Luftballons", "abc", "Anak", "Bintang", "Éclair", "!Hore")
        val sections = alphabetSections(songs, firstIndex = 2) { it }
        // angka & simbol masuk "#", huruf beraksen ikut huruf dasarnya
        assertEquals(mapOf('#' to 2, 'A' to 3, 'B' to 5, 'E' to 6), sections)
    }

    @Test
    fun `grid dua kolom memakai indeks baris`() {
        val albums = listOf("Air", "Api", "Batu", "Bumi", "Cahaya")
        val sections = alphabetSections(albums, firstIndex = 1, perItem = 2) { it }
        assertEquals(mapOf('A' to 1, 'B' to 2, 'C' to 3), sections)
    }
}
