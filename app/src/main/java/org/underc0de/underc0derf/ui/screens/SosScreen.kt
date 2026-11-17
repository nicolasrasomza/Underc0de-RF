package org.underc0de.underc0derf.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.underc0de.underc0derf.audio.Transmitter
import org.underc0de.underc0derf.service.SOSForegroundService
import org.underc0de.underc0derf.sos.CoordFormat
import org.underc0de.underc0derf.sos.LocationTracker
import org.underc0de.underc0derf.sos.SosPayload
import org.underc0de.underc0derf.sos.SosState
import org.underc0de.underc0derf.ui.*

private fun startSos(context: Context) {
    ContextCompat.startForegroundService(context, Intent(context, SOSForegroundService::class.java))
}

private fun stopSos(context: Context) {
    Transmitter.stop()
    context.stopService(Intent(context, SOSForegroundService::class.java))
}

@Composable
fun SosScreen() {
    val context = LocalContext.current
    val sosActive = Transmitter.current == "SOS"
    val withGps = SosState.withGps
    var showConfirm by remember { mutableStateOf(false) }
    var locDenied by remember { mutableStateOf(false) }

    val locLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            SosState.withGps = true
            locDenied = false
            LocationTracker.start(context)
        } else {
            SosState.withGps = false
            locDenied = true
        }
    }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> startSos(context) }

    DisposableEffect(withGps, sosActive) {
        if (withGps) LocationTracker.start(context)
        onDispose {
            if (Transmitter.current != "SOS") LocationTracker.stop()
        }
    }

    val loc = LocationTracker.current()
    val payload = SosPayload.preview()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .verticalScroll(rememberScrollState())
    ) {
        TopHeaderBar(status = if (sosActive) "SOS" else "IDLE")

        Column(modifier = Modifier.padding(16.dp)) {
            ScreenTitle(text = "SOS · SEÑAL DE SOCORRO", color = BrandAlert)
            Text(
                text = "Repite SOS en Morse con una pausa breve entre repeticiones hasta que lo detengas.",
                color = BrandText,
                fontFamily = BrandFonts.Subtitle,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            CyberCard(borderColor = if (withGps) BrandSuccess else BrandPrimary) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = withGps,
                        onCheckedChange = { on ->
                            if (on) {
                                if (LocationTracker.hasPermission(context)) {
                                    SosState.withGps = true
                                    locDenied = false
                                } else {
                                    locLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            } else {
                                SosState.withGps = false
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BrandBackground,
                            checkedTrackColor = BrandSuccess
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Transmitir posición GPS",
                        color = BrandText,
                        fontFamily = BrandFonts.Body,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (withGps) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val line = when {
                        !LocationTracker.hasPermission(context) -> "SIN PERMISO DE UBICACIÓN"
                        loc == null && !LocationTracker.gpsEnabled ->
                            "GPS DESACTIVADO: activá la ubicación del teléfono"
                        loc == null -> "BUSCANDO SEÑAL GPS... mientras tanto se emite solo SOS"
                        else -> "POS: " + CoordFormat.pretty(loc.latitude, loc.longitude)
                    }
                    Text(
                        text = line,
                        color = if (loc != null) BrandSuccess else BrandAlert,
                        fontFamily = BrandFonts.Body,
                        fontSize = 12.sp
                    )
                }
                if (locDenied) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Permiso de ubicación denegado.",
                        color = BrandAlert,
                        fontFamily = BrandFonts.Body,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            CyberCard {
                Text("SE TRANSMITE", color = BrandPrimary, fontFamily = BrandFonts.Body, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = payload,
                    color = BrandText,
                    fontFamily = BrandFonts.Body,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!sosActive) {
                CyberButton(
                    text = "[ INICIAR SOS CONTINUO ]",
                    enabled = !Transmitter.busy,
                    container = BrandAlert,
                    content = BrandText,
                    onClick = { showConfirm = true }
                )
            } else {
                CyberButton(
                    text = "[ DETENER SOS ]",
                    container = BrandAlert,
                    content = BrandText,
                    onClick = { stopSos(context) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            TxStatusCard("SOS")
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = {
                Text(
                    text = "[!] ADVERTENCIA SOS",
                    color = BrandAlert,
                    fontFamily = BrandFonts.Title,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "Una advertencia: el SOS es una señal de socorro real. Probarlo al aire en " +
                        "frecuencias de uso general puede provocar que alguien acuda o que se active una alerta.",
                    color = BrandText,
                    fontFamily = BrandFonts.Body,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirm = false
                        val needsNotif = Build.VERSION.SDK_INT >= 33 &&
                            ContextCompat.checkSelfPermission(
                                context, Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        if (needsNotif) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            startSos(context)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandAlert, contentColor = BrandText)
                ) {
                    Text("TRANSMITIR", fontFamily = BrandFonts.Subtitle, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandSurface)
                ) {
                    Text("CANCELAR", color = BrandPrimary, fontFamily = BrandFonts.Subtitle, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = BrandSurface
        )
    }
}
