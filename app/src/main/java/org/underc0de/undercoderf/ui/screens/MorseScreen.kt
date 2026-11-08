package org.underc0de.undercoderf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.underc0de.undercoderf.audio.Transmitter
import org.underc0de.undercoderf.morse.MorseEngine
import org.underc0de.undercoderf.ui.*

@Composable
fun MorseScreen() {
    var text by remember { mutableStateOf("") }
    var wpm by remember { mutableFloatStateOf(10f) }
    val engine = remember { MorseEngine() }
    val symbols = remember(text) { engine.toSymbols(text) }
    val mine = Transmitter.current == "MORSE"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .verticalScroll(rememberScrollState())
    ) {
        TopHeaderBar(status = if (mine) "TX" else "IDLE")

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "MODO CÓDIGO MORSE",
                color = NeonCyan,
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Texto a transmitir", color = NeonCyan, fontFamily = FontFamily.Monospace) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !Transmitter.busy,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = NeonCyan.copy(alpha = 0.4f),
                    disabledBorderColor = NeonCyan.copy(alpha = 0.2f),
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    disabledTextColor = TextWhite,
                    cursorColor = NeonCyan
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "VELOCIDAD: ${wpm.toInt()} PPM",
                color = TextWhite,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            Slider(
                value = wpm,
                onValueChange = { wpm = it },
                valueRange = 5f..25f,
                steps = 19,
                enabled = !Transmitter.busy,
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonCyan,
                    inactiveTrackColor = CardBackground
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            CyberCard {
                Text("TRADUCCIÓN", color = NeonCyan, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = symbols.ifEmpty { "—" },
                    color = TextWhite,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            CyberButton(
                text = "[ ENVIAR MORSE ]",
                enabled = text.isNotBlank() && symbols.isNotEmpty() && !Transmitter.busy,
                container = NeonGreen,
                onClick = {
                    val t = text
                    val w = wpm.toInt()
                    Transmitter.transmit("MORSE") { MorseEngine().textToAudio(t, w) }
                }
            )

            if (mine) {
                Spacer(modifier = Modifier.height(10.dp))
                CyberButton(
                    text = "[ DETENER ]",
                    container = NeonPink,
                    content = TextWhite,
                    onClick = { Transmitter.stop() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            TxStatusCard("MORSE")
        }
    }
}
