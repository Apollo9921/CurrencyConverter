package com.example.currency.model.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_table")
data class History(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val date: String,
    val from: String,
    val fromAmount: String,
    val to: String,
    val toAmount: String
)
