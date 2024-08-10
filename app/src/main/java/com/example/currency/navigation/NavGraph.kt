package com.example.currency.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.currency.ui.currency.CurrenciesScreen
import com.example.currency.ui.conversion.CurrencyConverterScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: Destination = Destination.E1
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Destination.E1> {
            CurrenciesScreen(navController)
        }
        composable<Destination.E2> { backStackEntry ->
            val e2 = backStackEntry.toRoute<Destination.E2>()
            CurrencyConverterScreen(
                from = e2.from,
                to = e2.to,
                amount = e2.amount,
                navController
            )
        }
    }
}