package com.example.currency.model.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HistoryDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addHistory(history: History)

    @Query("SELECT * FROM history_table ORDER BY id DESC")
    fun readAllData(): List<History>

    @Query("DELETE FROM history_table")
    suspend fun deleteAllHistory()
}