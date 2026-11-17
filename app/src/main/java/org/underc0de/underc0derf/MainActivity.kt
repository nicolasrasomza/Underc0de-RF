package org.underc0de.underc0derf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.underc0de.underc0derf.ui.*
import org.underc0de.underc0derf.ui.screens.*

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
        BrandFonts.init(applicationContext)
        setContent {
            Underc0deTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route ?: "home"

                Scaffold(
                    containerColor = BrandBackground,
                    bottomBar = {
                        NavigationBar(containerColor = BrandBackground) {
                            tabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                val activeColor = if (tab.route == "sos") BrandAlert else BrandPrimary
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navController.goTo(tab.route) },
                                    colors = NavigationBarItemDefaults.colors(indicatorColor = BrandSurface),
                                    icon = {
                                        Text(
                                            text = tab.label,
                                            color = if (selected) activeColor else BrandMuted,
                                            fontFamily = BrandFonts.Body,
                                            fontWeight = FontWeight.Medium,
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
}
