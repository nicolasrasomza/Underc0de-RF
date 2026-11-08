package org.underc0de.undercoderf.sos

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.underc0de.undercoderf.morse.MorseEngine

/** Estado del switch "incluir posición GPS" de la pestaña SOS. */
object SosState {
    var withGps by mutableStateOf(false)
}

object SosPayload {
    const val BASE = "SOS SOS SOS"
    const val WPM = 10
    const val GAP_MS = 3000L

    /** Texto que se emitiría con el estado actual (sin tocar el GPS). */
    fun preview(): String {
        if (!SosState.withGps) return BASE
        val loc = LocationTracker.current() ?: return BASE
        return "$BASE ${CoordFormat.morse(loc.latitude, loc.longitude)} K"
    }

    /** Texto a emitir ahora. Con GPS y fix reciente: "SOS SOS SOS 32 53 10 S 68 50 40 W K". */
    fun text(context: Context): String {
        if (SosState.withGps) LocationTracker.start(context)
        return preview()
    }

    fun audio(context: Context): ShortArray = MorseEngine().textToAudio(text(context), WPM)
}
