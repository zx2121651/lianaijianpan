package com.lovekey.ime.ui.host

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "首页", Icons.Filled.Home)
    object Store : BottomNavItem("store", "装扮", Icons.Filled.Palette)
    object Phrases : BottomNavItem("phrases", "语料", Icons.Filled.Translate)
    object Dictionary : BottomNavItem("dictionary", "词库", Icons.AutoMirrored.Filled.LibraryBooks)
    object Profile : BottomNavItem("profile", "我的", Icons.Filled.Settings)
}

val items = listOf(
    BottomNavItem.Home,
    BottomNavItem.Store,
    BottomNavItem.Phrases,
    BottomNavItem.Dictionary,
    BottomNavItem.Profile
)

@Composable
fun MainScreen(context: Context, onNavigateToSmartInput: () -> Unit, onNavigateToDiy: () -> Unit, onNavigateToAddWord: () -> Unit, onNavigateToAbout: () -> Unit) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(context)
            }
            composable(BottomNavItem.Store.route) {
                StoreScreen(context, onNavigateToDiy)
            }
            composable(BottomNavItem.Phrases.route) {
                PhrasesScreen(context)
            }
            composable(BottomNavItem.Dictionary.route) {
                DictionaryScreen(context, onNavigateToAddWord)
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(context, onNavigateToSmartInput, onNavigateToAbout)
            }
        }
    }
}

@Composable
fun LovekeyHostApp(context: Context) {
    val rootNavController = rememberNavController()

    // We start with onboarding. If finished, we go to "main".
    NavHost(navController = rootNavController, startDestination = "onboarding") {
        composable("onboarding") {
            OnboardingScreen(
                context = context,
                onFinish = {
                    rootNavController.navigate("main") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainScreen(
                context = context,
                onNavigateToSmartInput = { rootNavController.navigate("smart_input") },
                onNavigateToDiy = { rootNavController.navigate("diy_theme") },
                onNavigateToAddWord = { rootNavController.navigate("add_word") },
                onNavigateToAbout = { rootNavController.navigate("about") }
            )
        }

        composable("smart_input") {
            SmartInputScreen(
                context = context,
                onBack = { rootNavController.popBackStack() }
            )
        }

        composable("diy_theme") {
            DiyThemeScreen(
                context = context,
                onBack = { rootNavController.popBackStack() }
            )
        }

        composable("add_word") {
            AddWordScreen(
                context = context,
                onBack = { rootNavController.popBackStack() }
            )
        }

        composable("about") {
            AboutScreen(onBack = { rootNavController.popBackStack() })
        }
    }
}
