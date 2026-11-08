package org.underc0de.undercoderf.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

/**
 * Reproduce un buffer PCM. Es bloqueante: llamarlo desde un hilo de fondo.
 * Una instancia por reproducción; stop() puede llamarse desde cualquier hilo.
 */
class PcmPlayer(private val sampleRate: Int = 44100) {
    @Volatile private var stopped = false
    @Volatile private var track: AudioTrack? = null

    fun play(samples: ShortArray, onProgress: (Float) -> Unit = {}) {
        if (stopped || samples.isEmpty()) return
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        val bufBytes = maxOf(minBuf, 16384)
        val t = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufBytes)
            .build()
        track = t
        try {
            t.play()
            var offset = 0
            val chunk = 4096
            while (offset < samples.size && !stopped) {
                val n = minOf(chunk, samples.size - offset)
                val written = t.write(samples, offset, n)
                if (written < 0) break
                offset += written
                onProgress(offset.toFloat() / samples.size)
            }
            // esperar a que se vacíe el buffer interno
            var waited = 0
            while (!stopped && t.playbackHeadPosition < samples.size && waited < 3000) {
                Thread.sleep(20)
                waited += 20
            }
        } catch (_: Exception) {
        } finally {
            try { t.stop() } catch (_: Exception) {}
            try { t.release() } catch (_: Exception) {}
            track = null
        }
    }

    fun stop() {
        stopped = true
        try {
            track?.pause()
            track?.flush()
        } catch (_: Exception) {
        }
    }
}
