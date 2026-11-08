package org.underc0de.undercoderf.sos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat

/** Posición GPS con el LocationManager del sistema (no depende de Google Play Services). */
object LocationTracker {
    private const val MAX_AGE_MS = 10 * 60 * 1000L

    var last by mutableStateOf<Location?>(null)
        private set
    var gpsEnabled by mutableStateOf(true)
        private set

    private var started = false
    private var manager: LocationManager? = null

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            last = pickBetter(last, location)
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    fun hasFinePermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    @Synchronized
    fun start(context: Context) {
        if (started || !hasPermission(context)) return
        val lm = context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        manager = lm
        try {
            val gps = hasFinePermission(context) && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val net = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            gpsEnabled = gps || net
            if (gps) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 0f, listener, Looper.getMainLooper())
            }
            if (net) {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 0f, listener, Looper.getMainLooper())
            }
            for (p in listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)) {
                val known = try { lm.getLastKnownLocation(p) } catch (_: SecurityException) { null }
                if (known != null) last = pickBetter(last, known)
            }
            started = gps || net
        } catch (_: SecurityException) {
            started = false
        }
    }

    @Synchronized
    fun stop() {
        try {
            manager?.removeUpdates(listener)
        } catch (_: SecurityException) {
        }
        started = false
    }

    private fun ageMs(l: Location): Long =
        (SystemClock.elapsedRealtimeNanos() - l.elapsedRealtimeNanos) / 1_000_000L

    private fun pickBetter(old: Location?, new: Location): Location {
        if (old == null) return new
        if (new.elapsedRealtimeNanos <= old.elapsedRealtimeNanos) return old
        val oldStale = ageMs(old) > 30_000L
        val notWorse = !new.hasAccuracy() || !old.hasAccuracy() || new.accuracy <= old.accuracy * 1.5f
        return if (oldStale || notWorse) new else old
    }

    /** Última posición, solo si es reciente (menos de 10 minutos). */
    fun current(): Location? = last?.takeIf { ageMs(it) <= MAX_AGE_MS }
}
