@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.cashbooknepal.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cashbooknepal.app.data.local.database.AppDatabase
import com.cashbooknepal.app.data.local.entity.BookEntity
import com.cashbooknepal.app.data.local.entity.CategoryEntity
import com.cashbooknepal.app.data.local.entity.ContactEntity
import com.cashbooknepal.app.data.local.entity.PaymentMethodEntity
import com.cashbooknepal.app.data.local.entity.TransactionEntity
import com.cashbooknepal.app.data.repository.BookRepository
import com.cashbooknepal.app.data.repository.CategoryRepository
import com.cashbooknepal.app.data.repository.ContactRepository
import com.cashbooknepal.app.data.repository.PaymentMethodRepository
import com.cashbooknepal.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository
    private val categoryRepository: CategoryRepository
    private val paymentMethodRepository: PaymentMethodRepository
    private val contactRepository: ContactRepository
    private val bookRepository: BookRepository

    private val _currentBookId = MutableStateFlow(1L)
    val currentBookId: StateFlow<Long> = _currentBookId.asStateFlow()

    val allBooks: StateFlow<List<BookEntity>>
    val allTransactions: StateFlow<List<TransactionEntity>>
    val totalCashIn: StateFlow<Double>
    val totalCashOut: StateFlow<Double>
    val allPaymentMethods: StateFlow<List<PaymentMethodEntity>>
    val allContacts: StateFlow<List<ContactEntity>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TransactionRepository(db.transactionDao())
        categoryRepository = CategoryRepository(db.categoryDao())
        paymentMethodRepository = PaymentMethodRepository(db.paymentMethodDao())
        contactRepository = ContactRepository(db.contactDao())
        bookRepository = BookRepository(db.bookDao())

        allBooks = bookRepository.allBooks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allTransactions = _currentBookId.flatMapLatest { bookId ->
            repository.getTransactionsByBook(bookId)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        totalCashIn = _currentBookId.flatMapLatest { bookId ->
            repository.getTotalCashInByBook(bookId)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

        totalCashOut = _currentBookId.flatMapLatest { bookId ->
            repository.getTotalCashOutByBook(bookId)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

        allPaymentMethods = paymentMethodRepository.allPaymentMethods.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allContacts = contactRepository.allContacts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun selectBook(bookId: Long) {
        _currentBookId.value = bookId
    }

    fun addBook(name: String) {
        viewModelScope.launch {
            bookRepository.insertBook(BookEntity(name = name))
        }
    }

    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>> {
        return categoryRepository.getCategoriesByType(type)
    }

    fun addCustomCategory(name: String, type: String) {
        viewModelScope.launch {
            categoryRepository.insertCategory(
                CategoryEntity(name = name, type = type, isDefault = false)
            )
        }
    }

    fun addCustomPaymentMethod(name: String) {
        viewModelScope.launch {
            paymentMethodRepository.insertPaymentMethod(
                PaymentMethodEntity(name = name, isDefault = false)
            )
        }
    }

    fun addContact(name: String, contactType: String) {
        viewModelScope.launch {
            contactRepository.insertContact(
                ContactEntity(name = name, contactType = contactType)
            )
        }
    }

    fun addTransaction(
        type: String,
        amount: Double,
        description: String,
        category: String,
        paymentMethod: String,
        contactName: String,
        dateMillis: Long
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    type = type,
                    amount = amount,
                    description = description,
                    category = category,
                    paymentMethod = paymentMethod,
                    contactName = contactName,
                    bookId = _currentBookId.value,
                    dateMillis = dateMillis
                )
            )
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    suspend fun getTransactionById(id: Long): TransactionEntity? {
        return repository.getTransactionById(id)
    }

    fun updateTransactionFull(
        id: Long,
        type: String,
        amount: Double,
        description: String,
        category: String,
        paymentMethod: String,
        contactName: String,
        dateMillis: Long
    ) {
        viewModelScope.launch {
            repository.updateTransaction(
                TransactionEntity(
                    id = id,
                    type = type,
                    amount = amount,
                    description = description,
                    category = category,
                    paymentMethod = paymentMethod,
                    contactName = contactName,
                    bookId = _currentBookId.value,
                    dateMillis = dateMillis
                )
            )
        }
    }
}