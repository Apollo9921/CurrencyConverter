package com.example.currency.ui.currency

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currency.core.status
import com.example.currency.koin.CurrencyRepository
import com.example.currency.model.currencyList.Currency
import com.example.currency.model.rates.Rates
import com.example.currency.network.ConnectivityObserver
import io.ktor.client.call.body
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class CurrenciesScreenViewModel(
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val _getCurrencyRates = MutableStateFlow<CurrencyRatesState>(CurrencyRatesState.Loading)
    private val getCurrencyRates: StateFlow<CurrencyRatesState> = _getCurrencyRates

    var isLoading = mutableStateOf(false)
    var isError = mutableStateOf(false)
    var messageError = mutableStateOf("")
    var isSuccess = mutableStateOf(false)
    var rates = mutableStateOf<Rates?>(null)
    var currencies = mutableStateOf<Currency?>(null)

    sealed class CurrencyRatesState {
        data object Loading : CurrencyRatesState()
        data class Success(val currencies: Rates) : CurrencyRatesState()
        data class Error(val message: String) : CurrencyRatesState()
    }

    init {
        getAllCurrencies(status)
    }

    fun getAllCurrencies(status: ConnectivityObserver.Status, from: String = "EUR") {
        viewModelScope.launch {
            try {
                if (status == ConnectivityObserver.Status.Unavailable) {
                    isError.value = true
                    _getCurrencyRates.value = CurrencyRatesState.Error("No internet connection")
                    return@launch
                } else {
                    _getCurrencyRates.value = CurrencyRatesState.Loading
                    makeRequest(from)
                }
            } catch (e: Exception) {
                _getCurrencyRates.value = CurrencyRatesState.Error(e.message ?: "Unknown error")
            }
        }
        getAllCurrenciesResponse()
    }

    private fun makeRequest(from: String) {
        viewModelScope.launch {
            try {
                val response = currencyRepository.getLatestRates(from)
                if (response.status.value in 200..299) {
                    val currenciesList = response.body<String>()
                    val jsonElement = Json.parseToJsonElement(currenciesList)
                    if (jsonElement is JsonObject) {
                        val newCurrencies =
                            currencies.value?.currencies?.toMutableMap() ?: mutableMapOf()
                        jsonElement.forEach { (key, value) ->
                            if (key == "rates") {
                                for (rate in value.jsonObject) {
                                    newCurrencies[rate.key] =
                                        rate.value.jsonPrimitive.toString()
                                }
                            }
                        }
                        val sortedCurrencies = newCurrencies.toSortedMap()
                        currencies.value = Currency(sortedCurrencies)
                        val ratesResponse = Rates(
                            amount = jsonElement["amount"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 1.0,
                            base = from,
                            date = jsonElement["date"]?.jsonPrimitive?.content ?: "",
                            rates = currencies.value!!
                        )
                        _getCurrencyRates.value = CurrencyRatesState.Success(ratesResponse)
                    } else {
                        _getCurrencyRates.value = CurrencyRatesState.Error("Unknown error")
                    }
                } else {
                    _getCurrencyRates.value = CurrencyRatesState.Error(response.status.description)
                }
            } catch (e: Exception) {
                _getCurrencyRates.value = CurrencyRatesState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun getAllCurrenciesResponse() {
        viewModelScope.launch {
            getCurrencyRates.collect {
                when (it) {
                    is CurrencyRatesState.Error -> {
                        messageError.value = it.message
                        isLoading.value = false
                        isError.value = true
                        isSuccess.value = false
                    }

                    CurrencyRatesState.Loading -> {
                        isLoading.value = true
                        isError.value = false
                        isSuccess.value = false
                    }

                    is CurrencyRatesState.Success -> {
                        rates.value = it.currencies
                        isLoading.value = false
                        isError.value = false
                        isSuccess.value = true
                    }
                }
            }
        }
    }
}