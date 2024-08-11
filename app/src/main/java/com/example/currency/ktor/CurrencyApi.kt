package com.example.currency.ktor

import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class CurrencyApi {
    suspend fun getCurrencies(): HttpResponse =
            KtorClient.httpClient.get {
                url("https://api.frankfurter.app/currencies")
                contentType(ContentType.Application.Json)
            }

    suspend fun getLatestRates(from: String): HttpResponse =
            KtorClient.httpClient.get {
                url("https://api.frankfurter.app/latest?from=$from")
                contentType(ContentType.Application.Json)
            }

    suspend fun makeConversion(from: String, to: String, amount: String): HttpResponse =
            KtorClient.httpClient.get {
                url("https://api.frankfurter.app/latest?from=$from&to=$to&amount=$amount")
                contentType(ContentType.Application.Json)
            }

}