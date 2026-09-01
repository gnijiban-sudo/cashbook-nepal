package com.cashbooknepal.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val type: String, // "CASH_IN" or "CASH_OUT" — which side this category belongs to
    val isDefault: Boolean = false // true for built-in categories, false for user-added ones
)