package org.underc0de.undercoderf.audio

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Transmisor único de la app (SSTV, Morse y SOS). Vive a nivel de proceso, por eso la
 * transmisión sigue aunque cambies de pestaña. Antes de emitir hay una cuenta regresiva
 * de COUNTDOWN_SECONDS para dar tiempo a apretar el PTT del handy (ponelo en 0 para
 * transmitir al instante).
 */
object Transmitter {
    enum class Phase { IDLE, COUNTDOWN, PLAYING, GAP }

    const val COUNTDOWN_SECONDS = 3

    var phase by mutableStateOf(Phase.IDLE)
        private set
    var countdown by mutableIntStateOf(0)
        private set
    var current by mutableStateOf("")
        private set
    var progress by mutableFloatStateOf(0f)
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lock = Any()
    private var generation = 0
    private var job: Job? = null
    private var player: PcmPlayer? = null

    val busy: Boolean get() = phase != Phase.IDLE

    /**
     * @param label  "SSTV", "MORSE" o "SOS"
     * @param loop   repetir hasta que se llame a stop()
     * @param build  genera el audio; se vuelve a llamar en cada repetición
     */
    fun transmit(label: String, loop: Boolean = false, gapMs: Long = 3000, build: () -> ShortArray) {
        stop()
        val gen: Int
        synchronized(lock) {
            gen = generation
            current = label
            phase = Phase.COUNTDOWN
            countdown = COUNTDOWN_SECONDS
            progress = 0f
        }
        job = scope.launch {
            try {
                var pre: ShortArray? = build()
                for (i in COUNTDOWN_SECONDS downTo 1) {
                    if (gen != generation) return@launch
                    phase = Phase.COUNTDOWN
                    countdown = i
                    delay(1000)
                }
                do {
                    if (!isActive || gen != generation) return@launch
                    val samples = pre ?: build()
                    pre = null
                    progress = 0f
                    phase = Phase.PLAYING
                    val p = PcmPlayer()
                    synchronized(lock) {
                        if (gen != generation) return@launch
                        player = p
                    }
                    p.play(samples) { progress = it }
                    if (loop && isActive && gen == generation) {
                        phase = Phase.GAP
                        progress = 0f
                        delay(gapMs)
                    }
                } while (loop && isActive && gen == generation)
            } finally {
                synchronized(lock) {
                    if (gen == generation) resetState()
                }
            }
        }
    }

    fun stop() {
        synchronized(lock) {
            generation++
            player?.stop()
            player = null
            job?.cancel()
            job = null
            resetState()
        }
    }

    private fun resetState() {
        phase = Phase.IDLE
        countdown = 0
        progress = 0f
        current = ""
    }
}
