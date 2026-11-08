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
import org.underc0de.undercoderf.ui.*

@Composable
fun HomeScreen(
    onNavigateToReceive: () -> Unit,
    onNavigateToSend: () -> Unit,
    onTriggerSOS: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 60.dp)
    ) {
        TopHeaderBar(status = "IDLE")

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
                Button(
                    onClick = onNavigateToReceive,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("[ RECIBIR ]", color = TerminalBlack, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNavigateToSend,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("[ ENVIAR ]", color = TerminalBlack, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CyberCard(modifier = Modifier.weight(1f)) {
                    Text("RX IMÁGENES", color = NeonCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("14", color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                CyberCard(modifier = Modifier.weight(1f)) {
                    Text("TX IMÁGENES", color = NeonCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("5", color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            var showSOSDialog by remember { mutableStateOf(false) }

            Button(
                onClick = { showSOSDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
            ) {
                Text("[ TRANSMITIR SOS EMERGENCY ]", color = TextWhite, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }

            if (showSOSDialog) {
                AlertDialog(
                    onDismissRequest = { showSOSDialog = false },
                    title = {
                        Text(
                            text = "[!] ADVERTENCIA SOS",
                            color = NeonPink,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "Una advertencia: el SOS es una señal de socorro real. Probarlo al aire en frecuencias de uso general puede provocar que alguien acuda o que se active una alerta.",
                            color = TextWhite,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSOSDialog = false
                                onTriggerSOS()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                        ) {
                            Text("TRANSMITIR", fontFamily = FontFamily.Monospace)
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showSOSDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = CardBackground)
                        ) {
                            Text("CANCELAR", color = NeonCyan, fontFamily = FontFamily.Monospace)
                        }
                    },
                    containerColor = TerminalBlack
                )
            }

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
