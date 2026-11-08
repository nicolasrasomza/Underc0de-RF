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
import org.underc0de.undercoderf.audio.SstvReceiver
import org.underc0de.undercoderf.audio.Transmitter
import org.underc0de.undercoderf.ui.*

@Composable
fun HomeScreen(
    onNavigateToReceive: () -> Unit,
    onNavigateToSend: () -> Unit,
    onTriggerSOS: () -> Unit
) {
    val status = when {
        Transmitter.busy -> "TX"
        SstvReceiver.listening -> "RX"
        else -> "IDLE"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .verticalScroll(rememberScrollState())
    ) {
        TopHeaderBar(status = status)

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "UNDERCODE RF",
                color = TextWhite,
                fontFamily = FontFamily.Monospace,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            CyberCard(borderColor = WarningAmber) {
                Text(
                    text = "[!] AVISO DE CONFIGURACIÓN",
                    color = WarningAmber,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Desactivá la 'Doble Escucha' (Dual Watch) en tu handy bibanda para evitar interrupciones en la recepción de audio SSTV.",
                    color = TextWhite,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CyberButton(
                    text = "[ RECIBIR ]",
                    onClick = onNavigateToReceive,
                    modifier = Modifier.weight(1f)
                )
                CyberButton(
                    text = "[ ENVIAR ]",
                    onClick = onNavigateToSend,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            CyberButton(
                text = "[ SOS EMERGENCIA ]",
                onClick = onTriggerSOS,
                container = NeonPink,
                content = TextWhite
            )

            Spacer(modifier = Modifier.height(16.dp))

            CyberCard(borderColor = NeonGreen) {
                Text("UNDERC0DE - SSTV.LOG", color = NeonGreen, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("$ tail -f /var/log/radio.log", color = TextWhite, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                Text("14:50:18 radio listo. esperando órdenes...", color = NeonCyan, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                Text("14:52:01 modo seleccionado: Robot36", color = TextWhite, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            }
        }
    }
}
