package com.zybooks.quickdraw.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zybooks.quickdraw.ui.game.GameScreen
import com.zybooks.quickdraw.ui.home.HomeScreen
//import com.zybooks.quickdraw.ui.leaderboard.LeaderboardScreen
import com.zybooks.quickdraw.ui.settings.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToGameSetup = { navController.navigate("game") },
                onNavigateToLeaderboard = { navController.navigate("leaderboard") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("game") {
            GameScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

//        composable("leaderboard") {
//            LeaderboardScreen(
//                onNavigateBack = { navController.popBackStack() }
//            )
//        }
//
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}