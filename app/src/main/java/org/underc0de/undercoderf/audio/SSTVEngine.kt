package org.underc0de.undercoderf.audio

import android.graphics.Bitmap

/**
 * Codificador SSTV Robot 36 color (320x240, ~36 s).
 *
 * Cabecera: líder 1900 Hz 300 ms, corte 1200 Hz 10 ms, líder 1900 Hz 300 ms,
 * VIS (inicio 1200 Hz + 7 bits + paridad par + fin 1200 Hz, 30 ms cada uno, LSB primero,
 * 1100 Hz = 1 y 1300 Hz = 0). Robot 36 = código VIS 8.
 *
 * Cada línea (150 ms): sync 1200 Hz 9 ms, porch 1500 Hz 3 ms, Y 88 ms (320 px),
 * separador 4,5 ms (1500 Hz línea par = R-Y, 2300 Hz línea impar = B-Y),
 * porch 1900 Hz 1,5 ms, crominancia 44 ms (160 muestras).
 */
class SSTVEngine {
    companion object {
        const val WIDTH = 320
        const val HEIGHT = 240
        const val PIXEL_MS = 0.275
    }

    fun generateRobot36Audio(source: Bitmap): ShortArray {
        val bmp = if (source.width == WIDTH && source.height == HEIGHT) source
        else Bitmap.createScaledBitmap(source, WIDTH, HEIGHT, true)
        val pixels = IntArray(WIDTH * HEIGHT)
        bmp.getPixels(pixels, 0, WIDTH, 0, 0, WIDTH, HEIGHT)

        val tb = ToneBuilder(44100, 38.0)

        // Cabecera
        tb.tone(1900.0, 300.0)
        tb.tone(1200.0, 10.0)
        tb.tone(1900.0, 300.0)
        tb.tone(1200.0, 30.0) // bit de inicio
        val vis = 8
        var ones = 0
        for (i in 0 until 7) {
            val bit = (vis shr i) and 1
            ones += bit
            tb.tone(if (bit == 1) 1100.0 else 1300.0, 30.0)
        }
        tb.tone(if (ones % 2 == 1) 1100.0 else 1300.0, 30.0) // paridad par
        tb.tone(1200.0, 30.0) // bit de fin

        val yRow = DoubleArray(WIDTH)
        val ryRow = DoubleArray(WIDTH)
        val byRow = DoubleArray(WIDTH)

        for (y in 0 until HEIGHT) {
            for (x in 0 until WIDTH) {
                val p = pixels[y * WIDTH + x]
                val r = ((p shr 16) and 0xFF).toDouble()
                val g = ((p shr 8) and 0xFF).toDouble()
                val b = (p and 0xFF).toDouble()
                yRow[x] = 16.0 + (65.738 * r + 129.057 * g + 25.064 * b) / 256.0
                ryRow[x] = 128.0 + (112.439 * r - 94.154 * g - 18.285 * b) / 256.0
                byRow[x] = 128.0 + (-37.945 * r - 74.494 * g + 112.439 * b) / 256.0
            }

            tb.tone(1200.0, 9.0)   // sync
            tb.tone(1500.0, 3.0)   // porch
            for (x in 0 until WIDTH) tb.tone(levelToFreq(yRow[x]), PIXEL_MS)

            val even = y % 2 == 0
            tb.tone(if (even) 1500.0 else 2300.0, 4.5)
            tb.tone(1900.0, 1.5)
            val chroma = if (even) ryRow else byRow
            for (x in 0 until WIDTH step 2) {
                tb.tone(levelToFreq((chroma[x] + chroma[x + 1]) / 2.0), PIXEL_MS)
            }
        }
        tb.silence(200.0)
        return tb.build()
    }

    private fun levelToFreq(level: Double): Double {
        val l = level.coerceIn(0.0, 255.0)
        return 1500.0 + l / 255.0 * 800.0
    }
}
