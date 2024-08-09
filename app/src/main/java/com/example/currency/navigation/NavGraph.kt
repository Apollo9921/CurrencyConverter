package com.example.currency.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.currency.ui.CurrenciesScreen

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
        composable<Destination.E2> {
            // CurrencyConverterScreen(navController)
        }
    }
}