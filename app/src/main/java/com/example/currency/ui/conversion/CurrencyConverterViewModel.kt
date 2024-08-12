package com.example.currency.ui.conversion

import android.icu.text.DecimalFormat
import android.icu.util.Calendar
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currency.core.status
import com.example.currency.koin.CurrencyRepository
import com.example.currency.model.conversion.Conversion
import com.example.currency.model.conversion.Rates
import com.example.currency.model.currencyList.Currency
import com.example.currency.model.history.History
import com.example.currency.model.history.HistoryRepository
import com.example.currency.network.ConnectivityObserver
import io.ktor.client.call.body
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class CurrencyConverterViewModel(
    private val currencyRepository: CurrencyRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private var conversionJob: Job? = null

    private val _getCurrency =
        MutableStateFlow<CurrencyConverterState>(CurrencyConverterState.Loading)
    private val getCurrency: StateFlow<CurrencyConverterState> = _getCurrency

    private val _getCurrencyConverter =
        MutableStateFlow<CurrencyConverterEvent>(CurrencyConverterEvent.Loading)
    private val getCurrencyConverter: StateFlow<CurrencyConverterEvent> = _getCurrencyConverter

    var isLoading = mutableStateOf(false)
    var isError = mutableStateOf(false)
    var messageError = mutableStateOf("")
    var isSuccess = mutableStateOf(false)
    var currencies = mutableStateOf<Currency?>(null)
    var conversion = mutableStateOf<Conversion?>(null)

    sealed class CurrencyConverterState {
        data object Loading : CurrencyConverterState()
        data class Success(val currencies: Currency?) : CurrencyConverterState()
        data class Error(val message: String) : CurrencyConverterState()
    }

    sealed class CurrencyConverterEvent {
        data object Loading : CurrencyConverterEvent()
        data class Success(val currencies: Conversion) : CurrencyConverterEvent()
        data class Error(val message: String) : CurrencyConverterEvent()
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
                        val newCurrencies =
                            currencies.value?.currencies?.toMutableMap() ?: mutableMapOf()
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

    fun makeConversion(
        status: ConnectivityObserver.Status,
        from: String,
        to: String,
        amount: String
    ) {
        viewModelScope.launch {
            try {
                if (status == ConnectivityObserver.Status.Unavailable) {
                    isError.value = true
                    _getCurrency.value = CurrencyConverterState.Error("No internet connection")
                    return@launch
                }
                _getCurrencyConverter.value = CurrencyConverterEvent.Loading
                val response = currencyRepository.makeConversion(from, to, amount)
                val conversionList = response.body<String>()
                val jsonElement = Json.parseToJsonElement(conversionList)
                if (jsonElement is JsonObject) {
                    val newConversion =
                        conversion.value?.rates?.conversion?.toMutableMap() ?: mutableMapOf()
                    jsonElement.forEach { (key, value) ->
                        if (key == "rates") {
                            for (rate in value.jsonObject) {
                                newConversion[rate.key] =
                                    rate.value.jsonPrimitive.toString().toDouble()
                            }
                        }
                    }
                    val ratesResponse = Conversion(
                        amount = jsonElement["amount"]?.jsonPrimitive?.content?.toDoubleOrNull()
                            ?: 1.0,
                        base = from,
                        date = jsonElement["date"]?.jsonPrimitive?.content ?: "",
                        rates = Rates(newConversion)
                    )
                    _getCurrencyConverter.value = CurrencyConverterEvent.Success(ratesResponse)
                } else {
                    _getCurrencyConverter.value =
                        CurrencyConverterEvent.Error(response.status.description)
                }
            } catch (e: Exception) {
                _getCurrencyConverter.value =
                    CurrencyConverterEvent.Error(e.message ?: "Unknown error")
            }
        }
        getConversionResponse()
    }

    private fun getConversionResponse() {
        conversionJob?.cancel()
        conversionJob = viewModelScope.launch {
            getCurrencyConverter.collect {
                when (it) {
                    is CurrencyConverterEvent.Error -> {
                        messageError.value = it.message
                        isLoading.value = false
                        isError.value = true
                        isSuccess.value = false
                    }

                    CurrencyConverterEvent.Loading -> {
                        isLoading.value = true
                        isError.value = false
                        isSuccess.value = false
                    }

                    is CurrencyConverterEvent.Success -> {
                        conversion.value = it.currencies
                        addHistoryToRoom()
                        isLoading.value = false
                        isError.value = false
                        isSuccess.value = true
                    }
                }
            }
        }
    }

    private suspend fun addHistoryToRoom() {
        val fromAmount = DecimalFormat("#.##").format(conversion.value?.amount)
        val toAmount = DecimalFormat("#.##").format(conversion.value?.rates?.conversion?.values?.first())
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        viewModelScope.launch(Dispatchers.IO) {
            val formattedDate = String.format(
                "%02d/%02d/%04d - %02d:%02d:%02d",
                day,
                month,
                year,
                hour,
                minute,
                second
            )
            val history = History(
                id = 0,
                date = formattedDate,
                from = conversion.value?.base ?: "",
                fromAmount = fromAmount,
                to = conversion.value?.rates?.conversion?.keys?.first() ?: "",
                toAmount = toAmount
            )

            val date = historyRepository.readAllData()
            if (date.isNotEmpty()) {
                val filter = date.filter { it.date == history.date }
                if (filter.isEmpty()) {
                    historyRepository.addHistory(history)
                }
            } else {
                historyRepository.addHistory(history)
            }
        }
    }
}