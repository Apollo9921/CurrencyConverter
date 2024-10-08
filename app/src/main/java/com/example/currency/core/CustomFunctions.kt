package com.example.currency.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.currency.network.ConnectivityObserver

val small = 600.dp
val normal = 840.dp

lateinit var status: ConnectivityObserver.Status

@Composable
fun mediaQueryWidth(): Dp {
    return LocalContext.current.resources.displayMetrics.widthPixels.dp / LocalDensity.current.density
}

@Composable
fun mediaQueryHeight(): Dp {
    return LocalContext.current.resources.displayMetrics.heightPixels.dp / LocalDensity.current.density
}