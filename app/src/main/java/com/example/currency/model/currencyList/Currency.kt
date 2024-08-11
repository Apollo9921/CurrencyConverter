package com.example.currency.model.currencyList

import kotlinx.serialization.Serializable

@Serializable
data class Currency(val currencies: MutableMap<String, String>)