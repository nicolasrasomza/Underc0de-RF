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
import org.underc0de.underc0derf.audio.SstvReceiver
import org.underc0de.underc0derf.audio.Transmitter
import org.underc0de.underc0derf.ui.*

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
            .background(BrandBackground)
            .verticalScroll(rememberScrollState())
    ) {
        TopHeaderBar(status = status)

        Column(modifier = Modifier.padding(16.dp)) {
            ScreenTitle(text = "UNDERC0DE RF", color = BrandText, fontSize = 30.sp)
            Spacer(modifier = Modifier.height(12.dp))

            CyberCard(borderColor = BrandWarning) {
                Text(
                    text = "[!] AVISO DE CONFIGURACIÓN",
                    color = BrandWarning,
                    fontFamily = BrandFonts.Subtitle,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Desactivá la 'Doble Escucha' (Dual Watch) en tu handy bibanda para evitar interrupciones en la recepción de audio SSTV.",
                    color = BrandText,
                    fontFamily = BrandFonts.Body,
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
                container = BrandAlert,
                content = BrandText
            )

            Spacer(modifier = Modifier.height(16.dp))

            CyberCard(borderColor = BrandSuccess) {
                Text("UNDERC0DE - SSTV.LOG", color = BrandSuccess, fontFamily = BrandFonts.Accent, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("$ tail -f /var/log/radio.log", color = BrandText, fontFamily = BrandFonts.Accent, fontWeight = FontWeight.Medium, fontSize = 11.sp)
                Text("14:50:18 radio listo. esperando órdenes...", color = BrandPrimary, fontFamily = BrandFonts.Accent, fontWeight = FontWeight.Medium, fontSize = 11.sp)
                Text("14:52:01 modo seleccionado: Robot36", color = BrandText, fontFamily = BrandFonts.Accent, fontWeight = FontWeight.Medium, fontSize = 11.sp)
            }
        }
    }
}
