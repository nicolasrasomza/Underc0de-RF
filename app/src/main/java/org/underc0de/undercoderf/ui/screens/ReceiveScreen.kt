package org.underc0de.undercoderf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.underc0de.undercoderf.ui.*

@Composable
fun ReceiveScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .padding(16.dp)
    ) {
        TopHeaderBar(status = "RX")

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "RECEPCIÓN SSTV (EN VIVO)",
            color = NeonCyan,
            fontFamily = FontFamily.Monospace,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        CyberCard(modifier = Modifier.weight(1f), borderColor = NeonCyan) {
            Text(
                text = "ESPERANDO SEÑAL DE AUDIO...",
                color = TextWhite,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
        }
    }
}
