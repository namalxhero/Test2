package com.namal.qmgstudio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

/** Notification channels and helpers shared by the encode service and the diagnostics worker. */
object Notif {
    const val CH_ENCODE = "qmg_encode"
    const val CH_ALERT = "qmg_alert"

    fun init(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CH_ENCODE, "Encoding progress", NotificationManager.IMPORTANCE_LOW))
        nm.createNotificationChannel(NotificationChannel(CH_ALERT, "Alerts", NotificationManager.IMPORTANCE_DEFAULT))
    }

    fun canPost(ctx: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ctx.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun openApp(ctx: Context): PendingIntent {
        val i = Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(ctx, 0, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
}

/**
 * Keeps the process alive (foreground service + progress notification) while a long clip is being encoded,
 * so switching to another app does not slow down or kill the encode. The encoding itself still runs in the app.
 */
class EncodeService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Notif.init(this)
        startForeground(ID, build(this, 0, 0), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        return START_NOT_STICKY
    }

    companion object {
        private const val ID = 4201
        private const val DONE_ID = 4203

        @Volatile
        private var lastPct = -1

        private fun build(ctx: Context, done: Int, total: Int): Notification {
            val pct = if (total > 0) done * 100 / total else 0
            return NotificationCompat.Builder(ctx, Notif.CH_ENCODE)
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentTitle("QMG Studio is encoding")
                .setContentText(if (total > 0) "$done / $total frames · $pct%" else "Starting…")
                .setProgress(100, pct, total <= 0)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(Notif.openApp(ctx))
                .build()
        }

        fun start(ctx: Context) {
            lastPct = -1
            try {
                ContextCompat.startForegroundService(ctx, Intent(ctx, EncodeService::class.java))
            } catch (e: Exception) {
                // the encode still works without the service, it just is not protected in the background
            }
        }

        fun progress(ctx: Context, done: Int, total: Int) {
            val pct = if (total > 0) done * 100 / total else 0
            if (pct == lastPct) return
            lastPct = pct
            try {
                ctx.getSystemService(NotificationManager::class.java).notify(ID, build(ctx, done, total))
            } catch (e: Exception) {
            }
        }

        fun stop(ctx: Context) {
            try {
                ctx.stopService(Intent(ctx, EncodeService::class.java))
            } catch (e: Exception) {
            }
        }

        /** "Animation ready" – only shown when the app is not on screen */
        fun done(ctx: Context, text: String) {
            if (MainActivity.visible || !Notif.canPost(ctx)) return
            try {
                Notif.init(ctx)
                val n = NotificationCompat.Builder(ctx, Notif.CH_ALERT)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentTitle("QMG Studio")
                    .setContentText(text)
                    .setContentIntent(Notif.openApp(ctx))
                    .setAutoCancel(true)
                    .build()
                ctx.getSystemService(NotificationManager::class.java).notify(DONE_ID, n)
            } catch (e: Exception) {
            }
        }
    }
}
