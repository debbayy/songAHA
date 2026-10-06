package com.example.audioplayer.ui.theme

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.audioplayer.data.AppSettings
import com.example.audioplayer.data.WallPalette
import com.example.audioplayer.data.WallpaperFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt
import android.graphics.Color as AColor

/**
 * Wallpaper aktif yang sudah siap digambar.
 *
 * Cara kerjanya meniru iOS: gambar latar dibuat tidak terlalu kontras (kontras diturunkan ke arah
 * warna rata-ratanya + lapisan gelap/terang otomatis sesuai kecerahan gambar), lalu setiap card
 * menggambar ulang potongan wallpaper yang sudah diblur pekat tepat di belakangnya. Hasilnya card
 * terlihat seperti kaca buram yang warnanya ikut gambar di belakangnya, tanpa blur real-time.
 */
@Stable
class Wallpaper(
    val background: ImageBitmap,
    val frost: ImageBitmap,
    val filter: ColorFilter,
    val scrim: Color,
    val palette: WallPalette,
    private val root: MutableState<IntSize>,
) {
    var rootSize: IntSize
        get() = root.value
        set(value) { root.value = value }

    /** Gambar wallpaper seolah elemen di [origin] (koordinat root) adalah jendela ke latar. */
    fun draw(scope: DrawScope, origin: Offset, frosted: Boolean) = with(scope) {
        val r = rootSize
        if (r.width == 0 || r.height == 0) return@with
        val img = if (frosted) frost else background
        val scale = max(r.width / img.width.toFloat(), r.height / img.height.toFloat())
        val dw = img.width * scale
        val dh = img.height * scale
        clipRect {
            drawImage(
                img,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(img.width, img.height),
                dstOffset = IntOffset(((r.width - dw) / 2 - origin.x).roundToInt(), ((r.height - dh) / 2 - origin.y).roundToInt()),
                dstSize = IntSize(dw.roundToInt(), dh.roundToInt()),
                colorFilter = filter,
                filterQuality = FilterQuality.High,
            )
            drawRect(scrim)
        }
    }
}

val LocalWallpaper = staticCompositionLocalOf<Wallpaper?> { null }

/** Muat & olah wallpaper sesuai pengaturan. Null selama belum ada / belum siap. */
@Composable
fun rememberWallpaper(settings: AppSettings, dark: Boolean): Wallpaper? {
    val context = LocalContext.current
    val root = remember { mutableStateOf(IntSize.Zero) }

    val source by produceState<Pair<Bitmap, WallPalette>?>(null, settings.wallpaper) {
        value = if (settings.wallpaper == 0L) null
        else WallpaperFile.load(context)?.let { bmp ->
            withContext(Dispatchers.Default) { bmp to WallpaperFile.analyze(bmp) }
        }
    }

    // Diolah ulang saat slider blur digeser; nilai lama dipertahankan supaya tidak berkedip.
    var frames by remember { mutableStateOf<Pair<ImageBitmap, ImageBitmap>?>(null) }
    val src = source
    LaunchedEffect(src, settings.wallBlur) {
        if (src == null) { frames = null; return@LaunchedEffect }
        if (frames != null) delay(60)
        frames = withContext(Dispatchers.Default) {
            val b = settings.wallBlur.coerceIn(0f, 1f)
            val bg = if (b < 0.03f) WallpaperFile.blurred(src.first, 1280, 0, 0)
            else WallpaperFile.blurred(src.first, (420 - 340 * b).roundToInt(), 1 + (b * 3).roundToInt(), 2)
            val frost = WallpaperFile.blurred(src.first, 56, 4, 3)
            bg.asImageBitmap() to frost.asImageBitmap()
        }
    }

    val f = frames ?: return null
    val palette = src?.second ?: return null
    return remember(f, palette, dark) {
        Wallpaper(
            background = f.first,
            frost = f.second,
            filter = comfortFilter(palette),
            scrim = scrimFor(palette, dark),
            palette = palette,
            root = root,
        )
    }
}

/**
 * Membuat gambar nyaman di mata, otomatis dari isi gambarnya (user tidak perlu mengatur):
 * - kontras diturunkan ke arah warna rata-rata sampai simpangan kecerahannya ± [TARGET_STD],
 *   jadi foto yang ramai/kontras tinggi diredam lebih kuat, foto yang sudah kalem dibiarkan;
 * - saturasi diturunkan untuk gambar yang warnanya terlalu menyala.
 */
private fun comfortFilter(p: WallPalette): ColorFilter {
    val k = (TARGET_STD / p.lumaStd.coerceAtLeast(0.01f)).coerceIn(0.35f, 0.8f)
    val sat = (0.32f / p.saturation.coerceAtLeast(0.01f)).coerceIn(0.6f, 0.9f)
    // matriks saturasi (bobot luminans sama seperti android.graphics.ColorMatrix) dikali kontras
    val lr = 0.213f * (1 - sat)
    val lg = 0.715f * (1 - sat)
    val lb = 0.072f * (1 - sat)
    val o = 1f - k
    return ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                k * (lr + sat), k * lg, k * lb, 0f, o * AColor.red(p.mean),
                k * lr, k * (lg + sat), k * lb, 0f, o * AColor.green(p.mean),
                k * lr, k * lg, k * (lb + sat), 0f, o * AColor.blue(p.mean),
                0f, 0f, 0f, 1f, 0f,
            )
        )
    )
}

