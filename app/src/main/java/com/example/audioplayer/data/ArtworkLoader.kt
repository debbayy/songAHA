package com.example.audioplayer.data

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.os.Build
import android.provider.MediaStore
import android.util.LruCache
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import java.util.Collections

/**
 * Pemuat cover album ringan (pengganti Coil/Glide): LruCache di memori + decode dengan sampling.
 * Ukuran dibulatkan ke beberapa "bucket" supaya cache tidak menyimpan banyak varian.
 */
object ArtworkLoader {

    private val cache = object : LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()
    ) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount / 1024
    }
    private val misses = Collections.synchronizedSet(HashSet<String>())
    private val colors = LruCache<String, Int>(256)
    private val tinies = LruCache<String, Bitmap>(16)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val io = Dispatchers.IO.limitedParallelism(3)

    private fun bucket(px: Int) = when {
        px <= 96 -> 96
        px <= 200 -> 200
        px <= 420 -> 420
        else -> 800
    }

    private fun key(song: Song, px: Int) = "${song.artKey}@${bucket(px)}"

    fun cached(song: Song, px: Int): Bitmap? = cache.get(key(song, px))

    suspend fun load(context: Context, song: Song, px: Int): Bitmap? {
        val size = bucket(px)
        val k = key(song, px)
        cache.get(k)?.let { return it }
        if (song.artKey in misses) return null
        return withContext(io) {
            cache.get(k) ?: decode(context.applicationContext, song, size)?.also { cache.put(k, it) }
                ?: run { misses += song.artKey; null }
        }
    }

    /** Warna dominan (cenderung yang hidup/vibrant) untuk mewarnai halaman. */
    suspend fun dominantColor(context: Context, song: Song): Int? {
        colors.get(song.artKey)?.let { return it }
        val bmp = load(context, song, 96) ?: return null
        return withContext(Dispatchers.Default) {
            pickColor(bmp).also { colors.put(song.artKey, it) }
        }
    }

    /** Cover 8x8 px: kalau di-stretch dengan filter bilinear hasilnya seperti blur, murah di HP lama. */
    suspend fun tiny(context: Context, song: Song): Bitmap? {
        tinies.get(song.artKey)?.let { return it }
        val bmp = load(context, song, 96) ?: return null
        return withContext(Dispatchers.Default) {
            Bitmap.createScaledBitmap(bmp, 8, 8, true).also { tinies.put(song.artKey, it) }
        }
    }

    private fun decode(context: Context, song: Song, px: Int): Bitmap? {
        if (song.albumId > 0) {
            val fromAlbum = if (Build.VERSION.SDK_INT >= 29) {
                runCatching {
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, song.albumId
                    )
                    context.contentResolver.loadThumbnail(uri, Size(px, px), null)
                }.getOrNull()
            } else {
                song.artworkUri?.let { uri ->
                    runCatching {
                        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                        if (bounds.outWidth <= 0) return@runCatching null
                        val opts = BitmapFactory.Options().apply { inSampleSize = sample(bounds.outWidth, bounds.outHeight, px) }
                        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                    }.getOrNull()
                }
            }
            if (fromAlbum != null) return fromAlbum
        }
        return embedded(context, song, px)
    }

    private fun embedded(context: Context, song: Song, px: Int): Bitmap? {
        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(context, song.uri)
            val bytes = mmr.embeddedPicture ?: return null
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            val opts = BitmapFactory.Options().apply { inSampleSize = sample(bounds.outWidth, bounds.outHeight, px) }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        } catch (e: Exception) {
            null
        } finally {
            runCatching { mmr.release() }
        }
    }

    private fun sample(w: Int, h: Int, target: Int): Int {
        var s = 1
        while (w / (s * 2) >= target && h / (s * 2) >= target) s *= 2
        return s
    }

    private fun pickColor(src: Bitmap): Int {
        val bmp = Bitmap.createScaledBitmap(src, 16, 16, true)
        val hsv = FloatArray(3)
        var r = 0f; var g = 0f; var b = 0f; var total = 0f
        for (y in 0 until bmp.height) for (x in 0 until bmp.width) {
            val p = bmp.getPixel(x, y)
            Color.colorToHSV(p, hsv)
            // utamakan warna jenuh dengan kecerahan sedang
            val w = 0.15f + hsv[1] * (1f - kotlin.math.abs(hsv[2] - 0.6f))
            r += Color.red(p) * w; g += Color.green(p) * w; b += Color.blue(p) * w; total += w
        }
        if (bmp !== src) bmp.recycle()
        return Color.rgb((r / total).toInt(), (g / total).toInt(), (b / total).toInt())
    }
}
