package org.underc0de.undercoderf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.underc0de.undercoderf.ui.TerminalBlack
import org.underc0de.undercoderf.ui.NeonCyan
import org.underc0de.undercoderf.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            Scaffold(
                containerColor = TerminalBlack,
                bottomBar = {
                    NavigationBar(containerColor = TerminalBlack) {
                        NavigationBarItem(
                            selected = true,
                            onClick = { navController.navigate("home") },
                            icon = { Text("INICIO", color = NeonCyan, fontFamily = FontFamily.Monospace) }
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { navController.navigate("receive") },
                            icon = { Text("RECIBIR", color = Color.Gray, fontFamily = FontFamily.Monospace) }
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { navController.navigate("send") },
                            icon = { Text("ENVIAR", color = Color.Gray, fontFamily = FontFamily.Monospace) }
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { navController.navigate("morse") },
                            icon = { Text("MORSE", color = Color.Gray, fontFamily = FontFamily.Monospace) }
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { navController.navigate("more") },
                            icon = { Text("MÁS", color = Color.Gray, fontFamily = FontFamily.Monospace) }
                        )
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("home") {
                        HomeScreen(
                            onNavigateToReceive = { navController.navigate("receive") },
                            onNavigateToSend = { navController.navigate("send") },
                            onTriggerSOS = { navController.navigate("morse") }
                        )
                    }
                    composable("receive") { ReceiveScreen() }
                    composable("send") { SendScreen() }
                    composable("morse") { MorseScreen() }
                    composable("more") { MoreScreen() }
                }
            }
        }
    }
}
