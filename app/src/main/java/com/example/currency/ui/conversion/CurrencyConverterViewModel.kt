package com.example.currency.ui.conversion

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currency.core.status
import com.example.currency.koin.CurrencyRepository
import com.example.currency.model.currencyList.Currency
import com.example.currency.network.ConnectivityObserver
import io.ktor.client.call.body
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class CurrencyConverterViewModel(
    private val currencyRepository: CurrencyRepository
): ViewModel() {

    private val _getCurrency = MutableStateFlow<CurrencyConverterState>(CurrencyConverterState.Loading)
    private val getCurrency: StateFlow<CurrencyConverterState> = _getCurrency

    var isLoading = mutableStateOf(false)
    var isError = mutableStateOf(false)
    var messageError = mutableStateOf("")
    var isSuccess = mutableStateOf(false)
    var currencies = mutableStateOf<Currency?>(null)

    sealed class CurrencyConverterState {
        data object Loading : CurrencyConverterState()
        data class Success(val currencies: Currency?) : CurrencyConverterState()
        data class Error(val message: String) : CurrencyConverterState()
    }

    init {
        getCurrencies(status)
    }

    fun getCurrencies(status: ConnectivityObserver.Status) {
        viewModelScope.launch {
            try {
                if (status == ConnectivityObserver.Status.Unavailable) {
                    isError.value = true
                    _getCurrency.value = CurrencyConverterState.Error("No internet connection")
                    return@launch
                } else {
                    makeRequest()
                }
            } catch (e: Exception) {
                _getCurrency.value = CurrencyConverterState.Error(e.message ?: "Unknown error")
            }
        }
        getCurrenciesResponse()
    }

    private fun makeRequest() {
        viewModelScope.launch {
            try {
                _getCurrency.value = CurrencyConverterState.Loading
                val response = currencyRepository.getCurrencies()
                if (response.status.value in 200..299) {
                    val currenciesList = response.body<String>()
                    val jsonElement = Json.parseToJsonElement(currenciesList)
                    if (jsonElement is JsonObject) {
                        val newCurrencies = currencies.value?.currencies?.toMutableMap() ?: mutableMapOf()
                        jsonElement.forEach { (key, value) ->
                            newCurrencies[key] = value.jsonPrimitive.content
                        }
                        _getCurrency.value = CurrencyConverterState.Success(Currency(newCurrencies))
                    } else {
                        _getCurrency.value = CurrencyConverterState.Error("Unknown error")
                    }
                } else {
                    _getCurrency.value = CurrencyConverterState.Error(response.status.description)
                }
            } catch (e: Exception) {
                _getCurrency.value = CurrencyConverterState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun getCurrenciesResponse() {
        viewModelScope.launch {
            getCurrency.collect {
                when (it) {
                    is CurrencyConverterState.Error -> {
                        messageError.value = it.message
                        isLoading.value = false
                        isError.value = true
                        isSuccess.value = false
                    }
                    CurrencyConverterState.Loading -> {
                        isLoading.value = true
                        isError.value = false
                        isSuccess.value = false
                    }
                    is CurrencyConverterState.Success -> {
                        currencies.value = it.currencies
                        isLoading.value = false
                        isError.value = false
                        isSuccess.value = true
                    }
                }
            }
        }
    }
}