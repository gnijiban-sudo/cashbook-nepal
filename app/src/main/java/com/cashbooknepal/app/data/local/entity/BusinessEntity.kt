package com.cashbooknepal.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "businesses")
data class BusinessEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val phone: String = "",
    val address: String = "",
    val panVat: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
