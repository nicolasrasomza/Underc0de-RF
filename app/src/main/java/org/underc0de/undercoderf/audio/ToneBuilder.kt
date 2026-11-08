package org.underc0de.undercoderf.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Genera audio PCM 16 bit mono con fase continua entre tonos.
 * Los tiempos se acumulan de forma fraccionaria, así no hay deriva en SSTV
 * (cada píxel dura 0,275 ms = 12,1 muestras a 44100 Hz).
 */
class ToneBuilder(
    private val sampleRate: Int = 44100,
    expectedSeconds: Double = 1.0,
    private val amplitude: Double = 0.8
) {
    private var buf = ShortArray((sampleRate * expectedSeconds).toInt() + 1024)
    private var count = 0
    private var targetSamples = 0.0
    private var phase = 0.0

    private fun ensure(extra: Int) {
        if (count + extra > buf.size) {
            buf = buf.copyOf(maxOf(buf.size * 2, count + extra))
        }
    }

    /** Tono senoidal. rampMs > 0 suaviza el inicio y el final (evita clics en Morse). */
    fun tone(freq: Double, ms: Double, rampMs: Double = 0.0) {
        targetSamples += sampleRate * ms / 1000.0
        val n = Math.round(targetSamples).toInt() - count
        if (n <= 0) return
        ensure(n)
        val step = 2.0 * PI * freq / sampleRate
        val ramp = (sampleRate * rampMs / 1000.0).toInt()
        for (i in 0 until n) {
            var a = amplitude
            if (ramp > 0) {
                if (i < ramp) {
                    a *= 0.5 * (1.0 - cos(PI * i / ramp))
                } else if (i >= n - ramp) {
                    a *= 0.5 * (1.0 - cos(PI * (n - 1 - i) / ramp))
                }
            }
            buf[count++] = (sin(phase) * a * 32767.0).toInt().toShort()
            phase += step
            if (phase > 2.0 * PI) phase -= 2.0 * PI
        }
    }

    fun silence(ms: Double) {
        targetSamples += sampleRate * ms / 1000.0
        val n = Math.round(targetSamples).toInt() - count
        if (n <= 0) return
        ensure(n)
        for (i in 0 until n) {
            buf[count++] = 0
        }
    }

    fun build(): ShortArray = buf.copyOf(count)
}
