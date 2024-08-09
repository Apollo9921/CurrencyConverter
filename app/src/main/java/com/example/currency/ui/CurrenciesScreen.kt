package com.example.currency.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.currency.main.keepSplashOpened

@Composable
fun CurrenciesScreen(navController: NavHostController) {
    Text(text = "Currencies Screen")
    keepSplashOpened = false
}