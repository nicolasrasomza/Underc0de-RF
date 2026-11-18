package org.underc0de.underc0derf.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.underc0de.underc0derf.audio.RxPhase
import org.underc0de.underc0derf.audio.SstvReceiver
import org.underc0de.underc0derf.ui.*
import org.underc0de.underc0derf.util.ImageUtils

@Composable
fun ReceiveScreen() {
    val context = LocalContext.current
    val view = LocalView.current
    val listening = SstvReceiver.listening
    val bitmap = SstvReceiver.image
    var message by remember { mutableStateOf<String?>(null) }

    fun saveCurrent() {
        val b = SstvReceiver.image
        if (b != null) {
            message = if (ImageUtils.saveToGallery(context, b)) {
                "Imagen guardada en Pictures/Underc0deRF"
            } else {
                "No se pudo guardar la imagen"
            }
        }
    }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) SstvReceiver.start() else message = "Sin permiso de micrófono no se puede recibir"
    }
    val storageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) saveCurrent() else message = "Sin permiso de almacenamiento no se puede guardar"
    }

    DisposableEffect(listening) {
        view.keepScreenOn = listening
        onDispose { view.keepScreenOn = false }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .verticalScroll(rememberScrollState())
    ) {
        TopHeaderBar(status = if (listening) "RX" else "IDLE")

        Column(modifier = Modifier.padding(16.dp)) {
            ScreenTitle(text = "RECEPCIÓN SSTV")
            Text(
                text = "Robot 36 · escucha el audio del handy por el micrófono",
                color = BrandText,
                fontFamily = BrandFonts.Subtitle,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            val statusText = when {
                !listening -> "DETENIDO"
                SstvReceiver.phase == RxPhase.LISTENING -> "ESCUCHANDO... ESPERANDO SEÑAL SSTV"
                SstvReceiver.phase == RxPhase.VIS -> "SEÑAL DETECTADA (CABECERA VIS)"
                SstvReceiver.phase == RxPhase.RECEIVING -> "RECIBIENDO LÍNEA ${SstvReceiver.lines}/240"
                else -> "IMAGEN COMPLETA · ESCUCHANDO DE NUEVO"
            }
            CyberCard(borderColor = if (listening) BrandSuccess else BrandPrimary) {
                Text(
                    text = statusText,
                    color = if (listening) BrandSuccess else BrandText,
                    fontFamily = BrandFonts.Body,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                if (listening) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = SstvReceiver.lines / 240f,
                        modifier = Modifier.fillMaxWidth(),
                        color = BrandSuccess,
                        trackColor = BrandBackground
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "NIVEL DE ENTRADA",
                        color = BrandPrimary,
                        fontFamily = BrandFonts.Body,
                        fontSize = 10.sp
                    )
                    LinearProgressIndicator(
                        progress = (SstvReceiver.level * 3f).coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth(),
                        color = BrandPrimary,
                        trackColor = BrandBackground
                    )
                    if (SstvReceiver.level < 0.02f) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Nivel muy bajo: subí el volumen del handy o acercalo al micrófono.",
                            color = BrandAlert,
                            fontFamily = BrandFonts.Body,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val shape = BrandCardShape
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .border(1.dp, BrandPrimary.copy(alpha = 0.6f), shape)
                    .background(BrandSurface, shape),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Imagen recibida",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = "SIN IMAGEN RECIBIDA",
                        color = BrandText,
                        fontFamily = BrandFonts.Body,
                        fontSize = 13.sp
                    )
                }
            }

            SstvReceiver.error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = BrandAlert, fontFamily = BrandFonts.Body, fontSize = 12.sp)
            }
            message?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = BrandSuccess, fontFamily = BrandFonts.Body, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!listening) {
                CyberButton(
                    text = "[ INICIAR ESCUCHA ]",
                    container = BrandSuccess,
                    onClick = {
                        message = null
                        val granted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) SstvReceiver.start() else micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )
            } else {
                CyberButton(
                    text = "[ DETENER ESCUCHA ]",
                    container = BrandAlert,
                    content = BrandText,
                    onClick = { SstvReceiver.stop() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            CyberButton(
                text = "[ GUARDAR IMAGEN ]",
                enabled = bitmap != null,
                outlined = true,
                onClick = {
                    val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    if (needsPermission) {
                        storageLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } else {
                        saveCurrent()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            CyberCard(borderColor = BrandWarning) {
                Text(
                    text = "Dejá la pantalla encendida mientras escuchás. Desactivá la 'Doble Escucha' " +
                        "del handy y probá con volumen medio-alto. La imagen se guarda en Pictures/Underc0deRF.",
                    color = BrandText,
                    fontFamily = BrandFonts.Body,
                    fontSize = 11.sp
                )
            }
        }
    }
}
