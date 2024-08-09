package com.example.currency.model.rates

import kotlinx.serialization.Serializable

@Serializable
data class RateList(val base: String, val rate: Double)