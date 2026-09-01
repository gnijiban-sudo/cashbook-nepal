package com.cashbooknepal.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cashbooknepal.app.ui.dashboard.DashboardScreen
import com.cashbooknepal.app.ui.settings.SettingsScreen
import com.cashbooknepal.app.ui.transaction.AddTransactionScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val ADD_CASH_IN = "add_cash_in"
    const val ADD_CASH_OUT = "add_cash_out"
    const val SETTINGS = "settings"
    const val EDIT_TRANSACTION = "edit_transaction/{transactionId}/{type}"

    fun editTransaction(transactionId: Long, type: String) = "edit_transaction/$transactionId/$type"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD
    ) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onAddCashIn = { navController.navigate(Routes.ADD_CASH_IN) },
                onAddCashOut = { navController.navigate(Routes.ADD_CASH_OUT) },
                onEditTransaction = { id, type ->
                    navController.navigate(Routes.editTransaction(id, type))
                },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.ADD_CASH_IN) {
            AddTransactionScreen(
                transactionType = "CASH_IN",
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(Routes.ADD_CASH_OUT) {
            AddTransactionScreen(
                transactionType = "CASH_OUT",
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.EDIT_TRANSACTION,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.LongType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
            val type = backStackEntry.arguments?.getString("type") ?: "CASH_IN"
            AddTransactionScreen(
                transactionType = type,
                editTransactionId = transactionId,
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}