package com.lovekey.ime.ui.host

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun LovekeyHostApp(context: Context) {
    val navController = rememberNavController()

    // We will start with a Splash or Onboarding check
    NavHost(navController = navController, startDestination = "onboarding") {
        composable("onboarding") {
            OnboardingScreen(
                context = context,
                onFinish = {
                    navController.navigate("settings_home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("settings_home") {
            SettingsHomeScreen(
                context = context,
                onNavigateToSmartInput = { navController.navigate("smart_input") }
            )
        }

        composable("smart_input") {
            SmartInputScreen(
                context = context,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
