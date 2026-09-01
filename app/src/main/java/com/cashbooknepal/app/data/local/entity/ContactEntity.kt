package com.cashbooknepal.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val phone: String = "",
    val contactType: String = "Other", // Customer, Supplier, Employee, Contractor, Personal, Other
    val address: String = "",
    val notes: String = ""
)