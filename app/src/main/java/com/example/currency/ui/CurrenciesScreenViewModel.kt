package com.example.currency.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currency.koin.CurrencyRepository
import com.example.currency.model.rates.Rates
import com.example.currency.network.ConnectivityObserver
import io.ktor.client.call.body
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

    sealed class CurrencyRatesState {
        data object Loading : CurrencyRatesState()
        data class Success(val currencies: Rates) : CurrencyRatesState()
        data class Error(val message: String) : CurrencyRatesState()
    }

    fun getAllCurrencies(status: ConnectivityObserver.Status, from: String = "EUR") {
        viewModelScope.launch {
            try {
                if (status == ConnectivityObserver.Status.Unavailable) {
                    isError.value = true
                    _getCurrencyRates.value = CurrencyRatesState.Error("No internet connection")
                    return@launch
                }
                _getCurrencyRates.value = CurrencyRatesState.Loading
                val response = currencyRepository.getLatestRates(from)
                if (response.status.value in 200..299) {
                    _getCurrencyRates.value = CurrencyRatesState.Success(response.body())
                } else {
                    _getCurrencyRates.value = CurrencyRatesState.Error(response.status.description)
                }
            } catch (e: Exception) {
                _getCurrencyRates.value = CurrencyRatesState.Error(e.message ?: "Unknown error")
            }
            getAllCurrenciesResponse()
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