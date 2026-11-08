package org.underc0de.undercoderf.morse

import kotlin.math.sin

class MorseEngine {
    private val sampleRate = 44100
    private val toneFreq = 700.0

    private val morseMap = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".",
        'F' to "..-.", 'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---",
        'K' to "-.-", 'L' to ".-..", 'M' to "--", 'N' to "-.", 'O' to "---",
        'P' to ".--.", 'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-",
        'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-", 'Y' to "-.--",
        'Z' to "--..", '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-",
        '5' to ".....", '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----.",
        '0' to "-----", ' ' to " "
    )

    fun textToAudio(text: String, wpm: Int = 10): ShortArray {
        val dotDurationMs = 1200.0 / wpm
        val samples = mutableListOf<Short>()

        val uppercaseText = text.uppercase()
        for (char in uppercaseText) {
            val code = morseMap[char] ?: continue
            if (char == ' ') {
                addSilence(samples, dotDurationMs * 7)
                continue
            }

            for (symbol in code) {
                when (symbol) {
                    '.' -> addTone(samples, toneFreq, dotDurationMs)
                    '-' -> addTone(samples, toneFreq, dotDurationMs * 3)
                }
                addSilence(samples, dotDurationMs)
            }
            addSilence(samples, dotDurationMs * 3)
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

    private fun addSilence(samples: MutableList<Short>, durationMs: Double) {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        for (i in 0 until numSamples) {
            samples.add(0)
        }
    }
}
