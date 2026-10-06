package com.example.audioplayer.data.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LrcParserTest {

    @Test
    fun `baris bertimestamp diurutkan dan tag info diabaikan`() {
        val lyrics = LrcParser.parse(
            """
            [ar:Penyanyi]
            [ti:Judul]
            [00:20.00]Kedua
            [00:05.5]Pertama
            """.trimIndent()
        )!!
        assertTrue(lyrics.synced)
        assertEquals(listOf(5_500L to "Pertama", 20_000L to "Kedua"), lyrics.lines.map { it.timeMs to it.text })
    }

    @Test
    fun `satu baris dengan beberapa timestamp jadi beberapa baris`() {
        val lyrics = LrcParser.parse("[00:10.00][01:10.00]Reff")!!
        assertEquals(listOf(10_000L, 70_000L), lyrics.lines.map { it.timeMs })
    }

    @Test
    fun `offset menggeser waktu dan penanda per kata dibuang`() {
        val lyrics = LrcParser.parse("[offset:+500]\n[00:02.000]<00:02.00>Halo <00:02.50>dunia")!!
        assertEquals(1_500L, lyrics.lines.single().timeMs)
        assertEquals("Halo dunia", lyrics.lines.single().text)
    }

    @Test
    fun `teks tanpa timestamp jadi lirik biasa`() {
        val lyrics = LrcParser.parse("\nBait satu\n\nBait dua\n")!!
        assertFalse(lyrics.synced)
        assertEquals(listOf("Bait satu", "", "Bait dua"), lyrics.lines.map { it.text })
    }

    @Test
    fun `BOM di awal file diabaikan`() {
        val lyrics = LrcParser.parse(Char(0xFEFF) + "[00:01.00]Halo")!!
        assertEquals(1_000L, lyrics.lines.single().timeMs)
    }

    @Test
    fun `teks kosong bukan lirik`() {
        assertNull(LrcParser.parse("[ar:Penyanyi]\n\n"))
    }

    @Test
    fun `indexAt mencari baris yang sedang dinyanyikan`() {
        val lyrics = LrcParser.parse("[00:01.00]A\n[00:03.00]B\n[00:05.00]C")!!
        assertEquals(-1, lyrics.indexAt(500))
        assertEquals(0, lyrics.indexAt(1_000))
        assertEquals(1, lyrics.indexAt(4_999))
        assertEquals(2, lyrics.indexAt(60_000))
    }
}
