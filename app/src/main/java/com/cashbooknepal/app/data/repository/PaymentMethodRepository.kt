package com.cashbooknepal.app.data.repository

import com.cashbooknepal.app.data.local.dao.PaymentMethodDao
import com.cashbooknepal.app.data.local.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

class PaymentMethodRepository(private val paymentMethodDao: PaymentMethodDao) {

    val allPaymentMethods: Flow<List<PaymentMethodEntity>> = paymentMethodDao.getAllPaymentMethods()

    suspend fun insertPaymentMethod(paymentMethod: PaymentMethodEntity) {
        paymentMethodDao.insertPaymentMethod(paymentMethod)
    }
}