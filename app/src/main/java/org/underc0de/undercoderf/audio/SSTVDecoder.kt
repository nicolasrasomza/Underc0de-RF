package org.underc0de.undercoderf.audio

import android.graphics.Bitmap
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

enum class RxPhase { LISTENING, VIS, RECEIVING, DONE }

/**
 * Decodificador SSTV Robot 36 en tiempo real (44100 Hz mono 16 bit).
 *
 * 1) Demodulación FM: mezcla con 1900 Hz (tabla de 441 muestras = 19 ciclos exactos),
 *    filtro pasa-bajos FIR y frecuencia instantánea por diferencia de fase.
 * 2) Busca el líder (1900 Hz) seguido del bit de inicio de VIS (1200 Hz), lee el código
 *    VIS y acepta solo Robot 36 (8).
 * 3) Cada línea se alinea con el final del pulso de sync (1200 Hz) y se lee Y + croma.
 */
class SstvDecoder(
    private val onPhase: (RxPhase, Int) -> Unit,
    private val onImage: (Bitmap, Boolean) -> Unit
) {
    private val fs = 44100
    private val fsD = 44100.0

    // ---------- demodulador ----------
    private val period = 441
    private val cosTab = FloatArray(period) { cos(2.0 * PI * 19.0 * it / period).toFloat() }
    private val sinTab = FloatArray(period) { sin(2.0 * PI * 19.0 * it / period).toFloat() }
    private val taps = 101
    private val h = FloatArray(taps)
    private val bufI = FloatArray(taps * 2)
    private val bufQ = FloatArray(taps * 2)
    private var fpos = 0
    private var phIdx = 0
    private var prevI = 0f
    private var prevQ = 0f
    private val avgN = 16
    private val avgBuf = FloatArray(avgN)
    private var avgIdx = 0
    private var avgSum = 0.0

    private val ringSize = 1 shl 17
    private val mask = ringSize - 1
    private val ring = FloatArray(ringSize)
    private var writePos = 0

    init {
        val fc = 1800.0 / fsD
        val mid = (taps - 1) / 2
        var sum = 0.0
        for (k in 0 until taps) {
            val m = k - mid
            val sinc = if (m == 0) 2.0 * fc else sin(2.0 * PI * fc * m) / (PI * m)
            val win = 0.54 - 0.46 * cos(2.0 * PI * k / (taps - 1))
            val v = sinc * win
            h[k] = v.toFloat()
            sum += v
        }
        for (k in 0 until taps) h[k] = (h[k] / sum).toFloat()
    }

    // ---------- máquina de estados ----------
    private enum class St { SEARCH, VIS, IMAGE }
    private var st = St.SEARCH
    private var scanPos = 0
    private var leaderCount = 0
    private var lowCount = 0
    private var otherCount = 0
    private var lowStart = 0
    private var visT0 = 0

    // imagen
    private val W = SSTVEngine.WIDTH
    private val H = SSTVEngine.HEIGHT
    private val pixels = IntArray(W * H)
    private val yLev = FloatArray(W * H)
    private val ry = FloatArray(W / 2) { 128f }
    private val by = FloatArray(W / 2) { 128f }
    private var lineIdx = 0
    private var expectedSync = 0
    private var miss = 0

    fun reset() {
        st = St.SEARCH
        leaderCount = 0; lowCount = 0; otherCount = 0
        scanPos = writePos
        onPhase(RxPhase.LISTENING, 0)
    }

    /** Alimenta el decodificador con audio crudo. Devuelve el nivel pico (0..1). */
    fun feed(samples: ShortArray, n: Int): Float {
        var peak = 0f
        for (i in 0 until n) {
            val x = samples[i] / 32768f
            val ax = if (x < 0) -x else x
            if (ax > peak) peak = ax
            pushSample(x)
        }
        process()
        return peak
    }

    private fun pushSample(x: Float) {
        val c = cosTab[phIdx]
        val s = sinTab[phIdx]
        phIdx = (phIdx + 1) % period

        bufI[fpos] = x * c
        bufI[fpos + taps] = x * c
        bufQ[fpos] = -x * s
        bufQ[fpos + taps] = -x * s
        var zi = 0f
        var zq = 0f
        val newest = fpos + taps
        for (j in 0 until taps) {
            zi += h[j] * bufI[newest - j]
            zq += h[j] * bufQ[newest - j]
        }
        fpos = (fpos + 1) % taps

        val re = zi * prevI + zq * prevQ
        val im = zq * prevI - zi * prevQ
        prevI = zi
        prevQ = zq
        val amp = zi * zi + zq * zq

        var f = 0f
        if (amp >= 1e-8f) {
            f = (1900.0 + atan2(im.toDouble(), re.toDouble()) * fsD / (2.0 * PI)).toFloat()
            if (f < 900f) f = 900f
            if (f > 2700f) f = 2700f
        }

        avgSum += f - avgBuf[avgIdx]
        avgBuf[avgIdx] = f
        avgIdx = (avgIdx + 1) % avgN
        ring[writePos and mask] = (avgSum / avgN).toFloat()
        writePos++
    }

    private fun process() {
        var go = true
        while (go) {
            go = when (st) {
                St.SEARCH -> stepSearch()
                St.VIS -> stepVis()
                St.IMAGE -> stepImage()
            }
        }
    }

    private fun f(i: Int): Float = ring[i and mask]

    private fun mean(a: Int, b: Int): Float {
        val end = if (b > a) b else a + 1
        var s = 0.0
        for (i in a until end) s += f(i)
        return (s / (end - a)).toFloat()
    }

    private fun ms(v: Double): Int = (v * fsD / 1000.0).toInt()

    // ---------- búsqueda de líder + inicio de VIS ----------
    private fun stepSearch(): Boolean {
        val leaderMin = ms(150.0)
        val lowMin = ms(20.0)
        val otherMax = ms(4.0)
        while (scanPos < writePos) {
            val v = f(scanPos)
            if (v in 1750f..2050f) {
                leaderCount++
                lowCount = 0
                otherCount = 0
            } else if (v in 1000f..1450f) {
                if (lowCount == 0) lowStart = scanPos
                lowCount++
                otherCount = 0
                if (lowCount >= lowMin && leaderCount >= leaderMin) {
                    visT0 = lowStart
                    scanPos++
                    leaderCount = 0
                    lowCount = 0
                    otherCount = 0
                    st = St.VIS
                    onPhase(RxPhase.VIS, 0)
                    return true
                }
            } else {
                otherCount++
                if (otherCount > otherMax) {
                    leaderCount = 0
                    lowCount = 0
                }
            }
            scanPos++
        }
        return false
    }

    // ---------- lectura del código VIS ----------
    private fun stepVis(): Boolean {
        if (writePos < visT0 + ms(315.0)) return false

        fun bitMean(index: Int): Float {
            val center = visT0 + ms(30.0 * (1 + index) + 15.0)
            return mean(center - ms(8.0), center + ms(8.0))
        }

        val startOk = mean(visT0 + ms(7.0), visT0 + ms(23.0)) < 1350f
        var code = 0
        var ones = 0
        for (i in 0 until 7) {
            val b = if (bitMean(i) < 1200f) 1 else 0
            ones += b
            code = code or (b shl i)
        }
        val parityBit = if (bitMean(7) < 1200f) 1 else 0
        val parityOk = (ones + parityBit) % 2 == 0
        val stopOk = mean(visT0 + ms(277.0), visT0 + ms(293.0)) < 1350f

        if (startOk && parityOk && stopOk && code == 8) {
            java.util.Arrays.fill(pixels, 0xFF000000.toInt())
            java.util.Arrays.fill(yLev, 16f)
            java.util.Arrays.fill(ry, 128f)
            java.util.Arrays.fill(by, 128f)
            lineIdx = 0
            miss = 0
            expectedSync = visT0 + ms(309.0)
            st = St.IMAGE
            onPhase(RxPhase.RECEIVING, 0)
            emit(false)
        } else {
            st = St.SEARCH
            onPhase(RxPhase.LISTENING, 0)
        }
        return true
    }

    // ---------- imagen ----------
    private fun stepImage(): Boolean {
        if (writePos < expectedSync + ms(165.0)) return false

        val found = findSyncEnd(expectedSync)
        val s = found ?: expectedSync
        if (found == null) miss++ else miss = 0

        if (miss >= 8) {
            // señal perdida: se conserva lo recibido hasta ahora
            emit(false)
            endImage(false)
            return true
        }

        decodeLine(s)
        lineIdx++
        expectedSync = s + ms(150.0)
        onPhase(RxPhase.RECEIVING, lineIdx)

        if (lineIdx >= H) {
            emit(true)
            endImage(true)
        } else if (lineIdx % 2 == 0) {
            emit(false)
        }
        return true
    }

    private fun endImage(complete: Boolean) {
        st = St.SEARCH
        leaderCount = 0; lowCount = 0; otherCount = 0
        scanPos = max(scanPos, expectedSync - ms(20.0))
        if (scanPos > writePos) scanPos = writePos
        onPhase(if (complete) RxPhase.DONE else RxPhase.LISTENING, if (complete) H else lineIdx)
    }

    /** Busca el flanco de subida que cierra el pulso de sync (1200 -> 1500 Hz). */
    private fun findSyncEnd(expected: Int): Int? {
        val from = expected - ms(12.0)
        val to = expected + ms(12.0)
        val pre = ms(3.0)
        for (p in from..to) {
            if (f(p) >= 1350f && f(p - 1) < 1350f && mean(p - pre, p) < 1300f) return p
        }
        return null
    }

    private fun levelOf(freq: Float): Float {
        val l = (freq - 1500f) / 800f * 255f
        return if (l < 0f) 0f else if (l > 255f) 255f else l
    }

    private fun decodeLine(s: Int) {
        val pxMs = SSTVEngine.PIXEL_MS
        val yStart = s + 3.0 * fsD / 1000.0
        for (x in 0 until W) {
            val a = (yStart + x * pxMs * fsD / 1000.0).toInt()
            val b = (yStart + (x + 1) * pxMs * fsD / 1000.0).toInt()
            yLev[lineIdx * W + x] = levelOf(mean(a, b))
        }

        val sepF = mean(s + ms(91.5), s + ms(94.5))
        val isRy = if (sepF in 1300f..2500f) sepF < 1900f else lineIdx % 2 == 0

        val cStart = s + 97.0 * fsD / 1000.0
        val target = if (isRy) ry else by
        for (j in 0 until W / 2) {
            val a = (cStart + j * pxMs * fsD / 1000.0).toInt()
            val b = (cStart + (j + 1) * pxMs * fsD / 1000.0).toInt()
            target[j] = levelOf(mean(a, b))
        }

        if (isRy) {
            renderRow(lineIdx)
        } else {
            if (lineIdx > 0) renderRow(lineIdx - 1)
            renderRow(lineIdx)
        }
    }

    private fun renderRow(row: Int) {
        for (x in 0 until W) {
            val y = yLev[row * W + x]
            val r0 = ry[x / 2]
            val b0 = by[x / 2]
            val yy = 298.082f * (y - 16f)
            val r = (yy + 408.583f * (r0 - 128f)) / 256f
            val g = (yy - 100.291f * (b0 - 128f) - 208.120f * (r0 - 128f)) / 256f
            val b = (yy + 516.411f * (b0 - 128f)) / 256f
            pixels[row * W + x] = (0xFF shl 24) or (clamp(r) shl 16) or (clamp(g) shl 8) or clamp(b)
        }
    }

    private fun clamp(v: Float): Int = if (v < 0f) 0 else if (v > 255f) 255 else v.toInt()

    private fun emit(complete: Boolean) {
        val bmp = Bitmap.createBitmap(pixels.copyOf(), W, H, Bitmap.Config.ARGB_8888)
        onImage(bmp, complete)
    }
}
