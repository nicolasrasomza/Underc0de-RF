package org.underc0de.undercoderf.audio

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Escucha el micrófono / entrada de audio y decodifica SSTV Robot 36.
 * Vive a nivel de proceso: seguir escuchando al cambiar de pestaña.
 * (Android corta el micrófono si la app pasa a segundo plano: dejá la pantalla encendida.)
 */
object SstvReceiver {
    private const val SAMPLE_RATE = 44100

    var listening by mutableStateOf(false)
        private set
    var phase by mutableStateOf(RxPhase.LISTENING)
        private set
    var lines by mutableIntStateOf(0)
        private set
    var level by mutableFloatStateOf(0f)
        private set
    var image by mutableStateOf<Bitmap?>(null)
        private set
    var imageComplete by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    @Volatile private var running = false
    private var thread: Thread? = null

    @SuppressLint("MissingPermission")
    @Synchronized
    fun start() {
        if (running) return
        error = null
        val minBuf = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        val bufBytes = maxOf(minBuf, 16384)
        val record = createRecord(bufBytes)
        if (record == null) {
            error = "No se pudo abrir el micrófono"
            return
        }

        val decoder = SstvDecoder(
            onPhase = { p, l ->
                phase = p
                lines = l
            },
            onImage = { bmp, complete ->
                image = bmp
                imageComplete = complete
            }
        )

        running = true
        listening = true
        phase = RxPhase.LISTENING
        lines = 0
        thread = Thread {
            val buffer = ShortArray(bufBytes / 2)
            try {
                record.startRecording()
                while (running) {
                    val n = record.read(buffer, 0, buffer.size)
                    if (n > 0) {
                        level = decoder.feed(buffer, n)
                    } else if (n < 0) {
                        error = "Error de lectura de audio"
                        break
                    }
                }
            } catch (e: Exception) {
                error = "Error de audio: ${e.message}"
            } finally {
                try { record.stop() } catch (_: Exception) {}
                record.release()
                running = false
                listening = false
                level = 0f
            }
        }.also {
            it.name = "sstv-rx"
            it.start()
        }
    }

    @Synchronized
    fun stop() {
        running = false
    }

    @SuppressLint("MissingPermission")
    private fun createRecord(bufBytes: Int): AudioRecord? {
        val sources = intArrayOf(MediaRecorder.AudioSource.UNPROCESSED, MediaRecorder.AudioSource.MIC)
        for (src in sources) {
            try {
                val r = AudioRecord(
                    src, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufBytes
                )
                if (r.state == AudioRecord.STATE_INITIALIZED) return r
                r.release()
            } catch (_: Exception) {
            }
        }
        return null
    }
}
