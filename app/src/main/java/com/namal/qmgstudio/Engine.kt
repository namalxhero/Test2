package com.namal.qmgstudio

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Movie
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.nio.ShortBuffer
import kotlinx.coroutines.sync.Mutex
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

enum class Fit { FILL, FIT, STRETCH, GLOW }

/** Animation size the phone expects. */
data class Target(val w: Int, val h: Int, val source: String, val stockDir: String?)

data class Opts(
    val startMs: Long,
    val lenSec: Float,       // output length in seconds
    val quality: Int,        // 0 best, 1 balanced, 2 small
    val fit: Fit,
    val dither: Boolean,
    val fadeFrames: Int,     // frames faded in at the start / out at the end
    val speed: Float,        // 1.0 = normal
    val targetKb: Int,       // 0 = off, otherwise try to stay near this many KB
    val ed: Edits = Edits()  // extra editing (play order, brightness/contrast, text)
)

/** Extra editing applied on top of the clip: brightness/contrast, a text overlay and a free crop box. */
data class Edits(
    val brightness: Int = 0,  // -60..60 (on the 0..255 scale)
    val contrast: Int = 100,  // 70..140 percent
    val text: String = "",
    val textPos: Int = 2,     // 0 top, 1 middle, 2 bottom
    val textSize: Int = 1,    // 0 small, 1 medium, 2 large
    val cropW: Int = 100,     // 10..100: how much of the video's width the crop box keeps (percent)
    val cropH: Int = 100,     // 10..100: how much of the video's height the crop box keeps (percent)
    val panX: Int = 50,       // 0..100: where the crop box sits (0 = left, 100 = right)
    val panY: Int = 50,       // 0..100: 0 = top, 100 = bottom
    val zoom: Int = 100,      // 30..200: size of the whole video on the screen (percent of "fits the screen")
    val moveX: Int = 50,      // 0..100: where the sized video sits (0 = left, 100 = right)
    val moveY: Int = 50,      // 0..100: 0 = top, 100 = bottom
    val stretch: Int = 0      // 0..100: 0 = keep the video's shape, 100 = stretched to fill the whole screen (nothing is cut off)
)

data class Built(val file: File, val frames: Int, val bytes: Long)

/** Sequential .qmg reader. Every slot is an independent native decoder, so the preview player, the GIF exporter and the
 *  single-frame preview never disturb each other. */
class QReader(private val slot: Int) {
    val lock = Any()
    var w = 0
    var h = 0
    var n = 0
    var lastRc = 0
    private var px = ShortArray(0)
    private val ring = arrayOfNulls<Bitmap>(4)
    private var ringPos = 0

    fun open(path: String, lib: String): Int = synchronized(lock) {
        val r = Native.nativeDecOpen(slot, path, lib)
        if (r > 0) {
            val i = Native.nativeDecInfo(slot)
            if (i[0] != w || i[1] != h) { for (k in ring.indices) ring[k] = null }
            w = i[0]; h = i[1]; n = i[2]
            if (px.size != w * h) px = ShortArray(w * h)
        }
        lastRc = 0
        r
    }

    /**
     * next frame as an RGB_565 bitmap, or null at the end / on error (see lastRc).
     * reuse = true cycles through 4 bitmaps (for the live player: no garbage per frame). Callers must not recycle them.
     */
    fun next(reuse: Boolean = false): Bitmap? = synchronized(lock) {
        val rc = Native.nativeDecNext(slot, px)
        lastRc = rc
        if (rc != 0) return@synchronized null
        val b: Bitmap
        if (reuse) {
            ringPos = (ringPos + 1) % ring.size
            val old = ring[ringPos]
            b = if (old != null && !old.isRecycled && old.width == w && old.height == h) old
            else Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565).also { ring[ringPos] = it }
        } else {
            b = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
        }
        b.copyPixelsFromBuffer(ShortBuffer.wrap(px))
        b
    }

    fun close() = synchronized(lock) { Native.nativeDecClose(slot) }
}

val Reader = QReader(0)        // live preview player
val GifReader = QReader(1)     // QMG -> GIF export
val FrameReader = QReader(2)   // single-frame preview of the real encoder

object Engine {
    const val FPS = 12
    const val MAX_SECONDS = 60
    const val MODULE_ID = "qmgstudio_anim"
    const val AUTHOR = "xBooM-SL"
    const val REPO_URL = "https://github.com/xBooM-SL/Qmg-studio"
    const val ISSUES_URL = "https://github.com/xBooM-SL/Qmg-studio/issues"
    const val UPDATE_REPO = "namalxhero/Test2"   // where the new APKs are published (GitHub Actions of this repo)
    const val RELEASES_URL = "https://github.com/" + UPDATE_REPO + "/releases"

    /** max per-channel error (0..255) for Best / Balanced / Small */
    val TOL = intArrayOf(3, 8, 14)

    @Volatile
    var lastError: String = ""

    private val LIBS = listOf(
        "/system/lib64/libQmageDecoder.so",
        "/system_ext/lib64/libQmageDecoder.so",
        "/vendor/lib64/libQmageDecoder.so"
    )
    private val MEDIA_DIRS = listOf("/system/media", "/product/media", "/system_ext/media", "/vendor/media")

    /* ---------------- this phone / root ---------------- */

    /** 0 = not asked yet, 1 = root granted, 2 = root not available or denied */
    @Volatile
    var rootState: Int = 0

    /** version name + build number of the installed app, read from the package (never hard-coded) */
    fun appVersion(ctx: Context): String = try {
        val i = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
        (i.versionName ?: "?") + " (build " + i.longVersionCode + ")"
    } catch (e: Exception) {
        "?"
    }

