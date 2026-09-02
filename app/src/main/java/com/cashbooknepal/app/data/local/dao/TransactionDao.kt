package com.cashbooknepal.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cashbooknepal.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE bookId = :bookId ORDER BY dateMillis DESC")
    fun getTransactionsByBook(bookId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'CASH_IN' AND bookId = :bookId")
    fun getTotalCashInByBook(bookId: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'CASH_OUT' AND bookId = :bookId")
    fun getTotalCashOutByBook(bookId: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM transactions WHERE bookId IN (SELECT id FROM books WHERE businessId = :businessId)")
    suspend fun getTransactionCountByBusiness(businessId: Long): Int
}