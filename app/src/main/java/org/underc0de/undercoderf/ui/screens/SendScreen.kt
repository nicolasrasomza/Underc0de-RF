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
fun SendScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .padding(16.dp)
    ) {
        TopHeaderBar(status = "TX")

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "TRANSMISIÓN SSTV",
            color = NeonCyan,
            fontFamily = FontFamily.Monospace,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
        ) {
            Text("[ SELECCIONAR IMAGEN ]", color = TerminalBlack, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
    }
}
