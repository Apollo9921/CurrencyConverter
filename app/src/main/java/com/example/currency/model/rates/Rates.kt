package com.example.currency.model.rates

import kotlinx.serialization.Serializable

@Serializable
data class Rates(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: RatesX
)