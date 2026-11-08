package org.underc0de.undercoderf.audio

import android.graphics.Bitmap
import kotlin.math.sin

class SSTVEngine {
    private val sampleRate = 44100

    fun generateRobot36Audio(bitmap: Bitmap): ShortArray {
        val scaled = Bitmap.createScaledBitmap(bitmap, 320, 240, true)
        val samples = mutableListOf<Short>()

        val syncFreq = 1200.0
        val porchFreq = 1500.0

        addTone(samples, 1900.0, 300.0)
        addTone(samples, 1200.0, 10.0)
        addTone(samples, 1900.0, 300.0)

        addTone(samples, 1200.0, 30.0)
        val visCode = 0x08
        for (i in 0..6) {
            val bit = (visCode shr i) and 1
            val freq = if (bit == 1) 1100.0 else 1300.0
            addTone(samples, freq, 30.0)
        }
        addTone(samples, 1200.0, 30.0)

        for (y in 0 until 240) {
            addTone(samples, syncFreq, 9.0)
            addTone(samples, porchFreq, 3.0)

            for (x in 0 until 320) {
                val pixel = scaled.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val yVal = 0.299 * r + 0.587 * g + 0.114 * b

                val freq = 1500.0 + (yVal / 255.0) * 800.0
                addTone(samples, freq, 0.275)
            }

            val isEven = y % 2 == 0
            addTone(samples, if (isEven) 1500.0 else 2300.0, 4.5)
            addTone(samples, 1900.0, 1.5)

            for (x in 0 until 320 step 2) {
                val pixel = scaled.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                val colorVal = if (isEven) {
                    (128 + 0.5 * r - 0.41869 * g - 0.08131 * b)
                } else {
                    (128 - 0.168736 * r - 0.331264 * g + 0.5 * b)
                }

                val freq = 1500.0 + (colorVal / 255.0) * 800.0
                addTone(samples, freq, 0.275)
            }
        }

        return samples.toShortArray()
    }

    private fun addTone(samples: MutableList<Short>, frequency: Double, durationMs: Double) {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * i * frequency / sampleRate
            val sample = (sin(angle) * Short.MAX_VALUE * 0.7).toInt().toShort()
            samples.add(sample)
        }
    }
}
