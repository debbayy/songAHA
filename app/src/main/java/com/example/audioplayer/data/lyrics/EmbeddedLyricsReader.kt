package com.example.audioplayer.data.lyrics

import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.EOFException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

/**
 * Membaca lirik yang tertanam di tag file audio, tanpa library:
 * - MP3  : frame ID3v2 `USLT`
 * - FLAC : Vorbis comment `LYRICS` / `UNSYNCEDLYRICS`
 * - OGG/Opus : sama seperti FLAC (Vorbis comment)
 * - M4A  : atom `©lyr`
 *
 * File dibaca berurutan dan bagian yang tidak perlu (cover, audio) dilewati dengan `skip`,
 * jadi hemat memori walau cover-nya besar.
 */
internal object EmbeddedLyricsReader {

    private const val MAX_TAG_BYTES = 8 * 1024 * 1024

    fun read(input: InputStream): String? = runCatching {
        val stream = DataInputStream(BufferedInputStream(input, 16 * 1024))
        stream.mark(12)
        val magic = ByteArray(12).also { stream.readFully(it) }
        stream.reset()
        when {
            magic.startsWith("ID3") -> readId3(stream)
            magic.startsWith("fLaC") -> readFlac(stream)
            magic.startsWith("OggS") -> readOgg(stream)
            String(magic, 4, 4, Charsets.ISO_8859_1) == "ftyp" -> readMp4(stream)
            else -> null
        }
    }.getOrNull()?.takeIf { it.isNotBlank() }

    // ---- MP3 (ID3v2) -------------------------------------------------------------------------

    private fun readId3(s: DataInputStream): String? {
        s.skipFully(3)
        val version = s.readUnsignedByte()
        s.skipFully(1)
        val flags = s.readUnsignedByte()
        var remaining = synchsafe(s.readInt())
        if (flags and 0x40 != 0) { // extended header
            val size = s.readInt()
            val extSize = if (version >= 4) synchsafe(size) - 4 else size
            s.skipFully(extSize); remaining -= extSize + 4
        }
        val idLength = if (version == 2) 3 else 4
        val headerLength = if (version == 2) 6 else 10
        val lyricsId = if (version == 2) "ULT" else "USLT"

        while (remaining > headerLength) {
            val id = String(ByteArray(idLength).also { s.readFully(it) }, Charsets.ISO_8859_1)
            if (id[0] == '\u0000') return null // padding: tidak ada frame lagi
            val size = when (version) {
                2 -> (s.readUnsignedByte() shl 16) or (s.readUnsignedByte() shl 8) or s.readUnsignedByte()
                3 -> s.readInt()
                else -> synchsafe(s.readInt())
            }
            if (version != 2) s.skipFully(2) // flag frame
            remaining -= headerLength + size
            if (size <= 0 || size > MAX_TAG_BYTES) return null
            if (id == lyricsId) return decodeUslt(ByteArray(size).also { s.readFully(it) })
            s.skipFully(size)
        }
        return null
    }

    /** Isi USLT: [encoding][bahasa 3 byte][deskripsi + terminator][teks lirik] */
    private fun decodeUslt(body: ByteArray): String? {
        if (body.size < 5) return null
        val charset = id3Charset(body[0].toInt())
        val wide = body[0].toInt() == 1 || body[0].toInt() == 2
        var i = 4
        // lewati deskripsi sampai terminator (1 byte nol, atau 2 byte nol untuk UTF-16)
        if (wide) {
            while (i + 1 < body.size && !(body[i].toInt() == 0 && body[i + 1].toInt() == 0)) i += 2
            i += 2
        } else {
            while (i < body.size && body[i].toInt() != 0) i++
            i += 1
        }
        if (i >= body.size) return null
        return String(body, i, body.size - i, charset).trimEnd('\u0000')
    }

    private fun id3Charset(encoding: Int): Charset = when (encoding) {
        1 -> Charsets.UTF_16
        2 -> Charsets.UTF_16BE
        3 -> Charsets.UTF_8
        else -> Charsets.ISO_8859_1
    }

    private fun synchsafe(v: Int) =
        (v and 0x7F) or ((v shr 8 and 0x7F) shl 7) or ((v shr 16 and 0x7F) shl 14) or ((v shr 24 and 0x7F) shl 21)

    // ---- FLAC --------------------------------------------------------------------------------

    private fun readFlac(s: DataInputStream): String? {
        s.skipFully(4)
        while (true) {
            val header = s.readUnsignedByte()
            val isLast = header and 0x80 != 0
            val type = header and 0x7F
            val length = (s.readUnsignedByte() shl 16) or (s.readUnsignedByte() shl 8) or s.readUnsignedByte()
            if (type == 4 && length <= MAX_TAG_BYTES) {
                return vorbisLyrics(ByteArray(length).also { s.readFully(it) })
            }
            s.skipFully(length)
            if (isLast) return null
        }
    }

    // ---- OGG / Opus --------------------------------------------------------------------------