    fun isSamsung(): Boolean = Build.MANUFACTURER.equals("samsung", ignoreCase = true)

    /** Model of the phone the app is running on (e.g. SM-A055F on a Samsung, "Google Pixel 8" elsewhere). */
    fun deviceName(): String {
        val model = Build.MODEL ?: "Android"
        val maker = Build.MANUFACTURER ?: ""
        if (isSamsung() || maker.isEmpty() || model.startsWith(maker, ignoreCase = true)) return model
        return maker.replaceFirstChar { it.uppercase() } + " " + model
    }

    /** Asks the root manager (KernelSU / Magisk / ...) for root. Shows its allow/deny pop-up the first time. */
    fun requestRoot(): Boolean {
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val finished = p.waitFor(30, java.util.concurrent.TimeUnit.SECONDS)
            if (!finished) {
                p.destroy()
                rootState = 2
                false
            } else {
                val out = p.inputStream.bufferedReader().readText()
                val ok = p.exitValue() == 0 && out.contains("uid=0")
                rootState = if (ok) 1 else 2
                ok
            }
        } catch (e: Exception) {
            rootState = 2
            false
        }
    }

    /* ---------------- decoder library (the delta table is read from the phone's own Samsung library) ---------------- */

    fun findLib(ctx: Context): File? {
        val own = File(ctx.filesDir, "libQmageDecoder.so")
        if (own.exists() && own.length() > 4096) return own
        return LIBS.map { File(it) }.firstOrNull { it.canRead() }
    }

    fun importLib(ctx: Context, uri: Uri): Boolean = try {
        ctx.contentResolver.openInputStream(uri)!!.use { i ->
            File(ctx.filesDir, "libQmageDecoder.so").outputStream().use { o -> i.copyTo(o) }
        }
        true
    } catch (e: Exception) {
        false
    }

    /* ---------------- source files ---------------- */

    fun displayName(ctx: Context, uri: Uri): String {
        ctx.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) return c.getString(0) ?: "video"
        }
        return "video"
    }

    fun isGif(ctx: Context, uri: Uri): Boolean {
        val t = ctx.contentResolver.getType(uri) ?: ""
        return t == "image/gif" || displayName(ctx, uri).lowercase().endsWith(".gif")
    }

    private fun openMovie(ctx: Context, uri: Uri): Movie? =
        try { ctx.contentResolver.openInputStream(uri)?.use { Movie.decodeStream(it) } } catch (e: Exception) { null }

    fun durationMs(ctx: Context, uri: Uri): Long {
        if (isGif(ctx, uri)) return (openMovie(ctx, uri)?.duration() ?: 0).toLong()
        val m = MediaMetadataRetriever()
        return try {
            m.setDataSource(ctx, uri)
            m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        } finally {
            m.release()
        }
    }

    fun thumbnail(ctx: Context, uri: Uri, atMs: Long): Bitmap? {
        try {
            var f: Bitmap? = null
            if (isGif(ctx, uri)) {
                val mv = openMovie(ctx, uri) ?: return null
                val b = Bitmap.createBitmap(maxOf(1, mv.width()), maxOf(1, mv.height()), Bitmap.Config.ARGB_8888)
                b.eraseColor(Color.BLACK)
                val d = mv.duration()
                mv.setTime(if (d > 0) (atMs % d).toInt() else 0)
                mv.draw(Canvas(b), 0f, 0f)
                f = b
            } else {
                val m = MediaMetadataRetriever()
                try {
                    m.setDataSource(ctx, uri)
                    f = m.getFrameAtTime(atMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                } finally {
                    m.release()
                }
            }
            val fr = f ?: return null
            val sc = 1280f / maxOf(fr.width, fr.height)
            if (sc >= 1f) return fr
            return Bitmap.createScaledBitmap(
                fr, (fr.width * sc).toInt().coerceAtLeast(1), (fr.height * sc).toInt().coerceAtLeast(1), true
            )
        } catch (e: Exception) {
            return null
        }
    }

    /** One accurately seeked frame (used by the "Exact" view so it matches the live preview). */
    fun frameAt(ctx: Context, uri: Uri, atMs: Long): Bitmap? {
        if (isGif(ctx, uri)) return thumbnail(ctx, uri, atMs)
        val m = MediaMetadataRetriever()
        return try {
            m.setDataSource(ctx, uri)
            val f = m.getFrameAtTime(atMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST) ?: return null
            val sc = 1280f / maxOf(f.width, f.height)
            if (sc >= 1f) f else Bitmap.createScaledBitmap(f, (f.width * sc).toInt().coerceAtLeast(1), (f.height * sc).toInt().coerceAtLeast(1), true)
        } catch (e: Exception) {
            null
        } finally {
            m.release()
        }
    }

    /* ---------------- matching the phone ---------------- */

    private fun parseHeader(b: ByteArray): IntArray? {
        if (b.size < 32 || b[0] != 'Q'.code.toByte() || b[1] != 'M'.code.toByte()) return null
        val w = (b[6].toInt() and 255) or ((b[7].toInt() and 255) shl 8)
        val h = (b[8].toInt() and 255) or ((b[9].toInt() and 255) shl 8)
        val n = (b[16].toInt() and 255) or ((b[17].toInt() and 255) shl 8)
        if (w <= 0 || h <= 0) return null
        return intArrayOf(w, h, n)
    }

    private fun readHead(path: String, allowRoot: Boolean): ByteArray? {
        try {
            val f = File(path)
            if (f.canRead()) {
                val buf = ByteArray(32)
                val got = f.inputStream().use { it.read(buf) }
                if (got == 32) return buf
            }
        } catch (e: Exception) {
        }
        if (!allowRoot) return null
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "head -c 32 '$path'"))
            val bytes = p.inputStream.use { it.readBytes() }
            p.waitFor()
            if (bytes.size >= 32) bytes else null
        } catch (e: Exception) {
            null
        }
    }

    /** Reads the stock bootsamsung.qmg header to learn the exact size this phone uses. Falls back to the screen size. */
    @Suppress("DEPRECATION")
    fun detectTarget(ctx: Context, allowRoot: Boolean): Target {
        for (dir in MEDIA_DIRS) {
            val head = readHead("$dir/bootsamsung.qmg", allowRoot) ?: continue
            val hd = parseHeader(head) ?: continue
            return Target(hd[0], hd[1], "stock animation", dir)
        }
        return try {
            val dm = DisplayMetrics()
            val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getRealMetrics(dm)
            val w = minOf(dm.widthPixels, dm.heightPixels) / 4 * 4
            val h = maxOf(dm.widthPixels, dm.heightPixels) / 4 * 4
            Target(w, h, "screen size", null)
        } catch (e: Exception) {
            Target(720, 1600, "default", null)
        }
    }

    fun loadTarget(ctx: Context): Target? {
        val p = ctx.getSharedPreferences("qmg", Context.MODE_PRIVATE)
        val w = p.getInt("tw", 0)
        val h = p.getInt("th", 0)
        if (w <= 0 || h <= 0) return null
        val src = p.getString("ts", "saved") ?: "saved"
        // an old "custom screen size" must never stay stuck: the phone preview always has the phone's own shape
        if (src == "custom") return null
        return Target(w, h, src, p.getString("td", null))
    }

    fun saveTarget(ctx: Context, t: Target) {
        ctx.getSharedPreferences("qmg", Context.MODE_PRIVATE).edit()
            .putInt("tw", t.w).putInt("th", t.h).putString("ts", t.source).putString("td", t.stockDir).apply()
    }

    /* ---------------- conversion ---------------- */

    /** the encoder keeps global state, so only one encode runs at a time */
    val encLock = Mutex()

    /** where Fill crops: 0 = keep the left/top part, 0.5 = centre, 1 = keep the right/bottom part */
    @Volatile var crop = 0.5f

    private fun avgColor(b: Bitmap, x: Int, y: Int, w: Int, h: Int): Int {
        val sub = Bitmap.createBitmap(b, x.coerceIn(0, b.width - 1), y.coerceIn(0, b.height - 1),
            w.coerceIn(1, b.width - x.coerceIn(0, b.width - 1)), h.coerceIn(1, b.height - y.coerceIn(0, b.height - 1)))
        val one = Bitmap.createScaledBitmap(sub, 1, 1, true)
        val c = one.getPixel(0, 0)
        if (one !== sub) one.recycle()
        if (sub !== b) sub.recycle()
        return c
    }

    /** average colour of the picture's top, bottom, left and right edge strips */
    fun edgeColors(src: Bitmap): IntArray {
        val tw = maxOf(2, src.width / 12)
        val th = maxOf(2, src.height / 12)
        return intArrayOf(
            avgColor(src, 0, 0, src.width, th),
            avgColor(src, 0, src.height - th, src.width, th),
            avgColor(src, 0, 0, tw, src.height),
            avgColor(src, src.width - tw, 0, tw, src.height)
        )
    }

    private fun darker(c: Int, f: Float): Int =
        Color.rgb((Color.red(c) * f).toInt(), (Color.green(c) * f).toInt(), (Color.blue(c) * f).toInt())

    /* ---------------- editing: crop, look, text ---------------- */

    /** The part of a w x h picture inside the user's crop box (any size or shape): [x, y, width, height]. */
    fun cropRect(w: Int, h: Int, ed: Edits): IntArray {
        val cw = (w * ed.cropW.coerceIn(10, 100) / 100f).toInt().coerceIn(1, maxOf(1, w))
        val ch = (h * ed.cropH.coerceIn(10, 100) / 100f).toInt().coerceIn(1, maxOf(1, h))
        val x = ((w - cw) * (ed.panX.coerceIn(0, 100) / 100f)).toInt().coerceIn(0, maxOf(0, w - cw))
        val y = ((h - ch) * (ed.panY.coerceIn(0, 100) / 100f)).toInt().coerceIn(0, maxOf(0, h - ch))
        return intArrayOf(x, y, cw, ch)
    }

    /** brightness/contrast as a 256-entry table, or null when nothing changes */
    fun lookTable(ed: Edits): IntArray? {
        if (ed.brightness == 0 && ed.contrast == 100) return null
        val k = ed.contrast / 100f
        return IntArray(256) { c -> (((c - 128) * k) + 128f + ed.brightness).toInt().coerceIn(0, 255) }
    }

    fun applyLook(ints: IntArray, lut: IntArray?) {
        if (lut == null) return
        for (i in ints.indices) {
            val p = ints[i]
            ints[i] = (p and -0x1000000) or (lut[(p shr 16) and 255] shl 16) or (lut[(p shr 8) and 255] shl 8) or lut[p and 255]
        }
    }

    /** Draws the overlay text centred on a canvas of size w x h (used by the encoder and by the live preview). */
    fun drawOverlay(cv: Canvas, w: Float, h: Float, ed: Edits) {
        val t = ed.text.trim()
        if (t.isEmpty()) return
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.typeface = android.graphics.Typeface.DEFAULT_BOLD
        p.color = Color.WHITE
        p.textAlign = Paint.Align.CENTER
        p.textSize = w * when (ed.textSize) { 0 -> 0.05f; 2 -> 0.11f; else -> 0.075f }
        val tw = p.measureText(t)
        val maxW = w * 0.9f
        if (tw > maxW) p.textSize = p.textSize * maxW / tw   // long text shrinks so it always fits
        p.setShadowLayer(p.textSize / 8f, 0f, 0f, Color.BLACK)
        val fm = p.fontMetrics
        val y = when (ed.textPos) {
            0 -> h * 0.08f - fm.ascent
            1 -> h / 2f - (fm.ascent + fm.descent) / 2f
            else -> h * 0.92f - fm.descent
        }
        cv.drawText(t, w / 2f, y, p)
    }

    fun drawOverlay(bmp: Bitmap, ed: Edits) {
        if (ed.text.isBlank()) return
        drawOverlay(Canvas(bmp), bmp.width.toFloat(), bmp.height.toFloat(), ed)
    }

    private fun drawFitted(src: Bitmap, w: Int, h: Int, fit: Fit, dst: Bitmap, ed: Edits = Edits()) {
        val cv = Canvas(dst)
        val m = Matrix()
        // the user's crop box first: only this part of the picture is fitted to the screen
        val cr = cropRect(src.width, src.height, ed)
        val fw = cr[2].toFloat()
        val fh = cr[3].toFloat()
        // size of the picture on screen: "fits the screen" * zoom, then stretched towards the full screen by the stretch amount
        val stK = ed.stretch / 100f
        val s0 = minOf(w / fw, h / fh) * ed.zoom / 100f
        val sx = s0 + (w / fw - s0) * stK
        val sy = s0 + (h / fh - s0) * stK
        m.postTranslate(-cr[0].toFloat(), -cr[1].toFloat())
        when (fit) {
            Fit.FILL -> {
                val s = maxOf(w / fw, h / fh)
                m.postScale(s, s); m.postTranslate((w - fw * s) * crop, (h - fh * s) * crop)
            }
            Fit.FIT, Fit.GLOW -> {
                m.postScale(sx, sy); m.postTranslate((w - fw * sx) * ed.moveX / 100f, (h - fh * sy) * ed.moveY / 100f)
            }
            Fit.STRETCH -> m.postScale(w / fw, h / fh)
        }
        cv.drawColor(Color.BLACK)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        // only the chosen part of the picture is drawn (matters when zoomed in, or with bars)
        val area = android.graphics.RectF(cr[0].toFloat(), cr[1].toFloat(), (cr[0] + cr[2]).toFloat(), (cr[1] + cr[3]).toFloat())
        m.mapRect(area)
        cv.save()
        cv.clipRect(area)
        cv.drawBitmap(src, m, paint)
        cv.restore()
    }

    /**
     * Encodes ONE frame with the real encoder and decodes it again, so the user sees exactly what the phone will show
     * (fit, dithering, quality). Returns the picture and the encoded size in bytes, or null if the encoder is busy.
     */
    suspend fun previewFrame(
        ctx: Context, src: Bitmap, tgt: Target, fit: Fit, dither: Boolean, quality: Int, ed: Edits = Edits()
    ): Pair<Bitmap, Int>? = withContext(Dispatchers.Default) {
        if (!encLock.tryLock()) return@withContext null
        try {
            val lib = findLib(ctx) ?: return@withContext null
            val w = tgt.w
            val h = tgt.h
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            drawFitted(src, w, h, fit, bmp, ed)
            drawOverlay(bmp, ed)
            val ints = IntArray(w * h)
            bmp.getPixels(ints, 0, w, 0, 0, w, h)
            applyLook(ints, lookTable(ed))
            bmp.recycle()
            val px = ShortArray(w * h)
            Native.nativeConvert(ints, px, w, h, dither, 256)
            if (Native.nativeInit(lib.absolutePath, w, h, TOL[quality.coerceIn(0, 2)]) != 0) return@withContext null
            val size = Native.nativeAddFrame(px)
            val tmp = File(ctx.cacheDir, "pv.qmg")
            val total = Native.nativeFinish(tmp.absolutePath)
            Native.nativeFree()
            if (size < 0 || total <= 0) return@withContext null
            synchronized(FrameReader.lock) {
                val n = FrameReader.open(tmp.absolutePath, lib.absolutePath)
                val b = if (n > 0) FrameReader.next() else null
                FrameReader.close()
                if (b != null) Pair(b, size) else null
            }
        } catch (e: Exception) {
            null
        } finally {
            encLock.unlock()
        }
    }

    /** One MediaMetadataRetriever per worker (a retriever is not thread safe). */
    private class Fetcher(ctx: Context, uri: Uri) {
        val m = MediaMetadataRetriever().apply { setDataSource(ctx, uri) }
        fun full(tMs: Long): Bitmap? = m.getFrameAtTime(tMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST)

        /** small upright frame for the live preview; falls back to scaling a full frame */
        fun small(tMs: Long, dw: Int, dh: Int): Bitmap? {
            val us = tMs * 1000L
            try {
                val s = m.getScaledFrameAtTime(us, MediaMetadataRetriever.OPTION_CLOSEST, dw, dh)
                if (s != null) {
                    val ok = Math.abs(s.width.toFloat() / s.height - dw.toFloat() / dh) < 0.06f
                    if (ok) return s
                    s.recycle()
                }
            } catch (e: Exception) {
            }
            val f = m.getFrameAtTime(us, MediaMetadataRetriever.OPTION_CLOSEST) ?: return null
            val r = Bitmap.createScaledBitmap(f, dw, dh, true)
            if (r !== f) f.recycle()
            return r
        }

        fun close() {
            try { m.release() } catch (e: Exception) { }
        }
    }

    private fun workerCount(): Int = minOf(3, maxOf(1, Runtime.getRuntime().availableProcessors() - 1))

    suspend fun convert(
        ctx: Context, uri: Uri, tgt: Target, o: Opts, outFile: File,
        onProgress: (done: Int, total: Int) -> Unit,
        onFrame: ((Bitmap, Long) -> Unit)? = null
    ): Built = withContext(Dispatchers.Default) {
        val lib = findLib(ctx) ?: error("libQmageDecoder.so not found")
        encLock.lock()
        val fetchers = ArrayList<Fetcher>()
        try {
            coroutineScope {
                val scope = this
                val w = tgt.w
                val h = tgt.h
                val n = (o.lenSec * FPS).toInt().coerceIn(1, MAX_SECONDS * FPS)
                val speed = o.speed.coerceIn(0.25f, 4f)
                val lut = lookTable(o.ed)
                val rc = Native.nativeInit(lib.absolutePath, w, h, TOL[o.quality.coerceIn(0, 2)])
                if (rc != 0) error("encoder init failed ($rc). Size must be a multiple of 4 and the library must be a Samsung libQmageDecoder.so")

                val gif = isGif(ctx, uri)
                var movie: Movie? = null
                var gifBmp: Bitmap? = null
                var workers = 0
                if (gif) {
                    movie = openMovie(ctx, uri) ?: error("cannot read this GIF")
                    gifBmp = Bitmap.createBitmap(maxOf(1, movie.width()), maxOf(1, movie.height()), Bitmap.Config.ARGB_8888)
                } else {
                    workers = workerCount()
                    for (k in 0 until workers) fetchers.add(Fetcher(ctx, uri))
                }
                val mv = movie
                val gb = gifBmp

                // video frames are decoded by a few workers in parallel while the encoder works on the previous frame
                val pending = arrayOfNulls<Deferred<Bitmap?>>(n)
                fun fetch(i: Int): Deferred<Bitmap?> = scope.async(Dispatchers.Default) {
                    fetchers[i % workers].full(o.startMs + (i * 1000f / FPS * speed).toLong())
                }
                if (!gif) for (i in 0 until minOf(workers, n)) pending[i] = fetch(i)

                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val ints = IntArray(w * h)
                val px = ShortArray(w * h)
                var last: Bitmap? = null
                val budgetTotal = if (o.targetKb > 0) o.targetKb * 1024 else 0
                var remaining = budgetTotal
                var remW = 5 + (n - 1)
                var sofar = 0L

                for (i in 0 until n) {
                    ensureActive()
                    val f: Bitmap
                    if (mv != null && gb != null) {
                        val tMs = o.startMs + (i * 1000f / FPS * speed).toLong()
                        val d = mv.duration()
                        mv.setTime(if (d > 0) (tMs % d).toInt() else 0)
                        gb.eraseColor(Color.BLACK)
                        mv.draw(Canvas(gb), 0f, 0f)
                        f = gb
                    } else {
                        val got = pending[i]!!.await()
                        pending[i] = null
                        if (i + workers < n) pending[i + workers] = fetch(i + workers)
                        f = got ?: last ?: error("cannot read video frame")
                        if (f !== last) { last?.recycle(); last = f }
                    }
                    drawFitted(f, w, h, o.fit, bmp, o.ed)
                    drawOverlay(bmp, o.ed)
                    bmp.getPixels(ints, 0, w, 0, 0, w, h)
                    applyLook(ints, lut)

                    var gain = 256
                    val fade = o.fadeFrames
                    if (fade > 0) {
                        if (i < fade) gain = 256 * (i + 1) / (fade + 1)
                        if (n - 1 - i < fade) gain = minOf(gain, 256 * (n - i) / (fade + 1))
                    }
                    Native.nativeConvert(ints, px, w, h, o.dither, gain)

                    val size: Int
                    if (budgetTotal > 0) {
                        val wgt = if (i == 0) 5 else 1
                        val b = (remaining.toLong() * wgt / remW).toInt().coerceAtLeast(3000)
                        size = Native.nativeAddFrameBudget(px, b, 40)
                        remaining -= size
                        remW -= wgt
                    } else {
                        size = Native.nativeAddFrame(px)
                    }
                    if (size < 0) error("encode error")
                    sofar += size
                    if (onFrame != null) {
                        onFrame(Bitmap.createScaledBitmap(bmp, maxOf(1, w / 5), maxOf(1, h / 5), true), sofar)
                    }
                    onProgress(i + 1, n)
                }
                val total = Native.nativeFinish(outFile.absolutePath)
                if (total <= 0) error("could not write output")
                Built(outFile, n, total)
            }
        } finally {
            for (fe in fetchers) fe.close()
            Native.nativeFree()
            encLock.unlock()
        }
    }

    /* ---------------- live preview clip ---------------- */

    const val PREVIEW_MAX_FRAMES = 360

    /**
     * Small frames spread over the WHOLE chosen part of the clip, so the editor can play all of it live.
     * A short clip gets every frame; a longer one gets at most PREVIEW_MAX_FRAMES evenly spaced frames
     * (the live player slows its steps down to match, so the clip still plays in its real length).
     */
    class PreviewClip(val frames: List<Bitmap>, val total: Int)

    /**
     * Frames are loaded coarse first (every 4th, then every 2nd, then the rest). After each round [onPass] gets everything
     * loaded so far in play order, so the preview can start playing the whole clip after about a second and gets smoother.
     */
    suspend fun previewClip(
        ctx: Context, uri: Uri, startMs: Long, lenSec: Float, speed: Float,
        onPass: ((List<Bitmap>) -> Unit)? = null,
        onProgress: (Int, Int) -> Unit
    ): PreviewClip? = withContext(Dispatchers.Default) {
        val total = (lenSec * FPS).toInt().coerceIn(1, MAX_SECONDS * FPS)
        val shown = minOf(total, PREVIEW_MAX_FRAMES)
        val sp = speed.coerceIn(0.25f, 4f)
        val maxSide = 192f
        // which output frame preview frame k stands for, and where that is in the source video
        fun outIdx(k: Int): Int = if (shown <= 1) 0 else ((k.toLong() * (total - 1)) / (shown - 1)).toInt()
        fun timeOf(k: Int): Long = startMs + (outIdx(k) * 1000f / FPS * sp).toLong()
        val slots = arrayOfNulls<Bitmap>(shown)
        fun snapshot(): List<Bitmap> {
            val l = ArrayList<Bitmap>()
            for (k in 0 until shown) {
                val b = slots[k]
                if (b != null) l.add(b)
            }
            return l
        }
        val fetchers = ArrayList<Fetcher>()
        try {
            coroutineScope {
                val scope = this
                if (isGif(ctx, uri)) {
                    val mv = openMovie(ctx, uri) ?: return@coroutineScope
                    val gb = Bitmap.createBitmap(maxOf(1, mv.width()), maxOf(1, mv.height()), Bitmap.Config.ARGB_8888)
                    val sc = minOf(1f, maxSide / maxOf(gb.width, gb.height))
                    val dw = maxOf(1, (gb.width * sc).toInt())
                    val dh = maxOf(1, (gb.height * sc).toInt())
                    val d = mv.duration()
                    for (k in 0 until shown) {
                        ensureActive()
                        val tMs = timeOf(k)
                        mv.setTime(if (d > 0) (tMs % d).toInt() else 0)
                        gb.eraseColor(Color.BLACK)
                        mv.draw(Canvas(gb), 0f, 0f)
                        slots[k] = Bitmap.createScaledBitmap(gb, dw, dh, true)
                        onProgress(k + 1, shown)
                    }
                    onPass?.invoke(snapshot())
                    return@coroutineScope
                }
                // upright size of the video
                val meta = MediaMetadataRetriever()
                var uw = 0
                var uh = 0
                try {
                    meta.setDataSource(ctx, uri)
                    val vw = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
                    val vh = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
                    val rot = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toInt() ?: 0
                    if (rot == 90 || rot == 270) { uw = vh; uh = vw } else { uw = vw; uh = vh }
                } finally {
                    meta.release()
                }
                if (uw <= 0 || uh <= 0) return@coroutineScope
                val sc = minOf(1f, maxSide / maxOf(uw, uh))
                val dw = maxOf(2, (uw * sc).toInt())
                val dh = maxOf(2, (uh * sc).toInt())
                val workers = workerCount()
                for (j in 0 until workers) fetchers.add(Fetcher(ctx, uri))
                val last = shown - 1
                val passes = listOf(
                    (0 until shown).filter { it % 8 == 0 || it == last },
                    (0 until shown).filter { it % 8 == 4 && it != last },
                    (0 until shown).filter { it % 4 == 2 && it != last },
                    (0 until shown).filter { it % 2 == 1 && it != last }
                )
                val doneCount = java.util.concurrent.atomic.AtomicInteger(0)
                for (ks in passes) {
                    if (ks.isEmpty()) continue
                    // each worker owns one retriever and takes every workers-th frame of this round
                    val jobs = (0 until minOf(workers, ks.size)).map { j ->
                        scope.async(Dispatchers.Default) {
                            var n = j
                            while (n < ks.size) {
                                ensureActive()
                                val k = ks[n]
                                slots[k] = fetchers[j].small(timeOf(k), dw, dh)
                                onProgress(doneCount.incrementAndGet(), shown)
                                n += workers
                            }
                        }
                    }
                    for (job in jobs) job.await()
                    ensureActive()
                    onPass?.invoke(snapshot())
                }
            }
        } finally {
            for (fe in fetchers) fe.close()
        }
        val all = snapshot()
        if (all.isEmpty()) null else PreviewClip(all, total)
    }

    /* ---------------- verification with the phone's own Samsung decoder (experimental) ---------------- */

    fun verify(ctx: Context, qmg: File): String {
        val own = findLib(ctx)?.absolutePath ?: return "Decoder library not found"
        val priv = File(ctx.filesDir, "libQmageDecoder.so")
        val tries = ArrayList<String>()
        tries.addAll(LIBS.filter { File(it).exists() })
        var last = "no decoder library to test with"
        var round = 0
        while (round < 2) {
            for (p in tries) {
                val r = Native.nativeVerify(p, qmg.absolutePath, own)
                when (r[0]) {
                    0 -> return if (r[1] == r[2] && r[2] > 0)
                        "✅ This phone's own Samsung decoder shows exactly the same picture (" + r[1] + "/" + r[2] + " frames)"
                    else "⚠️ Only " + r[1] + " of " + r[2] + " frames matched the system decoder"
                    -10 -> last = "the system library could not be loaded from the app"
                    -11 -> last = "decoder functions not found in the library"
                    -12 -> last = "system decoder refused the file"
                    -13 -> last = "system decoder reported an error on frame " + (r[2] + 1)
                    else -> last = "internal check failed (" + r[0] + ")"
                }
            }
            if (round == 0 && !priv.exists()) {
                // some phones refuse to load system libs from apps: try a private copy
                val src = LIBS.map { File(it) }.firstOrNull { it.canRead() }
                if (src != null) {
                    try {
                        src.inputStream().use { i -> priv.outputStream().use { o -> i.copyTo(o) } }
                        tries.clear(); tries.add(priv.absolutePath)
                    } catch (e: Exception) {
                    }
                } else break
            } else if (round == 0 && priv.exists()) {
                tries.clear(); tries.add(priv.absolutePath)
            }
            round++
        }
        return "Could not run the check: $last"
    }

    /* ---------------- saving ---------------- */

    /** Saves to Downloads/QmgStudio/<sub>. No permission is needed on Android 10+. Returns the Uri or null. */
    fun saveToDownloads(ctx: Context, name: String, mime: String, sub: String, write: (OutputStream) -> Unit): Uri? {
        return try {
            val cv = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, name)
                put(MediaStore.Downloads.MIME_TYPE, mime)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/QmgStudio" + (if (sub.isEmpty()) "" else "/$sub"))
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val res = ctx.contentResolver
            val uri = res.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv)
            if (uri == null) {
                lastError = "MediaStore insert failed"
                return null
            }
            val os = res.openOutputStream(uri)
            if (os == null) {
                lastError = "cannot open output stream"
                return null
            }
            os.use(write)
            cv.clear()
            cv.put(MediaStore.Downloads.IS_PENDING, 0)
            res.update(uri, cv, null, null)
            uri
        } catch (e: Exception) {
            lastError = e.message ?: e.javaClass.simpleName
            null
        }
    }

    /* ---------------- save to the main QmgStudio folder ---------------- */

    const val MAIN_DIR = "/storage/emulated/0/QmgStudio"

    /** "My Clip.mp4" -> "My_Clip" (safe for file names and shell). */
    fun baseName(clip: String): String {
        val n = clip.substringBeforeLast('.', clip).replace(Regex("[^A-Za-z0-9._-]+"), "_").trim('_', '.')
        return if (n.isEmpty()) "animation" else n.take(60)
    }

    /**
     * Saves into the main-storage folder QmgStudio (no picker). Order: direct write (works when the app has
     * "all files access"), then root copy, then Download/QmgStudio as a last resort.
     * Returns where it was saved, or null on failure (see lastError).
     */
    fun saveMain(ctx: Context, name: String, mime: String, write: (OutputStream) -> Unit): String? {
        return try {
            // write once into app cache (fast, always allowed), then move it to the main QmgStudio folder
            val tmp = File(ctx.cacheDir, "save_" + name)
            java.io.FileOutputStream(tmp).use { fo ->
                val b = java.io.BufferedOutputStream(fo, 256 * 1024)
                write(b); b.flush()
            }
            if (tmp.length() <= 0L) { lastError = "empty file"; return null }
            // 1) main storage /storage/emulated/0/QmgStudio (same folder as the backup): direct write
            try {
                val dir = File(MAIN_DIR)
                if (dir.isDirectory || dir.mkdirs()) {
                    val dst = File(dir, name)
                    tmp.inputStream().use { i -> dst.outputStream().use { o -> i.copyTo(o) } }
                    if (dst.length() == tmp.length()) { tmp.delete(); return "QmgStudio/$name" }
                }
            } catch (e: Exception) { }
            // 2) same folder through root (this is how the backup is saved)
            try {
                val cmd = "mkdir -p '$MAIN_DIR' && cp '${tmp.path}' '$MAIN_DIR/$name' && chmod 666 '$MAIN_DIR/$name'"
                val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
                p.inputStream.readBytes(); p.errorStream.readBytes()
                if (p.waitFor() == 0 && File("$MAIN_DIR/$name").length() == tmp.length()) { tmp.delete(); return "QmgStudio/$name" }
            } catch (e: Exception) { }
            // 3) last resort only: Download/QmgStudio
            val u = saveToDownloads(ctx, name, mime, "") { o -> tmp.inputStream().use { it.copyTo(o) } }
            tmp.delete()
            if (u != null) "Download/QmgStudio/$name" else null
        } catch (e: Exception) {
            lastError = e.message ?: e.javaClass.simpleName
            null
        }
    }

    /** Writes to a Uri chosen by the user with the system file picker (always works, no permission). */
    fun writeTo(ctx: Context, uri: Uri, write: (OutputStream) -> Unit): Boolean {
        return try {
            val os = ctx.contentResolver.openOutputStream(uri, "rwt")
            if (os == null) {
                lastError = "cannot open output stream"
                return false
            }
            os.use(write)
            true
        } catch (e: Exception) {
            lastError = e.message ?: e.javaClass.simpleName
            false
        }
    }

    /* ---------------- modules / root ---------------- */

    private fun moduleDir(stockDir: String?): String = when (stockDir) {
        "/product/media" -> "system/product/media"
        "/system_ext/media" -> "system/system_ext/media"
        "/vendor/media" -> "system/vendor/media"
        else -> "system/media"
    }

    private fun moduleProp(): String {
        val code = (System.currentTimeMillis() / 1000).toInt()
        return "id=$MODULE_ID\n" +
            "name=QMG Studio Boot Animation\n" +
            "version=v1\n" +
            "versionCode=$code\n" +
            "author=QMG Studio\n" +
            "description=Custom Samsung boot/shutdown animation made with QMG Studio\n"
    }

    /** KernelSU / Magisk style module (needs a metamodule such as meta-overlayfs). */
    fun writeModuleZip(boot: File?, loop: File?, shutdown: File?, stockDir: String?, out: OutputStream) {
        val dir = moduleDir(stockDir)
        // .qmg files are already compressed, so entries are STORED (no deflate): the zip is written instantly.
        val z = ZipOutputStream(out)
        z.setLevel(java.util.zip.Deflater.NO_COMPRESSION)
        fun put(name: String, bytes: ByteArray) {
            val e = ZipEntry(name)
            val crc = java.util.zip.CRC32(); crc.update(bytes)
            e.method = ZipEntry.STORED
            e.size = bytes.size.toLong(); e.compressedSize = bytes.size.toLong(); e.crc = crc.value
            z.putNextEntry(e); z.write(bytes); z.closeEntry()
        }
        put("module.prop", moduleProp().toByteArray())
        if (boot != null) {
            val bb = boot.readBytes()
            put("$dir/bootsamsung.qmg", bb)
            put("$dir/bootsamsungloop.qmg", if (loop != null && loop != boot) loop.readBytes() else bb)
        }
        if (shutdown != null) put("$dir/shutdown.qmg", shutdown.readBytes())
        z.finish()   // writes the central directory (EOCD) so the zip is complete
        z.flush()
    }

    private fun su(script: String): Boolean {
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", script))
            val err = p.errorStream.bufferedReader().readText()
            val code = p.waitFor()
            if (code != 0) lastError = "root command failed: " + err.take(200)
            code == 0
        } catch (e: Exception) {
            lastError = "root: " + (e.message ?: "su not available")
            false
        }
    }

    /** Writes the module straight into /data/adb/modules (no zip flashing). Needs root + a metamodule. */
    fun installModuleRoot(ctx: Context, boot: File?, loop: File?, shutdown: File?, stockDir: String?): Boolean {
        val base = "/data/adb/modules/$MODULE_ID"
        val media = "$base/" + moduleDir(stockDir)
        val tmp = File(ctx.cacheDir, "modprop")
        tmp.writeText(moduleProp())
        val sb = StringBuilder()
        sb.append("rm -rf '$base' && mkdir -p '$media' && cp '${tmp.absolutePath}' '$base/module.prop'")
        if (boot != null) {
            sb.append(" && cp '${boot.absolutePath}' '$media/bootsamsung.qmg'")
            sb.append(" && cp '${(loop ?: boot).absolutePath}' '$media/bootsamsungloop.qmg'")
        }
        if (shutdown != null) sb.append(" && cp '${shutdown.absolutePath}' '$media/shutdown.qmg'")
        sb.append(" && chmod -R 755 '$base/system' && chmod 644 '$media'/*.qmg")
        sb.append(" && chown -R 0:0 '$base' && (chcon -R u:object_r:system_file:s0 '$base/system' || true)")
        return su(sb.toString())
    }

    fun uninstallModuleRoot(): Boolean = su("rm -rf '/data/adb/modules/$MODULE_ID'")

    fun rebootRoot(): Boolean = su("reboot")

    private fun rootCat(path: String, out: OutputStream): Boolean {
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat '$path'"))
            val n = p.inputStream.use { it.copyTo(out) }
            p.waitFor() == 0 && n > 0
        } catch (e: Exception) {
            lastError = "root: " + (e.message ?: "su not available")
            false
        }
    }

    /** Where the last backup went (shown in the UI). */
    @Volatile
    var lastBackupDir: String = ""

    private const val MAIN_BACKUP_DIR = "/storage/emulated/0/QmgStudio/backup"

    private fun rootCopyToMain(src: File, name: String): Boolean {
        return try {
            val cmd = "mkdir -p '$MAIN_BACKUP_DIR' && cp '${src.path}' '$MAIN_BACKUP_DIR/$name' && chmod 666 '$MAIN_BACKUP_DIR/$name'"
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            p.inputStream.readBytes()
            p.waitFor() == 0 && File("$MAIN_BACKUP_DIR/$name").length() > 0
        } catch (e: Exception) {
            false
        }
    }

    private fun rootList(dir: String): List<String> {
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "ls '$dir'"))
            val txt = p.inputStream.bufferedReader().readText()
            p.waitFor()
            txt.lines().map { it.trim() }.filter { it.endsWith(".qmg") }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Copies every stock .qmg from /system/media (and friends) to Downloads/QmgStudio/backup and to the
     * app's private folder. Tries a normal read first, then falls back to root. Returns how many files were backed up.
     */
    fun backupStock(ctx: Context): Int {
        var n = 0
        val done = HashSet<String>()
        val local = File(ctx.filesDir, "backup").apply { mkdirs() }
        for (dir in MEDIA_DIRS) {
            val names = LinkedHashSet<String>()
            File(dir).list()?.filter { it.endsWith(".qmg") }?.let { names.addAll(it) }
            if (names.isEmpty()) names.addAll(rootList(dir))
            for (name in names) {
                if (!done.add(name)) continue
                val src = File("$dir/$name")
                val dst = File(local, name)
                var ok = false
                try {
                    src.inputStream().use { i -> dst.outputStream().use { o -> i.copyTo(o) } }
                    ok = dst.length() > 0
                } catch (e: Exception) {
                    ok = false
                }
                if (!ok) ok = dst.outputStream().use { rootCat(src.path, it) }
                if (!ok) {
                    dst.delete()
                    continue
                }
                if (rootCopyToMain(dst, name)) {
                    lastBackupDir = "QmgStudio/backup"
                } else {
                    val u = saveToDownloads(ctx, name, "application/octet-stream", "backup") { o ->
                        dst.inputStream().use { it.copyTo(o) }
                    }
                    if (u != null) lastBackupDir = "Download/QmgStudio/backup"
                }
                n++
            }
        }
        return n
    }

    fun backupFiles(ctx: Context): List<File> =
        File(ctx.filesDir, "backup").listFiles()?.filter { it.name.endsWith(".qmg") }?.sortedBy { it.name } ?: emptyList()
}
