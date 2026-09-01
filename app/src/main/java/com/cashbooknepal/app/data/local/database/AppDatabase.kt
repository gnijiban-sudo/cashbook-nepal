package com.cashbooknepal.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cashbooknepal.app.data.local.dao.BookDao
import com.cashbooknepal.app.data.local.dao.CategoryDao
import com.cashbooknepal.app.data.local.dao.ContactDao
import com.cashbooknepal.app.data.local.dao.PaymentMethodDao
import com.cashbooknepal.app.data.local.dao.TransactionDao
import com.cashbooknepal.app.data.local.entity.BookEntity
import com.cashbooknepal.app.data.local.entity.CategoryEntity
import com.cashbooknepal.app.data.local.entity.ContactEntity
import com.cashbooknepal.app.data.local.entity.PaymentMethodEntity
import com.cashbooknepal.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        PaymentMethodEntity::class,
        ContactEntity::class,
        BookEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun contactDao(): ContactDao
    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val defaultCashInCategories = listOf(
            "Sales", "Payment Received", "Salary", "Loan Received", "Refund", "Other Income"
        )

        private val defaultCashOutCategories = listOf(
            "Material", "Labour", "Transport", "Fuel", "Food", "Salary",
            "Rent", "Electricity", "Maintenance", "Equipment", "Office Expense",
            "Travel", "Marketing", "Other"
        )

        private val defaultPaymentMethods = listOf(
            "Cash", "Bank Transfer", "Cheque", "eSewa", "Khalti",
            "Fonepay", "ConnectIPS", "Mobile Banking", "Debit Card", "Credit", "Other"
        )

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cashbook_nepal_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance

                CoroutineScope(Dispatchers.IO).launch {
                    seedDefaultCategoriesIfNeeded(instance)
                    seedDefaultPaymentMethodsIfNeeded(instance)
                    seedDefaultBookIfNeeded(instance)
                }

                instance
            }
        }

        private suspend fun seedDefaultCategoriesIfNeeded(database: AppDatabase) {
            val dao = database.categoryDao()
            if (dao.getCategoryCount() == 0) {
                defaultCashInCategories.forEach { name ->
                    dao.insertCategory(
                        CategoryEntity(name = name, type = "CASH_IN", isDefault = true)
                    )
                }
                defaultCashOutCategories.forEach { name ->
                    dao.insertCategory(
                        CategoryEntity(name = name, type = "CASH_OUT", isDefault = true)
                    )
                }
            }
        }

        private suspend fun seedDefaultPaymentMethodsIfNeeded(database: AppDatabase) {
            val dao = database.paymentMethodDao()
            if (dao.getPaymentMethodCount() == 0) {
                defaultPaymentMethods.forEach { name ->
                    dao.insertPaymentMethod(
                        PaymentMethodEntity(name = name, isDefault = true)
                    )
                }
            }
        }

        private suspend fun seedDefaultBookIfNeeded(database: AppDatabase) {
            val dao = database.bookDao()
            if (dao.getBookCount() == 0) {
                dao.insertBook(BookEntity(name = "Main Cash Book"))
            }
        }
    }
}