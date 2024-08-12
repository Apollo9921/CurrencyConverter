package com.example.currency.model.history



class HistoryRepository(private val historyDAO: HistoryDAO) {

    fun readAllData(): List<History> {
        return historyDAO.readAllData()
    }


    suspend fun addHistory(history: History) {
        historyDAO.addHistory(history)
    }

    suspend fun deleteAllHistory() {
        historyDAO.deleteAllHistory()
    }
}