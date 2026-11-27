package com.namal.qmgstudio

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Diagnostics: is the module installed, is a metamodule present, and is the module's file really mounted over the stock one?
 * Used by the Tools > Diagnostics card and by the silent background worker.
 */
object Diag {
    private const val PREFS = "qmg_diag"
    private const val WORK_PERIODIC = "qmg_diag_periodic"
    private const val WORK_BOOT = "qmg_diag_boot"
    private const val NOTIF_ID = 4202

    /**
     * text = the report; problem = a short message when the module is installed but NOT active (null when fine or unknown);
     * metaWarn = set when KernelSU has no metamodule (module files would never be mounted), even before any module exists.
     * Both need root, so they are null when root is not available.
     */
    class Report(val text: String, val rootOk: Boolean, val problem: String?, val metaWarn: String? = null)

    const val META_WARNING =
        "Your root manager (KernelSU) has no metamodule installed. Newer KernelSU does not mount any module's files without one, " +
            "so your animation would never show. Install a metamodule (for example meta-overlayfs) in KernelSU, reboot, then tap Check again. " +
            "(Older KernelSU versions do not need one.)"

    // '@' stands for '$' (so the Kotlin string does not try to interpolate shell variables)
    private val SCRIPT = """
M=/data/adb/modules/${Engine.MODULE_ID}
echo uid=@(id -u)
echo adb=@(ls /data/adb 2>/dev/null | tr '\n' ' ')
echo metalink=@(readlink /data/adb/metamodule 2>/dev/null)
echo metaprops=@(grep -l -i -E '^metamodule=(1|true)' /data/adb/modules/*/module.prop 2>/dev/null | tr '\n' ' ')
echo modules=@(ls /data/adb/modules 2>/dev/null | tr '\n' ' ')
echo installed=@([ -d @M ] && echo 1 || echo 0)
echo disabled=@([ -f @M/disable ] && echo 1 || echo 0)
echo remove=@([ -f @M/remove ] && echo 1 || echo 0)
echo pending=@([ -d /data/adb/modules_update/${Engine.MODULE_ID} ] && echo 1 || echo 0)
echo modtime=@(stat -c %Y @M/module.prop 2>/dev/null)
echo now=@(date +%s)
echo up=@(cut -d. -f1 /proc/uptime)
echo selinux=@(getenforce 2>/dev/null)
for f in bootsamsung.qmg bootsamsungloop.qmg shutdown.qmg; do
for p in system/media:/system/media system/product/media:/product/media system/system_ext/media:/system_ext/media system/vendor/media:/vendor/media; do
s=@{p%%:*}
l=@{p##*:}
[ -f @M/@s/@f ] && echo "mod:@l/@f=@(md5sum @M/@s/@f | cut -d' ' -f1)"
[ -f @l/@f ] && echo "live:@l/@f=@(md5sum @l/@f | cut -d' ' -f1)"
done
done
true
""".trimIndent().replace('@', '$')

    private fun shell(script: String): String? = try {
        val p = ProcessBuilder("su", "-c", script).redirectErrorStream(true).start()
        val out = p.inputStream.bufferedReader().readText()
        p.waitFor()
        out
    } catch (e: Exception) {
        null
    }

    private fun short(s: String?): String = if (s == null) "—" else s.take(8)

