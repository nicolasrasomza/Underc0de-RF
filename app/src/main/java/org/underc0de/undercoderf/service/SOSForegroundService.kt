package org.underc0de.undercoderf.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import org.underc0de.undercoderf.MainActivity
import org.underc0de.undercoderf.audio.Transmitter
import org.underc0de.undercoderf.sos.LocationTracker
import org.underc0de.undercoderf.sos.SosPayload

/**
 * Mantiene viva la emisión continua de SOS aunque apagues la pantalla.
 * El audio lo genera/reproduce Transmitter; el servicio solo aporta el modo "primer plano".
 */
class SOSForegroundService : Service() {
    companion object {
        const val ACTION_STOP = "org.underc0de.undercoderf.STOP_SOS"
        private const val CHANNEL_ID = "SOS_CHANNEL"
    }

    @Volatile private var alive = false
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            Transmitter.stop()
            stopSelf()
            return START_NOT_STICKY
        }

        createNotificationChannel()
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, SOSForegroundService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("UndercodeRF - SOS ACTIVO")
            .setContentText("Transmitiendo señal de socorro SOS...")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setContentIntent(openApp)
            .addAction(0, "DETENER", stopIntent)
            .build()

        var type = ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        if (LocationTracker.hasFinePermission(this)) {
            type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        }
        try {
            ServiceCompat.startForeground(this, 1, notification, type)
        } catch (e: Exception) {
            stopSelf()
            return START_NOT_STICKY
        }

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UndercodeRF:SOS").apply {
            acquire(6 * 60 * 60 * 1000L)
        }

        alive = true
        val ctx = applicationContext
        Transmitter.transmit("SOS", loop = true, gapMs = SosPayload.GAP_MS) { SosPayload.audio(ctx) }

        // cuando el SOS se detiene (botón, notificación u otra transmisión), cerramos el servicio
        Thread {
            while (alive && Transmitter.current == "SOS") {
                try { Thread.sleep(500) } catch (_: InterruptedException) { break }
            }
            if (alive) stopSelf()
        }.start()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        alive = false
        if (Transmitter.current == "SOS") Transmitter.stop()
        LocationTracker.stop()
        wakeLock?.let { if (it.isHeld) it.release() }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Canal SOS de Emergencia", NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