/**
 * Lapisan gelap (mode gelap) atau terang (mode terang) yang kekuatannya dihitung dari kecerahan
 * gambar, supaya teks putih/hitam di atasnya selalu terbaca dan latar tidak menyilaukan.
 * Gambar yang ramai dapat lapisan sedikit lebih tebal.
 */
private fun scrimFor(p: WallPalette, dark: Boolean): Color {
    val luma = p.luma
    val alpha = if (dark) {
        val auto = 1f - DARK_TARGET / luma.coerceAtLeast(0.01f)
        (auto + p.lumaStd * 0.4f).coerceIn(0.2f, 0.88f)
    } else {
        val auto = (LIGHT_TARGET - luma) / (1f - luma).coerceAtLeast(0.01f)
        (auto + p.lumaStd * 0.4f).coerceIn(0.25f, 0.88f)
    }
    return (if (dark) Color.Black else Color.White).copy(alpha = alpha)
}

private const val TARGET_STD = 0.1f
private const val DARK_TARGET = 0.24f
private const val LIGHT_TARGET = 0.84f

// ---- Warna yang diturunkan dari wallpaper ------------------------------------------------

private fun hsv(h: Float, s: Float, v: Float, a: Float = 1f): Color =
    Color(AColor.HSVToColor(floatArrayOf(h, s.coerceIn(0f, 1f), v.coerceIn(0f, 1f)))).copy(alpha = a)

/**
 * Palet iOS yang diwarnai wallpaper: card & kaca memakai rona warna rata-rata gambar (tembus
 * pandang di atas potongan wallpaper buram), aksen diambil dari warna paling hidup di gambar.
 */
fun wallpaperColors(base: IosColors, p: WallPalette, tinted: Boolean): IosColors {
    val m = FloatArray(3).also { AColor.colorToHSV(p.mean, it) }
    val h = m[0]
    val s = minOf(m[1], 0.45f)
    val dark = base.isDark
    return base.copy(
        background = Color.Transparent,
        groupedBackground = Color.Transparent,
        cell = if (dark) hsv(h, s * 0.8f, 0.2f, 0.5f) else hsv(h, s * 0.25f, 1f, 0.55f),
        elevated = if (dark) hsv(h, s * 0.6f, 0.17f) else hsv(h, s * 0.15f, 0.98f),
        separator = if (dark) hsv(h, s * 0.3f, 0.85f, 0.22f) else hsv(h, s * 0.4f, 0.25f, 0.2f),
        fill = if (dark) hsv(h, s * 0.4f, 0.65f, 0.28f) else hsv(h, s * 0.5f, 0.4f, 0.14f),
        accent = wallpaperAccent(p, dark) ?: base.accent,
        placeholderTop = if (dark) hsv(h, s * 0.5f, 0.32f) else hsv(h, s * 0.25f, 0.93f),
        placeholderBottom = if (dark) hsv(h, s * 0.5f, 0.2f) else hsv(h, s * 0.3f, 0.83f),
        glassFill = if (dark) hsv(h, s * 0.8f, 0.2f, if (tinted) 0.85f else 0.45f)
        else hsv(h, s * 0.2f, 1f, if (tinted) 0.9f else 0.5f),
    )
}

/** Aksen dari warna hidup di gambar, digeser sampai cukup kontras. Null kalau gambarnya hampir abu-abu. */
private fun wallpaperAccent(p: WallPalette, dark: Boolean): Color? {
    if (p.vibrantSat < 0.18f) return null
    val v = FloatArray(3).also { AColor.colorToHSV(p.vibrant, it) }
    var sat = v[1].coerceIn(0.5f, 0.85f)
    var value = if (dark) 1f else 0.78f
    var c = hsv(v[0], sat, value)
    repeat(8) {
        val l = c.luminance()
        when {
            dark && l < 0.3f -> sat -= 0.06f
            !dark && l > 0.2f -> value -= 0.06f
            else -> return c
        }
        c = hsv(v[0], sat, value)
    }
    return c
}

// ---- Modifier ----------------------------------------------------------------------------

/**
 * Gambar potongan wallpaper yang tepat berada di belakang elemen ini. [frosted] = versi blur
 * pekat untuk card/kaca. Tidak melakukan apa-apa kalau wallpaper tidak aktif.
 */
fun Modifier.backdrop(frosted: Boolean): Modifier = composed {
    val wp = LocalWallpaper.current ?: return@composed this
    var origin by remember { mutableStateOf(Offset.Zero) }
    this
        .onGloballyPositioned { origin = it.positionInRoot() }
        .drawBehind { wp.draw(this, origin, frosted) }
}

/**
 * Latar tepi yang memudar (scroll edge effect di atas & bawah). Tanpa wallpaper cukup gradasi
 * warna; dengan wallpaper, wallpaper-nya sendiri yang digambar ulang lalu dimask memudar.
 * [stops] = posisi 0..1 ke opasitas 0..1.
 */
fun Modifier.edgeFill(color: Color, vararg stops: Pair<Float, Float>): Modifier = composed {
    if (LocalWallpaper.current == null) {
        background(Brush.verticalGradient(*stops.map { (p, a) -> p to color.copy(alpha = color.alpha * a) }.toTypedArray()))
    } else {
        val mask = Brush.verticalGradient(*stops.map { (p, a) -> p to Color.Black.copy(alpha = a) }.toTypedArray())
        this
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(mask, blendMode = BlendMode.DstIn)
            }
            .backdrop(frosted = false)
            .background(color)
    }
}
