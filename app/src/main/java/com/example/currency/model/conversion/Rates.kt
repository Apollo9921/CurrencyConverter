package com.example.currency.model.conversion

import kotlinx.serialization.Serializable

@Serializable
data class Rates(val conversion: Map<String, Double>)