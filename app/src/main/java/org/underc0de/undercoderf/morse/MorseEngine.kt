package org.underc0de.undercoderf.morse

import org.underc0de.undercoderf.audio.ToneBuilder
import java.text.Normalizer

class MorseEngine {
    private val toneFreq = 700.0

    private val morseMap = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".",
        'F' to "..-.", 'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---",
        'K' to "-.-", 'L' to ".-..", 'M' to "--", 'N' to "-.", 'O' to "---",
        'P' to ".--.", 'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-",
        'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-", 'Y' to "-.--",
        'Z' to "--..",
        '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-",
        '5' to ".....", '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----.",
        '.' to ".-.-.-", ',' to "--..--", '?' to "..--..", '/' to "-..-.", '=' to "-...-",
        '+' to ".-.-.", '-' to "-....-", '@' to ".--.-.", ':' to "---...", '!' to "-.-.--",
        '(' to "-.--.", ')' to "-.--.-", '\'' to ".----.", '"' to ".-..-.", '&' to ".-...",
        ';' to "-.-.-."
    )

    private fun normalize(text: String): String {
        val noAccents = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return noAccents.uppercase().trim()
    }

    private fun words(text: String): List<String> =
        normalize(text).split(Regex("\\s+")).filter { it.isNotEmpty() }

    /** Texto en puntos y rayas para mostrar en pantalla (palabras separadas por " / "). */
    fun toSymbols(text: String): String =
        words(text).joinToString("  /  ") { w ->
            w.mapNotNull { morseMap[it] }.joinToString(" ")
        }

    fun textToAudio(text: String, wpm: Int = 10): ShortArray {
        val dot = 1200.0 / wpm
        val ws = words(text)
        val tb = ToneBuilder(44100, 5.0 + ws.sumOf { it.length } * 6.0 * dot / 1000.0)
        for ((wi, word) in ws.withIndex()) {
            val letters = word.filter { morseMap.containsKey(it) }
            for ((ci, ch) in letters.withIndex()) {
                val code = morseMap.getValue(ch)
                for ((si, sym) in code.withIndex()) {
                    tb.tone(toneFreq, if (sym == '.') dot else dot * 3, rampMs = 4.0)
                    if (si < code.length - 1) tb.silence(dot)
                }
                if (ci < letters.length - 1) tb.silence(dot * 3)
            }
            if (wi < ws.size - 1) tb.silence(dot * 7)
        }
        tb.silence(300.0)
        return tb.build()
    }
}
