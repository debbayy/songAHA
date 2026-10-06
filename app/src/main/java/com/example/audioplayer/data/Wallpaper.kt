package com.example.audioplayer.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Ringkasan wallpaper: warna rata-rata (dasar card), warna hidup (aksen), kecerahan rata-rata,
 * seberapa kontras/"ramai" gambarnya (simpangan baku kecerahan), dan saturasi rata-rata. Semua 0..1.
 */
data class WallPalette(
    val mean: Int,
    val vibrant: Int,
    val luma: Float,
    val lumaStd: Float,
    val saturation: Float,
    val vibrantSat: Float,
)

/**
 * Wallpaper pilihan user. Gambar disalin (diperkecil) ke penyimpanan internal supaya tidak butuh
 * izin URI permanen, lalu diolah tanpa library: blur dengan box blur di bitmap kecil, cukup cepat
 * untuk HP lama dan tidak butuh RenderEffect (Android 12+).
 */
object WallpaperFile {

    private const val NAME = "wallpaper.jpg"
    private const val MAX_SIDE = 1600

    private fun file(context: Context) = File(context.filesDir, NAME)

    suspend fun import(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val cr = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0) return@runCatching false
            var sample = 1
            while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val bmp = cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) } ?: return@runCatching false
            val degrees = cr.openInputStream(uri)?.use {
                when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
            val upright = if (degrees == 0f) bmp
            else Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, Matrix().apply { postRotate(degrees) }, true)
            file(context).outputStream().use { upright.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            true
        }.getOrDefault(false)
    }

    fun delete(context: Context) {
        file(context).delete()
    }

    suspend fun load(context: Context): Bitmap? = withContext(Dispatchers.IO) {
        runCatching { BitmapFactory.decodeFile(file(context).path) }.getOrNull()
    }

    fun analyze(src: Bitmap): WallPalette {
        val bmp = Bitmap.createScaledBitmap(src, 24, 24, true)
        val hsv = FloatArray(3)
        var r = 0f; var g = 0f; var b = 0f
        var vr = 0f; var vg = 0f; var vb = 0f; var vt = 0f
        var satSum = 0f
        var lumaSum = 0f
        var lumaSq = 0f
        val n = bmp.width * bmp.height
        for (y in 0 until bmp.height) for (x in 0 until bmp.width) {
            val p = bmp.getPixel(x, y)
            r += Color.red(p); g += Color.green(p); b += Color.blue(p)
            Color.colorToHSV(p, hsv)
            // warna jenuh dengan kecerahan sedang paling cocok jadi aksen
            val w = hsv[1] * hsv[1] * (1f - abs(hsv[2] - 0.65f)) + 0.001f
            vr += Color.red(p) * w; vg += Color.green(p) * w; vb += Color.blue(p) * w; vt += w
            satSum += hsv[1]
            val l = (0.299f * Color.red(p) + 0.587f * Color.green(p) + 0.114f * Color.blue(p)) / 255f
            lumaSum += l
            lumaSq += l * l
        }
        if (bmp !== src) bmp.recycle()
        r /= n; g /= n; b /= n
        val vibrant = Color.rgb((vr / vt).roundToInt(), (vg / vt).roundToInt(), (vb / vt).roundToInt())
        Color.colorToHSV(vibrant, hsv)
        return WallPalette(
            mean = Color.rgb(r.roundToInt(), g.roundToInt(), b.roundToInt()),
            vibrant = vibrant,
            luma = lumaSum / n,
            lumaStd = sqrt((lumaSq / n - (lumaSum / n) * (lumaSum / n)).coerceAtLeast(0f)),
            saturation = satSum / n,
            vibrantSat = minOf(hsv[1], satSum / n * 2.5f),
        )
    }

    /**
     * Perkecil gambar sampai sisi terpanjang = [longSide], lalu box blur [passes] kali.
     * Tiga kali box blur ≈ gaussian blur; saat digambar ulang ke layar penuh dengan filter
     * bilinear hasilnya halus seperti material kaca iOS.
     */
    fun blurred(src: Bitmap, longSide: Int, radius: Int, passes: Int): Bitmap {
        val scale = longSide.toFloat() / max(src.width, src.height)
        val w = max(1, (src.width * scale).roundToInt())
        val h = max(1, (src.height * scale).roundToInt())
        val small = if (scale < 1f) Bitmap.createScaledBitmap(src, w, h, true) else src
        if (radius <= 0) return small
        val sw = small.width
        val sh = small.height
        val px = IntArray(sw * sh)
        small.getPixels(px, 0, sw, 0, 0, sw, sh)
        val tmp = IntArray(px.size)
        repeat(passes) {
            box(px, tmp, lines = sh, lineStep = sw, len = sw, step = 1, r = radius)
            box(tmp, px, lines = sw, lineStep = 1, len = sh, step = sw, r = radius)
        }
        return Bitmap.createBitmap(px, sw, sh, Bitmap.Config.ARGB_8888)
    }

    /** Box blur 1 dimensi dengan jendela geser (O(n), tidak tergantung radius). */
    private fun box(src: IntArray, dst: IntArray, lines: Int, lineStep: Int, len: Int, step: Int, r: Int) {
        val div = 2 * r + 1
        for (line in 0 until lines) {
            val base = line * lineStep
            fun at(i: Int) = src[base + i.coerceIn(0, len - 1) * step]
            var sr = 0; var sg = 0; var sb = 0
            for (i in -r..r) {
                val p = at(i)
                sr += p shr 16 and 0xFF; sg += p shr 8 and 0xFF; sb += p and 0xFF
            }
            for (i in 0 until len) {
                dst[base + i * step] = (0xFF shl 24) or ((sr / div) shl 16) or ((sg / div) shl 8) or (sb / div)
                val out = at(i - r)
                val inn = at(i + r + 1)
                sr += (inn shr 16 and 0xFF) - (out shr 16 and 0xFF)
                sg += (inn shr 8 and 0xFF) - (out shr 8 and 0xFF)
                sb += (inn and 0xFF) - (out and 0xFF)
            }
        }
    }
}
