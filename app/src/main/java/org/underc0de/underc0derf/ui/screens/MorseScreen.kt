package org.underc0de.underc0derf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.underc0de.underc0derf.audio.Transmitter
import org.underc0de.underc0derf.morse.MorseEngine
import org.underc0de.underc0derf.ui.*

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
            .background(BrandBackground)
            .verticalScroll(rememberScrollState())
    ) {
        TopHeaderBar(status = if (mine) "TX" else "IDLE")

        Column(modifier = Modifier.padding(16.dp)) {
            ScreenTitle(text = "MODO CÓDIGO MORSE")

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Texto a transmitir", color = BrandPrimary, fontFamily = BrandFonts.Body) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !Transmitter.busy,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPrimary,
                    unfocusedBorderColor = BrandPrimary.copy(alpha = 0.4f),
                    disabledBorderColor = BrandPrimary.copy(alpha = 0.2f),
                    focusedTextColor = BrandText,
                    unfocusedTextColor = BrandText,
                    disabledTextColor = BrandText,
                    cursorColor = BrandPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "VELOCIDAD: ${wpm.toInt()} PPM",
                color = BrandText,
                fontFamily = BrandFonts.Body,
                fontSize = 12.sp
            )
            Slider(
                value = wpm,
                onValueChange = { wpm = it },
                valueRange = 5f..25f,
                steps = 19,
                enabled = !Transmitter.busy,
                colors = SliderDefaults.colors(
                    thumbColor = BrandPrimary,
                    activeTrackColor = BrandPrimary,
                    inactiveTrackColor = BrandSurface
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            CyberCard {
                Text("TRADUCCIÓN", color = BrandPrimary, fontFamily = BrandFonts.Body, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = symbols.ifEmpty { "—" },
                    color = BrandText,
                    fontFamily = BrandFonts.Body,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            CyberButton(
                text = "[ ENVIAR MORSE ]",
                enabled = text.isNotBlank() && symbols.isNotEmpty() && !Transmitter.busy,
                container = BrandSuccess,
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
                    container = BrandAlert,
                    content = BrandText,
                    onClick = { Transmitter.stop() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            TxStatusCard("MORSE")
        }
    }
}
