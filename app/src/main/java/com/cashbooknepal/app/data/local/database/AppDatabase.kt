package com.cashbooknepal.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cashbooknepal.app.data.local.dao.BookDao
import com.cashbooknepal.app.data.local.dao.BusinessDao
import com.cashbooknepal.app.data.local.dao.CategoryDao
import com.cashbooknepal.app.data.local.dao.ContactDao
import com.cashbooknepal.app.data.local.dao.PaymentMethodDao
import com.cashbooknepal.app.data.local.dao.TransactionDao
import com.cashbooknepal.app.data.local.entity.BookEntity
import com.cashbooknepal.app.data.local.entity.BusinessEntity
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
        BookEntity::class,
        BusinessEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun contactDao(): ContactDao
    abstract fun bookDao(): BookDao
    abstract fun businessDao(): BusinessDao

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

        /**
         * Adds the businesses table and links every existing book to a default
         * migrated business ("My Business") so pre-existing cashbooks/transactions
         * remain intact. Books table is recreated because Room's FK on businessId
         * must match the table's actual SQL definition.
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS businesses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        phone TEXT NOT NULL DEFAULT '',
                        address TEXT NOT NULL DEFAULT '',
                        panVat TEXT NOT NULL DEFAULT '',
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    "INSERT INTO businesses (id, name, phone, address, panVat, notes, createdAt) " +
                        "VALUES (1, 'My Business', '', '', '', '', ${System.currentTimeMillis()})"
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS books_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        businessId INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(businessId) REFERENCES businesses(id) ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    "INSERT INTO books_new (id, name, businessId, createdAt) " +
                        "SELECT id, name, 1, createdAt FROM books"
                )

                db.execSQL("DROP TABLE books")
                db.execSQL("ALTER TABLE books_new RENAME TO books")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_books_businessId ON books(businessId)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cashbook_nepal_database"
                )
                    .addMigrations(MIGRATION_6_7)
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance

                CoroutineScope(Dispatchers.IO).launch {
                    seedDefaultCategoriesIfNeeded(instance)
                    seedDefaultPaymentMethodsIfNeeded(instance)
                    val businessId = seedDefaultBusinessIfNeeded(instance)
                    seedDefaultBookIfNeeded(instance, businessId)
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

        private suspend fun seedDefaultBusinessIfNeeded(database: AppDatabase): Long {
            val dao = database.businessDao()
            if (dao.getBusinessCount() == 0) {
                return dao.insertBusiness(BusinessEntity(name = "My Business"))
            }
            return dao.getFirstBusinessId() ?: 1L
        }

        private suspend fun seedDefaultBookIfNeeded(database: AppDatabase, businessId: Long) {
            val dao = database.bookDao()
            if (dao.getBookCount() == 0) {
                dao.insertBook(BookEntity(name = "Main Cash Book", businessId = businessId))
            }
        }
    }
}
