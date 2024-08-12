package com.example.currency.ui.history

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currency.model.history.History
import com.example.currency.model.history.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShowHistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _history = MutableStateFlow<HistoryState>(HistoryState.Success(emptyList()))
    private val history: StateFlow<HistoryState> = _history

    var historyData = mutableStateListOf<History>()
    var message = mutableStateOf("")
    var isSuccess = mutableStateOf(false)
    var isError = mutableStateOf(false)


    sealed class HistoryState {
        data class Success(val history: List<History>) : HistoryState()
        data class Error(val message: String) : HistoryState()
    }

    init {
        getHistory()
    }

    fun getHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = historyRepository.readAllData()
                if (data.isNotEmpty()) {
                    _history.value = HistoryState.Success(data)
                } else {
                    _history.value = HistoryState.Error("No history found")
                }
            } catch (e: Exception) {
                _history.value = HistoryState.Error(e.message ?: "Unknown error")
            }
            getHistoryResponse()
        }
    }

    private fun getHistoryResponse() {
        viewModelScope.launch(Dispatchers.IO) {
            history.collect {
                when (it) {
                    is HistoryState.Error -> {
                        message.value = it.message
                        historyData = mutableStateListOf()
                        isError.value = true
                        isSuccess.value = false
                    }

                    is HistoryState.Success -> {
                        historyData = it.history.toMutableStateList()
                        message.value = ""
                        isError.value = false
                        isSuccess.value = true
                    }
                }
            }
        }
    }

    fun deleteAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            historyRepository.deleteAllHistory()
            isSuccess.value = false
            isError.value = true
            _history.value = HistoryState.Error("No history found")
        }
    }
}