    /**
     * Paket pertama = identitas codec, paket kedua = komentar (tag). Isi halaman OGG dibaca
     * sampai dua paket itu lengkap; nilai lacing < 255 menandai akhir sebuah paket.
     */
    private fun readOgg(s: DataInputStream): String? {
        val packets = ByteArrayOutputStream()
        var completed = 0
        while (completed < 2 && packets.size() < MAX_TAG_BYTES) {
            val capture = ByteArray(4).also { s.readFully(it) }
            if (!capture.startsWith("OggS")) return null
            s.skipFully(22) // versi, flag, granule, serial, nomor halaman, CRC
            val lacing = IntArray(s.readUnsignedByte()) { s.readUnsignedByte() }
            completed += lacing.count { it < 255 }
            packets.write(ByteArray(lacing.sum()).also { s.readFully(it) })
        }
        val data = packets.toByteArray()
        return findKey(data)?.let { (start, length) -> String(data, start, length, Charsets.UTF_8) }
    }

    /** Cari "LYRICS=" (atau "UNSYNCEDLYRICS=") beserta panjang komentarnya (4 byte LE sebelumnya). */
    private fun findKey(data: ByteArray): Pair<Int, Int>? {
        for (key in listOf("LYRICS=", "UNSYNCEDLYRICS=")) {
            val keyBytes = key.toByteArray(Charsets.ISO_8859_1)
            var i = 4
            while (i <= data.size - keyBytes.size) {
                if (data.matchesIgnoreCase(i, keyBytes)) {
                    val length = ByteBuffer.wrap(data, i - 4, 4).order(ByteOrder.LITTLE_ENDIAN).int
                    val valueStart = i + keyBytes.size
                    val valueLength = length - keyBytes.size
                    if (valueLength > 0 && valueStart + valueLength <= data.size) return valueStart to valueLength
                }
                i++
            }
        }
        return null
    }

    /** Blok Vorbis comment: [vendor][jumlah][panjang + "KUNCI=nilai"]... (little endian) */
    private fun vorbisLyrics(block: ByteArray): String? {
        val buf = ByteBuffer.wrap(block).order(ByteOrder.LITTLE_ENDIAN)
        val vendorLength = buf.int
        buf.position(buf.position() + vendorLength)
        repeat(buf.int) {
            val length = buf.int
            val comment = String(block, buf.position(), length, Charsets.UTF_8)
            buf.position(buf.position() + length)
            val key = comment.substringBefore('=').uppercase()
            if (key == "LYRICS" || key == "UNSYNCEDLYRICS") return comment.substringAfter('=')
        }
        return null
    }

    // ---- M4A (MP4) ---------------------------------------------------------------------------

    /** Lewati atom tingkat atas (termasuk `mdat`) sampai ketemu `moov`, lalu cari di dalamnya. */
    private fun readMp4(s: DataInputStream): String? {
        while (true) {
            val (type, bodyLength) = s.readAtomHeader() ?: return null
            if (type == "moov") {
                if (bodyLength > MAX_TAG_BYTES) return null
                val moov = ByteArray(bodyLength.toInt()).also { s.readFully(it) }
                return findLyricsAtom(ByteBuffer.wrap(moov), path = listOf("udta", "meta", "ilst", "©lyr", "data"))
            }
            s.skipFully(bodyLength)
        }
    }

    private fun findLyricsAtom(buf: ByteBuffer, path: List<String>): String? {
        while (buf.remaining() >= 8) {
            val size = buf.int
            val type = ByteArray(4).also { buf.get(it) }.toString(Charsets.ISO_8859_1)
            val bodyLength = size - 8
            if (bodyLength < 0 || bodyLength > buf.remaining()) return null
            val body = buf.slice().also { it.limit(bodyLength) }
            buf.position(buf.position() + bodyLength)
            if (type != path.first()) continue
            if (path.size == 1) { // atom data: [tipe 4][locale 4][teks]
                body.position(8)
                return ByteArray(body.remaining()).also { body.get(it) }.toString(Charsets.UTF_8)
            }
            if (type == "meta") body.position(4) // meta adalah "full box" (versi + flag)
            return findLyricsAtom(body, path.drop(1))
        }
        return null
    }

    private fun DataInputStream.readAtomHeader(): Pair<String, Long>? {
        val size = try { readInt().toLong() and 0xFFFFFFFFL } catch (e: EOFException) { return null }
        val type = ByteArray(4).also { readFully(it) }.toString(Charsets.ISO_8859_1)
        val body = when (size) {
            1L -> readLong() - 16 // ukuran 64-bit
            0L -> return null     // atom sampai akhir file: pasti bukan moov yang kita cari
            else -> size - 8
        }
        return type to body
    }

    // ---- Utilitas ----------------------------------------------------------------------------

    private fun ByteArray.startsWith(prefix: String) =
        size >= prefix.length && prefix.indices.all { this[it] == prefix[it].code.toByte() }

    private fun ByteArray.matchesIgnoreCase(at: Int, key: ByteArray) =
        key.indices.all { (this[at + it].toInt().toChar()).uppercaseChar() == key[it].toInt().toChar() }

    private fun DataInputStream.skipFully(count: Number) {
        var left = count.toLong()
        while (left > 0) {
            val skipped = skip(left)
            if (skipped <= 0) {
                if (read() == -1) throw EOFException()
                left--
            } else {
                left -= skipped
            }
        }
    }
}
