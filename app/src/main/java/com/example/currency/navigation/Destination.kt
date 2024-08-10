package com.example.currency.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Destination {
    @Serializable
    data object E1: Destination()
    @Serializable
    data class E2(
        val from: String,
        val to: String,
        val amount: String
    ): Destination()
}