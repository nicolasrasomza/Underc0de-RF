package org.underc0de.underc0derf.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.underc0de.underc0derf.R
import org.underc0de.underc0derf.audio.Transmitter

/**
 * Encabezado común: logo oficial de Underc0de arriba al centro (isologotipo blanco
 * sobre azul institucional) y una línea de estado como acento gráfico (Pixelify Sans).
 *
 * Manual de marca: tamaño mínimo digital del isotipo 70 px y zona de protección libre
 * alrededor del logo, por eso 72.dp y el padding vertical generoso.
 */
@Composable
fun TopHeaderBar(status: String = "IDLE") {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandBackground)
            .padding(top = 20.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_underc0de),
            contentDescription = "Logo Underc0de",
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "SYS://UNDERC0DE.ORG · $status",
            color = BrandPrimary,
            fontFamily = BrandFonts.Accent,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "root@underc0de:~$ ./radio --scan",
            color = BrandMuted,
            fontFamily = BrandFonts.Accent,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

/** Título de pantalla con la tipografía principal (Usuzi). */
@Composable
fun ScreenTitle(text: String, color: Color = BrandPrimary, fontSize: TextUnit = 22.sp) {
    Text(
        text = text,
        color = color,
        fontFamily = BrandFonts.Title,
        fontWeight = FontWeight.Normal,
        fontSize = fontSize
    )
}

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = BrandBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, BrandCardShape),
        color = BrandSurface,
        shape = BrandCardShape
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            content = content
        )
    }
}

/**
 * Botón con esquinas cortadas (sup-izq / inf-der).
 * - Relleno: acción principal ([container] de fondo, [content] para el texto).
 * - [outlined] = true: acción secundaria, fondo negro con borde y texto del color [container].
 */
@Composable
fun CyberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    container: Color = BrandPrimary,
    content: Color = BrandBlack,
    outlined: Boolean = false
) {
    val label: @Composable RowScope.() -> Unit = {
        Text(
            text = text,
            fontFamily = BrandFonts.Subtitle,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            textAlign = TextAlign.Center
        )
    }
    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.heightIn(min = 46.dp),
            enabled = enabled,
            shape = BrandButtonShape,
            border = BorderStroke(1.dp, if (enabled) container else BrandBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = BrandBlack,
                contentColor = container,
                disabledContainerColor = BrandBlack,
                disabledContentColor = BrandMuted
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            content = label
        )
    } else {
        Button(
            onClick = onClick,
            modifier = modifier.heightIn(min = 52.dp),
            enabled = enabled,
            shape = BrandButtonShape,
            border = if (enabled) null else BorderStroke(1.dp, BrandBorder),
            colors = ButtonDefaults.buttonColors(
                containerColor = container,
                contentColor = content,
                disabledContainerColor = BrandSurface,
                disabledContentColor = BrandMuted
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            content = label
        )
    }
}

/** Cuenta regresiva grande + progreso de la transmisión en curso (solo si es la de [label]). */
@Composable
fun TxStatusCard(label: String) {
    if (Transmitter.current != label) return
    when (Transmitter.phase) {
        Transmitter.Phase.COUNTDOWN -> CyberCard(borderColor = BrandAlert) {
            Text(
                text = "PREPARÁ EL PTT DEL HANDY",
                color = BrandAlert,
                fontFamily = BrandFonts.Subtitle,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${Transmitter.countdown}",
                color = BrandAlert,
                fontFamily = BrandFonts.Accent,
                fontWeight = FontWeight.Medium,
                fontSize = 72.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Transmitter.Phase.PLAYING -> CyberCard(borderColor = BrandSuccess) {
            Text(
                text = "● TRANSMITIENDO $label... ${(Transmitter.progress * 100).toInt()}%",
                color = BrandSuccess,
                fontFamily = BrandFonts.Body,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = Transmitter.progress,
                modifier = Modifier.fillMaxWidth(),
                color = BrandSuccess,
                trackColor = BrandBackground
            )
        }
        Transmitter.Phase.GAP -> CyberCard(borderColor = BrandSuccess) {
            Text(
                text = "● $label · pausa breve, repite enseguida...",
                color = BrandSuccess,
                fontFamily = BrandFonts.Body,
                fontSize = 13.sp
            )
        }
        Transmitter.Phase.IDLE -> {}
    }
}
