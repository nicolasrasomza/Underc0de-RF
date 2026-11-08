package org.underc0de.undercoderf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.underc0de.undercoderf.ui.*
import org.underc0de.undercoderf.audio.AudioEngine
import org.underc0de.undercoderf.morse.MorseEngine

@Composable
fun MorseScreen() {
    var textInput by remember { mutableStateOf("UNDERC0DE LABORATORY") }
    var includeLocation by remember { mutableStateOf(true) }
    var countdownValue by remember { mutableStateOf<Int?>(null) }
    
    val morseEngine = remember { MorseEngine() }
    val audioEngine = remember { AudioEngine() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "MODO CÓDIGO MORSE",
            color = NeonCyan,
            fontFamily = FontFamily.Monospace,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Texto a transmitir", color = NeonCyan, fontFamily = FontFamily.Monospace) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = CardBackground,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = includeLocation,
                onCheckedChange = { includeLocation = it },
                colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Adjuntar Ubicación GPS (Lat/Long)",
                color = TextWhite,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                countdownValue = 3
                Thread {
                    for (i in 3 downTo 1) {
                        Thread.sleep(1000)
                        countdownValue = i - 1
                    }
                    countdownValue = null

                    val finalPayload = if (includeLocation) {
                        "SOS SOS SOS 32 53 10 S 68 50 40 W K"
                    } else {
                        textInput
                    }

                    val pcm = morseEngine.textToAudio(finalPayload, wpm = 10)
                    audioEngine.playPCM(pcm)
                }.start()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
        ) {
            Text("[ TRANSMITIR CW MORSE ]", color = TerminalBlack, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        countdownValue?.let { seconds ->
            if (seconds > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                CyberCard(borderColor = NeonPink) {
                    Text(
                        text = "PREPARÁ TU PTT DEL HANDY: $seconds S",
                        color = NeonPink,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
