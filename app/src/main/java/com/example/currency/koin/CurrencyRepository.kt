package com.example.currency.koin

import io.ktor.client.statement.HttpResponse

interface CurrencyRepository {
    suspend fun getCurrencies(): HttpResponse
    suspend fun getLatestRates(from: String, to: String): HttpResponse
}