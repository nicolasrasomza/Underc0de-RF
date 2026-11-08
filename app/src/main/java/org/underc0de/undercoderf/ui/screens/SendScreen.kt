package org.underc0de.undercoderf.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.underc0de.undercoderf.audio.SSTVEngine
import org.underc0de.undercoderf.audio.Transmitter
import org.underc0de.undercoderf.ui.*
import org.underc0de.undercoderf.util.ImageUtils

@Composable
fun SendScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            loading = true
            error = null
            scope.launch {
                val bmp = withContext(Dispatchers.IO) { ImageUtils.loadForSstv(context, uri) }
                loading = false
                if (bmp != null) bitmap = bmp else error = "No se pudo abrir esa imagen"
            }
        }
    }

    val mine = Transmitter.current == "SSTV"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .verticalScroll(rememberScrollState())
    ) {
        TopHeaderBar(status = if (mine) "TX" else "IDLE")

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TRANSMISIÓN SSTV",
                color = NeonCyan,
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Modo Robot 36 · 320×240 · unos 37 segundos",
                color = TextWhite,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            val shape = androidx.compose.foundation.shape.CutCornerShape(topEnd = 12.dp, bottomStart = 12.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .border(1.dp, NeonCyan, shape)
                    .background(CardBackground, shape),
                contentAlignment = Alignment.Center
            ) {
                val bmp = bitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Imagen a transmitir",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = if (loading) "CARGANDO IMAGEN..." else "SIN IMAGEN SELECCIONADA",
                        color = TextWhite,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
            }

            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = NeonPink, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            CyberButton(
                text = "[ SELECCIONAR IMAGEN ]",
                enabled = !Transmitter.busy,
                onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            CyberButton(
                text = "[ ENVIAR SSTV ]",
                enabled = bitmap != null && !Transmitter.busy,
                container = NeonGreen,
                onClick = {
                    val bmp = bitmap
                    if (bmp != null) {
                        Transmitter.transmit("SSTV") { SSTVEngine().generateRobot36Audio(bmp) }
                    }
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
            TxStatusCard("SSTV")

            Spacer(modifier = Modifier.height(16.dp))
            CyberCard(borderColor = WarningAmber) {
                Text(
                    text = "Conectá la salida de audio del teléfono a la entrada de micrófono del handy " +
                        "(o acercalo a su micrófono), subí el volumen multimedia y apretá el PTT " +
                        "durante la cuenta regresiva.",
                    color = TextWhite,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }
    }
}
