package com.cashbooknepal.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cashbooknepal.app.data.local.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {

    @Insert
    suspend fun insertPaymentMethod(paymentMethod: PaymentMethodEntity)

    @Query("SELECT * FROM payment_methods ORDER BY name ASC")
    fun getAllPaymentMethods(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT COUNT(*) FROM payment_methods")
    suspend fun getPaymentMethodCount(): Int
}