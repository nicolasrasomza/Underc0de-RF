package org.underc0de.undercoderf.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.underc0de.undercoderf.audio.AudioEngine
import org.underc0de.undercoderf.morse.MorseEngine

class SOSForegroundService : Service() {
    private val audioEngine = AudioEngine()
    private val morseEngine = MorseEngine()
    private var isRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, "SOS_CHANNEL")
            .setContentTitle("UndercodeRF - SOS ACTIVO")
            .setContentText("Transmitiendo señal de socorro SOS en segundo plano...")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        startForeground(1, notification)

        isRunning = true
        Thread {
            val pcm = morseEngine.textToAudio("SOS SOS SOS 32 53 10 S 68 50 40 W K", wpm = 10)
            while (isRunning) {
                audioEngine.playPCM(pcm)
                Thread.sleep(5000)
            }
        }.start()

        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        audioEngine.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "SOS_CHANNEL",
                "Canal SOS de Emergencia",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
