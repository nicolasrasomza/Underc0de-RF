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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.underc0de.undercoderf.ui.CardBackground
import org.underc0de.undercoderf.ui.NeonCyan
import org.underc0de.undercoderf.ui.NeonPink
import org.underc0de.undercoderf.ui.TerminalBlack
import org.underc0de.undercoderf.ui.screens.*

private data class Tab(val route: String, val label: String)

private val tabs = listOf(
    Tab("home", "INICIO"),
    Tab("receive", "RECIBIR"),
    Tab("send", "ENVIAR"),
    Tab("morse", "MORSE"),
    Tab("sos", "SOS")
)

private fun NavHostController.goTo(route: String) {
    navigate(route) {
        popUpTo("home") { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route ?: "home"

            Scaffold(
                containerColor = TerminalBlack,
                bottomBar = {
                    NavigationBar(containerColor = TerminalBlack) {
                        tabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            val activeColor = if (tab.route == "sos") NeonPink else NeonCyan
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navController.goTo(tab.route) },
                                colors = NavigationBarItemDefaults.colors(indicatorColor = CardBackground),
                                icon = {
                                    Text(
                                        text = tab.label,
                                        color = if (selected) activeColor else Color.Gray,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    )
                                }
                            )
                        }
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
                            onNavigateToReceive = { navController.goTo("receive") },
                            onNavigateToSend = { navController.goTo("send") },
                            onTriggerSOS = { navController.goTo("sos") }
                        )
                    }
                    composable("receive") { ReceiveScreen() }
                    composable("send") { SendScreen() }
                    composable("morse") { MorseScreen() }
                    composable("sos") { SosScreen() }
                }
            }
        }
    }
}
