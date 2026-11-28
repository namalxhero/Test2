package com.namal.qmgstudio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

/**
 * Community gallery. Animations live in a public GitHub repo (no server needed):
 *   items/<name>.zip   the module zip made by QMG Studio
 *   items/<name>.jpg   thumbnail (optional)
 * People add files with GitHub's "Upload files" page; the owner approves the pull request, then the
 * animation shows up for everyone. The app lists the folder with GitHub's public API and installs a
 * chosen zip with root.
 */
object Gallery {
    // change this one line to point the gallery at another repo
    const val REPO = "namalxhero/Qmg-gallery"
    const val BRANCH = "main"
    const val DIR = "items"

    const val REPO_URL = "https://github.com/" + REPO
    const val UPLOAD_URL = "https://github.com/" + REPO + "/upload/" + BRANCH + "/" + DIR

    data class Item(val name: String, val zipUrl: String, val thumbUrl: String?, val size: Long) {
        /** nice name for the list: "my_clip_QmgStudio-module" -> "my clip" */
        val title: String
            get() = name.removeSuffix("_QmgStudio-module").replace('_', ' ').replace('-', ' ').trim().ifEmpty { name }
    }

    /** The Explore repo does not exist yet (GitHub answered 404). */
    class NotSetUp : RuntimeException("Explore is not set up yet")

    private fun open(url: String, json: Boolean): HttpURLConnection {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 10_000
        c.readTimeout = 25_000
        c.instanceFollowRedirects = true
        c.setRequestProperty("User-Agent", "QmgStudio-Gallery")
        if (json) c.setRequestProperty("Accept", "application/vnd.github+json")
        return c
    }

    /** Lists the gallery. Returns an empty list when the folder does not exist yet. Throws on network errors. */
    fun list(): List<Item> {
        val c = open("https://api.github.com/repos/" + REPO + "/contents/" + DIR + "?ref=" + BRANCH, true)
        try {
            if (c.responseCode == 404) {
                // 404 on the repo itself = not created yet; 404 on only the items folder = nothing uploaded yet
                val r = open("https://api.github.com/repos/" + REPO, true)
                try { if (r.responseCode == 404) throw NotSetUp() } finally { r.disconnect() }
                return emptyList()
            }
            if (c.responseCode != 200) throw RuntimeException("GitHub answered " + c.responseCode)
            val arr = JSONArray(c.inputStream.bufferedReader().readText())
            val thumbs = HashMap<String, String>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val n = o.optString("name")
                val lower = n.lowercase()
                if (lower.endsWith(".jpg") || lower.endsWith(".png")) thumbs[n.substringBeforeLast('.')] = o.optString("download_url")
            }
            val out = ArrayList<Item>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val n = o.optString("name")
                if (!n.lowercase().endsWith(".zip")) continue
                val base = n.substringBeforeLast('.')
                val url = o.optString("download_url")
                if (url.isEmpty()) continue
                out.add(Item(base, url, thumbs[base], o.optLong("size", 0L)))
            }
            out.sortBy { it.name.lowercase() }
            return out
        } finally {
            c.disconnect()
        }
    }

    fun thumb(url: String): Bitmap? = try {
        val c = open(url, false)
        try {
            val bytes = c.inputStream.use { it.readBytes() }
            val opt = BitmapFactory.Options()
            opt.inSampleSize = 2
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opt)
        } finally {
            c.disconnect()
        }
    } catch (e: Exception) {
        null
    }

    /** Downloads the item's zip into the cache. */
    fun download(ctx: Context, item: Item, onProgress: (Long, Long) -> Unit): File {
        val dir = File(ctx.cacheDir, "gallery")
        dir.mkdirs()
        val f = File(dir, item.name.replace(Regex("[^A-Za-z0-9._-]"), "_") + ".zip")
        val c = open(item.zipUrl, false)
        try {
            if (c.responseCode != 200) throw RuntimeException("Download failed (" + c.responseCode + ")")
            val total = if (c.contentLengthLong > 0) c.contentLengthLong else item.size
            var got = 0L
            c.inputStream.use { ins ->
                f.outputStream().use { o ->
                    val buf = ByteArray(64 * 1024)
                    while (true) {
                        val n = ins.read(buf)
                        if (n < 0) break
                        o.write(buf, 0, n)
                        got += n
                        onProgress(got, total)
                    }
                }
            }
        } finally {
            c.disconnect()
        }
        return f
    }

    /** boot, loop, shutdown files found inside a module zip (each may be null) */
    class Files(val boot: File?, val loop: File?, val shutdown: File?)

    /** Pulls the three .qmg files out of a module zip. Only fixed output names are used, so a bad zip cannot write elsewhere. */
    fun extract(ctx: Context, zip: File): Files {
        val dir = File(ctx.cacheDir, "gallery_x")
        dir.deleteRecursively()
        dir.mkdirs()
        var boot: File? = null
        var loop: File? = null
        var shut: File? = null
        ZipInputStream(zip.inputStream().buffered()).use { z ->
            while (true) {
                val e = z.nextEntry ?: break
                if (e.isDirectory) continue
                val n = e.name.substringAfterLast('/')
                val target = when (n) {
                    "bootsamsung.qmg" -> File(dir, "boot.qmg")
                    "bootsamsungloop.qmg" -> File(dir, "loop.qmg")
                    "shutdown.qmg" -> File(dir, "shutdown.qmg")
                    else -> null
                }
                if (target == null) continue
                target.outputStream().use { o -> z.copyTo(o) }
                when (n) {
                    "bootsamsung.qmg" -> boot = target
                    "bootsamsungloop.qmg" -> loop = target
                    else -> shut = target
                }
            }
        }
        return Files(boot, loop, shut)
    }

    /** small jpg for the gallery list */
    fun thumbJpeg(src: Bitmap): ByteArray {
        val w = 320
        val h = (src.height * w / maxOf(1, src.width)).coerceIn(48, 640)
        val s = Bitmap.createScaledBitmap(src, w, h, true)
        val o = ByteArrayOutputStream()
        s.compress(Bitmap.CompressFormat.JPEG, 80, o)
        return o.toByteArray()
    }
}
