package com.example.audioplayer.data.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class EmbeddedLyricsReaderTest {

    private val lrc = "[00:01.00]Halo dunia"

    @Test
    fun `mp3 ID3v2_3 USLT setelah frame lain`() {
        val title = frame("TIT2", byteArrayOf(0) + "Judul".toByteArray())
        val uslt = frame("USLT", byteArrayOf(3) + "ind".toByteArray() + byteArrayOf(0) + lrc.toByteArray())
        assertEquals(lrc, read(id3(version = 3, frames = title + uslt)))
    }

    @Test
    fun `mp3 ID3v2_4 USLT UTF-16 dengan deskripsi`() {
        val text = byteArrayOf(1) + "eng".toByteArray() +
            "desc".toByteArray(Charsets.UTF_16) + byteArrayOf(0, 0) + lrc.toByteArray(Charsets.UTF_16)
        assertEquals(lrc, read(id3(version = 4, frames = frame("USLT", text, synchsafe = true))))
    }

    @Test
    fun `flac vorbis comment LYRICS setelah blok lain`() {
        val comments = vorbisComments("TITLE=Judul", "LYRICS=$lrc")
        val out = ByteArrayOutputStream()
        out.write("fLaC".toByteArray())
        out.write(flacBlock(type = 0, body = ByteArray(34), last = false))  // STREAMINFO
        out.write(flacBlock(type = 6, body = ByteArray(5000), last = false)) // PICTURE (dilewati)
        out.write(flacBlock(type = 4, body = comments, last = true))
        assertEquals(lrc, read(out.toByteArray()))
    }

    @Test
    fun `m4a atom lyr di moov setelah mdat`() {
        val data = atom("data", ByteArray(8) + lrc.toByteArray())
        val ilst = atom("ilst", atom("©lyr", data))
        val meta = atom("meta", ByteArray(4) + ilst)
        val moov = atom("moov", atom("udta", meta))
        val file = atom("ftyp", "M4A ".toByteArray() + ByteArray(4)) + atom("mdat", ByteArray(10_000)) + moov
        assertEquals(lrc, read(file))
    }

    @Test
    fun `file tanpa lirik`() {
        assertNull(read(id3(version = 3, frames = frame("TIT2", byteArrayOf(0) + "Judul".toByteArray()))))
        assertNull(read("RIFF....WAVE".toByteArray()))
    }

    // ---- Pembuat file sintetis ---------------------------------------------------------------

    private fun read(bytes: ByteArray) = EmbeddedLyricsReader.read(bytes.inputStream())

    private fun id3(version: Int, frames: ByteArray): ByteArray {
        val padding = ByteArray(32)
        val size = frames.size + padding.size
        return "ID3".toByteArray() + byteArrayOf(version.toByte(), 0, 0) + synchsafe(size) + frames + padding
    }

    private fun frame(id: String, body: ByteArray, synchsafe: Boolean = false): ByteArray =
        id.toByteArray() + (if (synchsafe) synchsafe(body.size) else int(body.size)) + byteArrayOf(0, 0) + body

    private fun synchsafe(v: Int) = byteArrayOf(
        (v shr 21 and 0x7F).toByte(), (v shr 14 and 0x7F).toByte(), (v shr 7 and 0x7F).toByte(), (v and 0x7F).toByte(),
    )

    private fun int(v: Int) = ByteBuffer.allocate(4).putInt(v).array()

    private fun vorbisComments(vararg comments: String): ByteArray {
        val vendor = "test".toByteArray()
        val buf = ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN)
        buf.putInt(vendor.size).put(vendor).putInt(comments.size)
        comments.forEach { val b = it.toByteArray(); buf.putInt(b.size).put(b) }
        return buf.array().copyOf(buf.position())
    }

    private fun flacBlock(type: Int, body: ByteArray, last: Boolean): ByteArray =
        byteArrayOf(
            ((if (last) 0x80 else 0) or type).toByte(),
            (body.size shr 16).toByte(), (body.size shr 8).toByte(), body.size.toByte(),
        ) + body

    private fun atom(type: String, body: ByteArray) = int(body.size + 8) + type.toByteArray(Charsets.ISO_8859_1) + body
}
