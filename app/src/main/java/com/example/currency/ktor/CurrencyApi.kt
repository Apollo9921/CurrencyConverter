package com.example.currency.ktor

import com.example.currency.BuildConfig
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

private const val apiUrl = BuildConfig.API_URL

class CurrencyApi {
    suspend fun getCurrencies(): HttpResponse =
            KtorClient.httpClient.get {
                url("${apiUrl}currencies")
                contentType(ContentType.Application.Json)
            }

    suspend fun getLatestRates(from: String): HttpResponse =
            KtorClient.httpClient.get {
                url("${apiUrl}latest?from=$from")
                contentType(ContentType.Application.Json)
            }

    suspend fun makeConversion(from: String, to: String, amount: String): HttpResponse =
            KtorClient.httpClient.get {
                url("${apiUrl}latest?from=$from&to=$to&amount=$amount")
                contentType(ContentType.Application.Json)
            }

}