    fun run(ctx: Context): Report {
        val sb = StringBuilder()
        sb.append("QMG Studio ").append(Engine.appVersion(ctx)).append('\n')
        sb.append("Device: ").append(Build.MODEL).append(" · Android ").append(Build.VERSION.RELEASE)
            .append(" (API ").append(Build.VERSION.SDK_INT).append(") · ").append(Build.SUPPORTED_ABIS.firstOrNull() ?: "?").append('\n')
        sb.append("Samsung: ").append(if (Engine.isSamsung()) "yes" else "NO").append(" · Decoder lib: ")
            .append(if (Engine.findLib(ctx) != null) "found" else "NOT found").append('\n')

        val out = shell(SCRIPT)
        val kv = HashMap<String, String>()
        val mod = LinkedHashMap<String, String>()
        val live = LinkedHashMap<String, String>()
        if (out != null) {
            for (line in out.lines()) {
                val eq = line.indexOf('=')
                if (eq <= 0) continue
                val k = line.substring(0, eq).trim()
                val v = line.substring(eq + 1).trim()
                when {
                    k.startsWith("mod:") -> mod[k.removePrefix("mod:")] = v
                    k.startsWith("live:") -> live[k.removePrefix("live:")] = v
                    else -> kv[k] = v
                }
            }
        }
        val rootOk = kv["uid"] == "0"
        if (!rootOk) {
            sb.append("Root: NOT available (denied or no su)\n\n")
            sb.append("Grant root to QMG Studio in your root manager and run this again.")
            return Report(sb.toString(), false, null)
        }

        val adb = kv["adb"].orEmpty().split(' ').filter { it.isNotEmpty() }
        val mgr = when {
            adb.contains("ksud") -> "KernelSU"
            adb.contains("magisk") -> "Magisk"
            adb.contains("ap") -> "APatch"
            else -> "unknown"
        }
        val metas = kv["metaprops"].orEmpty().split(' ').filter { it.isNotEmpty() }
            .map { it.substringAfter("/modules/").substringBefore("/") }
        val metaLink = kv["metalink"].orEmpty().substringAfterLast('/')
        val metaName = if (metas.isNotEmpty()) metas.joinToString(", ") else if (metaLink.isNotEmpty()) metaLink else ""
        val installed = kv["installed"] == "1"
        val disabled = kv["disabled"] == "1"
        val removing = kv["remove"] == "1"
        val modTime = kv["modtime"]?.toLongOrNull() ?: 0L
        val now = kv["now"]?.toLongOrNull() ?: 0L
        val up = kv["up"]?.toLongOrNull() ?: 0L
        val bootEpoch = if (now > 0 && up > 0) now - up else 0L
        val pendingReboot = kv["pending"] == "1" || (installed && bootEpoch > 0 && modTime > bootEpoch)

        sb.append("Root: yes · Manager: ").append(mgr).append(" · SELinux: ").append(kv["selinux"].orEmpty().ifEmpty { "?" }).append('\n')
        sb.append("Metamodule: ").append(if (metaName.isNotEmpty()) metaName else "none found").append('\n')
        sb.append("Module: ")
        sb.append(
            if (!installed && pendingReboot) "flashed, waiting for a reboot"
            else if (!installed) "not installed"
            else if (removing) "marked for removal"
            else if (disabled) "installed but DISABLED"
            else "installed"
        ).append('\n')

        var mounted: Boolean? = null
        if (installed && mod.isNotEmpty()) {
            mounted = mod.keys.all { k -> live[k] != null && live[k] == mod[k] }
            sb.append("Files:\n")
            for (k in mod.keys) {
                val same = live[k] != null && live[k] == mod[k]
                sb.append(if (same) "  ✓ " else "  ✗ ").append(k)
                    .append("  module ").append(short(mod[k])).append(" · live ").append(short(live[k])).append('\n')
            }
        } else if (live.isNotEmpty()) {
            sb.append("Stock files:\n")
            for (k in live.keys) sb.append("  ").append(k).append("  ").append(short(live[k])).append('\n')
        }

        var problem: String? = null
        val verdict: String
        if (!installed && pendingReboot) {
            verdict = "Reboot to activate the module."
        } else if (!installed) {
            verdict = "No QMG module installed yet. Save the module .zip and flash it in your root manager."
        } else if (removing) {
            verdict = "The module is marked for removal and goes away at the next reboot."
        } else if (disabled) {
            verdict = "The module is disabled. Turn it on in your root manager and reboot."
            problem = "The QMG module is disabled in your root manager."
        } else if (pendingReboot && mounted != true) {
            verdict = "Module updated after the last boot. Reboot to activate it."
        } else if (mounted == true) {
            verdict = "✅ Mounted: the phone uses your animation."
        } else if (mounted == false) {
            if (mgr == "KernelSU" && metaName.isEmpty()) {
                verdict = "❌ Not mounted. KernelSU has no metamodule, so module files are never mounted. Install meta-overlayfs and reboot."
                problem = "KernelSU has no metamodule, so your animation is not mounted. Install meta-overlayfs and reboot."
            } else {
                verdict = "❌ Not mounted. Reboot once; if it stays like this, check your metamodule / mount mode."
                problem = "The module is installed but its files do not appear mounted over the stock animation. Open QMG Studio and read the warning at the top."
            }
        } else {
            verdict = "Module found but it has no animation files."
        }
        // a module whose files are really mounted proves everything works, whatever we could or could not detect
        val metaWarn = if (mgr == "KernelSU" && metaName.isEmpty() && mounted != true) META_WARNING else null
        if (metaWarn != null && !installed) sb.append("\nWarning: no metamodule found. ").append("Install meta-overlayfs before you flash the module.\n")
        sb.append("\nVerdict: ").append(verdict).append("\n(checked from a root shell)")
        return Report(sb.toString(), true, problem, metaWarn)
    }

