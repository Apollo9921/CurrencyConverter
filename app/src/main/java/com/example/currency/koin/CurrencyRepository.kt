package com.example.currency.koin

import io.ktor.client.statement.HttpResponse

interface CurrencyRepository {
    suspend fun getCurrencies(): HttpResponse
    suspend fun getLatestRates(from: String): HttpResponse
    suspend fun makeConversion(from: String, to: String, amount: String): HttpResponse
}