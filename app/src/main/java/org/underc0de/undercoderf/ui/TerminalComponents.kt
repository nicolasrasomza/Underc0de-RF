package org.underc0de.undercoderf.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.underc0de.undercoderf.R
import org.underc0de.undercoderf.audio.Transmitter

val TerminalBlack = Color(0xFF070B10)
val CardBackground = Color(0xFF0D141F)
val NeonCyan = Color(0xFF00E5FF)
val NeonPink = Color(0xFFFF2A6D)
val NeonGreen = Color(0xFF05FFA1)
val WarningAmber = Color(0xFFFFB703)
val TextWhite = Color(0xFFE2F1FF)

@Composable
fun TopHeaderBar(status: String = "IDLE") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalBlack)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "SYS://UNDERC0DE.ORG · $status",
                color = NeonCyan,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "root@underc0de:~$ ./radio --scan",
                color = TextWhite,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_underc0de),
                contentDescription = "Logo Underc0de",
                modifier = Modifier.size(40.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.icono_underc0de_rf),
                contentDescription = "Icono Underc0de RF",
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = NeonCyan,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, CutCornerShape(topEnd = 12.dp, bottomStart = 12.dp)),
        color = CardBackground,
        shape = CutCornerShape(topEnd = 12.dp, bottomStart = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            content = content
        )
    }
}

@Composable
fun CyberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    container: Color = NeonCyan,
    content: Color = TerminalBlack
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = CardBackground,
            disabledContentColor = Color.Gray
        )
    ) {
        Text(text, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

/** Cuenta regresiva grande + progreso de la transmisión en curso (solo si es la de [label]). */
@Composable
fun TxStatusCard(label: String) {
    if (Transmitter.current != label) return
    when (Transmitter.phase) {
        Transmitter.Phase.COUNTDOWN -> CyberCard(borderColor = NeonPink) {
            Text(
                text = "PREPARÁ EL PTT DEL HANDY",
                color = NeonPink,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${Transmitter.countdown}",
                color = NeonPink,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 72.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Transmitter.Phase.PLAYING -> CyberCard(borderColor = NeonGreen) {
            Text(
                text = "● TRANSMITIENDO $label... ${(Transmitter.progress * 100).toInt()}%",
                color = NeonGreen,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = Transmitter.progress,
                modifier = Modifier.fillMaxWidth(),
                color = NeonGreen,
                trackColor = TerminalBlack
            )
        }
        Transmitter.Phase.GAP -> CyberCard(borderColor = NeonGreen) {
            Text(
                text = "● $label · pausa breve, repite enseguida...",
                color = NeonGreen,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
        }
        Transmitter.Phase.IDLE -> {}
    }
}
