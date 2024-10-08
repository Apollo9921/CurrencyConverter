package com.example.currency.koin

import com.example.currency.ktor.CurrencyApi
import io.ktor.client.statement.HttpResponse

class CurrencyRepositoryImpl(
    private val currencyApi: CurrencyApi
): CurrencyRepository {
    override suspend fun getCurrencies(): HttpResponse {
        return currencyApi.getCurrencies()
    }

    override suspend fun getLatestRates(from: String): HttpResponse {
        return currencyApi.getLatestRates(from)
    }

    override suspend fun makeConversion(from: String, to: String, amount: String): HttpResponse {
        return currencyApi.makeConversion(from, to, amount)
    }

}