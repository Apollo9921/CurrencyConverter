package com.example.currency.model.conversion

import kotlinx.serialization.Serializable

@Serializable
data class Conversion(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Rates
)