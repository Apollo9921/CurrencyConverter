package com.example.currency.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Destination {
    @Serializable
    data object E1: Destination()
    @Serializable
    data object E2: Destination()
}