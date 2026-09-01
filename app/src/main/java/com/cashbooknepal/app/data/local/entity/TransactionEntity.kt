package com.cashbooknepal.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val type: String,
    val amount: Double,
    val description: String,
    val category: String,
    val paymentMethod: String = "Cash",
    val contactName: String = "",
    val bookId: Long = 1L,
    val dateMillis: Long,

    val createdAt: Long = System.currentTimeMillis()
)