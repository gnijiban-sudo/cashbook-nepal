package com.cashbooknepal.app.data.repository

import com.cashbooknepal.app.data.local.dao.TransactionDao
import com.cashbooknepal.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    fun getTransactionsByBook(bookId: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByBook(bookId)
    }

    fun getTotalCashInByBook(bookId: Long): Flow<Double> {
        return transactionDao.getTotalCashInByBook(bookId)
    }

    fun getTotalCashOutByBook(bookId: Long): Flow<Double> {
        return transactionDao.getTotalCashOutByBook(bookId)
    }

    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun getTransactionById(id: Long): TransactionEntity? {
        return transactionDao.getTransactionById(id)
    }

    suspend fun getTransactionCountByBusiness(businessId: Long): Int {
        return transactionDao.getTransactionCountByBusiness(businessId)
    }
}