    /* ---------------- settings ---------------- */

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** the silent background check is on by default (it only ever shows a notification when something is wrong) */
    fun silentEnabled(ctx: Context): Boolean = prefs(ctx).getBoolean("silent", true)

    fun rootOk(ctx: Context): Boolean = prefs(ctx).getBoolean("root_ok", false)

    fun setRootOk(ctx: Context, v: Boolean) {
        prefs(ctx).edit().putBoolean("root_ok", v).apply()
    }

    fun setSilent(ctx: Context, on: Boolean) {
        prefs(ctx).edit().putBoolean("silent", on).apply()
        if (on) schedule(ctx) else cancel(ctx)
    }

    /* ---------------- background scheduling ---------------- */

    fun schedule(ctx: Context) {
        val req = PeriodicWorkRequestBuilder<DiagWorker>(12, TimeUnit.HOURS).build()
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(WORK_PERIODIC, ExistingPeriodicWorkPolicy.KEEP, req)
    }

    fun scheduleAfterBoot(ctx: Context) {
        // wait a little so services and module mounts have settled
        val req = OneTimeWorkRequestBuilder<DiagWorker>().setInitialDelay(2, TimeUnit.MINUTES).build()
        WorkManager.getInstance(ctx).enqueueUniqueWork(WORK_BOOT, ExistingWorkPolicy.REPLACE, req)
    }

    fun cancel(ctx: Context) {
        val wm = WorkManager.getInstance(ctx)
        wm.cancelUniqueWork(WORK_PERIODIC)
        wm.cancelUniqueWork(WORK_BOOT)
    }

    /** at most one warning per boot (or per day when the boot counter is not readable) */
    fun notifyProblem(ctx: Context, msg: String) {
        if (!Notif.canPost(ctx)) return
        val boot = try {
            Settings.Global.getInt(ctx.contentResolver, Settings.Global.BOOT_COUNT, 0)
        } catch (e: Exception) {
            0
        }
        val key = if (boot != 0) boot else (System.currentTimeMillis() / 86_400_000L).toInt()
        val p = prefs(ctx)
        if (p.getInt("notified_key", -1) == key) return
        p.edit().putInt("notified_key", key).apply()
        Notif.init(ctx)
        val n = NotificationCompat.Builder(ctx, Notif.CH_ALERT)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("QMG animation is not active")
            .setContentText(msg)
            .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
            .setContentIntent(Notif.openApp(ctx))
            .setAutoCancel(true)
            .build()
        ctx.getSystemService(NotificationManager::class.java).notify(NOTIF_ID, n)
    }
}

/** Runs quietly in the background. Only speaks up (one notification) when the module is installed but not mounted. */
class DiagWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val c = applicationContext
        // never asks for root by itself: it only runs after root was granted once in the app
        if (!Diag.silentEnabled(c) || !Diag.rootOk(c)) return Result.success()
        val r = withContext(Dispatchers.IO) { Diag.run(c) }
        val problem = r.problem ?: r.metaWarn
        if (problem != null) Diag.notifyProblem(c, problem)
        return Result.success()
    }
}

/** Starts the after-boot check when the phone has finished booting (only if the silent check is on). */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && Diag.silentEnabled(context)) Diag.scheduleAfterBoot(context)
    }
}
