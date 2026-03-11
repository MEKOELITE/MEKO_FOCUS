package com.meko.focus.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.meko.focus.presentation.screen.ChartScreen
import com.meko.focus.presentation.screen.TimerScreen

sealed class Screen(val route: String) {
    object Timer : Screen("timer")
    object Chart : Screen("chart")
}

@Composable
fun FocusNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Timer.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Timer.route) {
            TimerScreen(
                onNavigateToChart = {
                    navController.navigate(Screen.Chart.route)
                }
            )
        }

        composable(Screen.Chart.route) {
            ChartScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}