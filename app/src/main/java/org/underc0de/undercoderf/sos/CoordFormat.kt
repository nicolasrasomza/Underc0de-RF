package org.underc0de.undercoderf.sos

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

object CoordFormat {
    private data class Dms(val d: Int, val m: Int, val s: Int)

    private fun toDms(value: Double): Dms {
        val a = abs(value)
        var d = floor(a).toInt()
        val minFull = (a - d) * 60.0
        var m = floor(minFull).toInt()
        var s = ((minFull - m) * 60.0).roundToInt()
        if (s >= 60) { s = 0; m += 1 }
        if (m >= 60) { m = 0; d += 1 }
        return Dms(d, m, s)
    }

    private fun hemi(value: Double, pos: Char, neg: Char) = if (value >= 0) pos else neg

    /** Formato para Morse: "32 53 10 S 68 50 40 W" */
    fun morse(lat: Double, lon: Double): String {
        val a = toDms(lat)
        val o = toDms(lon)
        return "${a.d} ${a.m} ${a.s} ${hemi(lat, 'N', 'S')} ${o.d} ${o.m} ${o.s} ${hemi(lon, 'E', 'W')}"
    }

    /** Formato para pantalla: 32°53'10" S  68°50'40" W */
    fun pretty(lat: Double, lon: Double): String {
        val a = toDms(lat)
        val o = toDms(lon)
        return "${a.d}°${a.m}'${a.s}\" ${hemi(lat, 'N', 'S')}   ${o.d}°${o.m}'${o.s}\" ${hemi(lon, 'E', 'W')}"
    }
}
