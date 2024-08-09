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

    suspend fun getLatestRates(from: String, to: String): HttpResponse =
            KtorClient.httpClient.get {
                url("https://api.frankfurter.app/latest?from=$from&to=$to")
                contentType(ContentType.Application.Json)
            }
}