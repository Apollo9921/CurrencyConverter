package com.example.currency.model.currencyList

import kotlinx.serialization.Serializable

@Serializable
data class Currency(val currencies: Map<String, String>)