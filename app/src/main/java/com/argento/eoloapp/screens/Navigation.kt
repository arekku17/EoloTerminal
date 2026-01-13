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
            route = "SmsLoginScreen/{phoneNumber}",
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            SmsLoginScreen(navController, phoneNumber)
        }
        composable(
            route = "HomeScreen"
        ) {
            HomeScreen(navController)
        }
        composable(
            route = "RestorePinScreen"
        ) {
            RestorePinScreen(navController)
        }
        composable(
            route = "ParkingDetailScreen/{parkingId}",
            arguments = listOf(navArgument("parkingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val parkingId = backStackEntry.arguments?.getString("parkingId") ?: ""
            ParkingDetailScreen(navController, parkingId)
        }
        composable(
            route = "MovementDetailScreen/{parkingId}/{movementId}",
            arguments = listOf(
                navArgument("parkingId") { type = NavType.StringType },
                navArgument("movementId") { type = NavType.StringType }

            )
        ) { backStackEntry ->
            val movementId = backStackEntry.arguments?.getString("movementId") ?: ""
            val parkingId = backStackEntry.arguments?.getString("parkingId") ?: ""
            MovementDetailScreen(navController, movementId, parkingId)
        }
        composable(
            route = "PaymentMethodScreen/{idEstacionamiento}/{folio}/{amount}",
            arguments = listOf(
                navArgument("idEstacionamiento") { type = NavType.StringType },
                navArgument("folio") { type = NavType.StringType },
                navArgument("amount") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val folio = backStackEntry.arguments?.getString("folio") ?: ""
            val idEstacionamiento = backStackEntry.arguments?.getString("idEstacionamiento") ?: ""
            val amount = backStackEntry.arguments?.getString("amount") ?: "0.0"
            PaymentMethodScreen(navController, folio, amount, idEstacionamiento)
        }
    }
}