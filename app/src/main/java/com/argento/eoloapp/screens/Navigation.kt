package com.argento.eoloapp.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "LoginScreen"
    ) {
        composable(
            route = "LoginScreen"
        ) {
            LoginScreen(
                navController
            )
        }
        composable(
            route = "ForgotPassword"
        ) {
            ForgotPasswordScreen(navController)
        }
        composable(route = "ResetPassword") {
            ResetPasswordScreen(navController)
        }
        composable(
            route = "SignupScreen"
        ) {
            SignupScreen(navController)
        }
        composable(
            route = "SmsLoginScreen"
        ) {
            SmsLoginScreen(navController)
        }
        composable(
            route = "HomeScreen"
        ) {
            HomeScreen(navController)
        }
        composable(
            route = "ParkingDetailScreen/{parkingId}",
            arguments = listOf(navArgument("parkingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val parkingId = backStackEntry.arguments?.getString("parkingId") ?: ""
            ParkingDetailScreen(navController, parkingId)
        }
        composable(
            route = "MovementDetailScreen/{movementId}",
            arguments = listOf(navArgument("movementId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movementId = backStackEntry.arguments?.getString("movementId") ?: ""
            MovementDetailScreen(navController, movementId)
        }
    }
}