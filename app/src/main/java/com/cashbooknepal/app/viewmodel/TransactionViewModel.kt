@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.cashbooknepal.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cashbooknepal.app.data.local.database.AppDatabase
import com.cashbooknepal.app.data.local.entity.BookEntity
import com.cashbooknepal.app.data.local.entity.BusinessEntity
import com.cashbooknepal.app.data.local.entity.CategoryEntity
import com.cashbooknepal.app.data.local.entity.ContactEntity
import com.cashbooknepal.app.data.local.entity.PaymentMethodEntity
import com.cashbooknepal.app.data.local.entity.TransactionEntity
import com.cashbooknepal.app.data.repository.BookRepository
import com.cashbooknepal.app.data.repository.BusinessRepository
import com.cashbooknepal.app.data.repository.CategoryRepository
import com.cashbooknepal.app.data.repository.ContactRepository
import com.cashbooknepal.app.data.repository.PaymentMethodRepository
import com.cashbooknepal.app.data.repository.TransactionRepository
import com.cashbooknepal.app.settings.BusinessSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository
    private val categoryRepository: CategoryRepository
    private val paymentMethodRepository: PaymentMethodRepository
    private val contactRepository: ContactRepository
    private val bookRepository: BookRepository
    private val businessRepository: BusinessRepository
    private val businessSettingsRepository: BusinessSettingsRepository

    private val _currentBookId = MutableStateFlow(1L)
    val currentBookId: StateFlow<Long> = _currentBookId.asStateFlow()

    private val _currentBusinessId = MutableStateFlow(1L)
    val currentBusinessId: StateFlow<Long> = _currentBusinessId.asStateFlow()

    val allBooks: StateFlow<List<BookEntity>>
    val allBusinesses: StateFlow<List<BusinessEntity>>
    val booksForCurrentBusiness: StateFlow<List<BookEntity>>
    val allTransactions: StateFlow<List<TransactionEntity>>
    val totalCashIn: StateFlow<Double>
    val totalCashOut: StateFlow<Double>
    val allPaymentMethods: StateFlow<List<PaymentMethodEntity>>
    val allContacts: StateFlow<List<ContactEntity>>

    private val _filters = MutableStateFlow(TransactionFilters())
    val filters: StateFlow<TransactionFilters> = _filters.asStateFlow()

    val filteredTransactions: StateFlow<List<TransactionEntity>>
    val availableCategories: StateFlow<List<String>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TransactionRepository(db.transactionDao())
        categoryRepository = CategoryRepository(db.categoryDao())
        paymentMethodRepository = PaymentMethodRepository(db.paymentMethodDao())
        contactRepository = ContactRepository(db.contactDao())
        bookRepository = BookRepository(db.bookDao())
        businessRepository = BusinessRepository(db.businessDao())
        businessSettingsRepository = BusinessSettingsRepository(application)

        allBooks = bookRepository.allBooks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allBusinesses = businessRepository.allBusinesses.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        booksForCurrentBusiness = combine(allBooks, _currentBusinessId) { books, businessId ->
            books.filter { it.businessId == businessId }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            val stored = businessSettingsRepository.currentBusinessId.first()
            if (stored != null) {
                _currentBusinessId.value = stored
            }
        }

        viewModelScope.launch {
            allBusinesses.collect { businesses ->
                if (businesses.isNotEmpty() && businesses.none { it.id == _currentBusinessId.value }) {
                    _currentBusinessId.value = businesses.first().id
                }
            }
        }

        viewModelScope.launch {
            booksForCurrentBusiness.collect { books ->
                if (books.isNotEmpty() && books.none { it.id == _currentBookId.value }) {
                    _currentBookId.value = books.first().id
                }
            }
        }

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

        filteredTransactions = combine(allTransactions, _filters) { transactions, filters ->
            applyFilters(transactions, filters)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        availableCategories = allTransactions.map { transactions ->
            transactions.map { it.category }.distinct().sorted()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    private fun applyFilters(
        transactions: List<TransactionEntity>,
        filters: TransactionFilters
    ): List<TransactionEntity> {
        val query = filters.searchQuery.trim()
        val range = DateRangeCalculator.resolve(filters)

        return transactions.filter { tx ->
            val matchesType = filters.typeFilter == TransactionTypeFilter.ALL ||
                tx.type == filters.typeFilter.name
            val matchesCategory = filters.categoryFilter == null || tx.category == filters.categoryFilter
            val matchesPaymentMethod = filters.paymentMethodFilter == null ||
                tx.paymentMethod == filters.paymentMethodFilter
            val matchesDate = range == null || tx.dateMillis in range.first..range.second
            val matchesSearch = query.isBlank() ||
                tx.description.contains(query, ignoreCase = true) ||
                tx.contactName.contains(query, ignoreCase = true) ||
                tx.category.contains(query, ignoreCase = true) ||
                tx.amount.toString().contains(query) ||
                "%.2f".format(tx.amount).contains(query)

            matchesType && matchesCategory && matchesPaymentMethod && matchesDate && matchesSearch
        }
    }

    fun setSearchQuery(query: String) {
        _filters.value = _filters.value.copy(searchQuery = query)
    }

    fun setTypeFilter(type: TransactionTypeFilter) {
        _filters.value = _filters.value.copy(typeFilter = type)
    }

    fun setCategoryFilter(category: String?) {
        _filters.value = _filters.value.copy(categoryFilter = category)
    }

    fun setPaymentMethodFilter(paymentMethod: String?) {
        _filters.value = _filters.value.copy(paymentMethodFilter = paymentMethod)
    }

    fun setDateFilter(option: DateFilterOption, customStart: Long? = null, customEnd: Long? = null) {
        _filters.value = _filters.value.copy(
            dateFilter = option,
            customRangeStart = if (option == DateFilterOption.CUSTOM) customStart else null,
            customRangeEnd = if (option == DateFilterOption.CUSTOM) customEnd else null
        )
    }

    fun clearFilters() {
        _filters.value = TransactionFilters()
    }

    fun selectBook(bookId: Long) {
        _currentBookId.value = bookId
    }

    fun addBook(name: String) {
        viewModelScope.launch {
            bookRepository.insertBook(BookEntity(name = name, businessId = _currentBusinessId.value))
        }
    }

    fun selectBusiness(businessId: Long) {
        _currentBusinessId.value = businessId
        viewModelScope.launch {
            businessSettingsRepository.setCurrentBusinessId(businessId)
        }
    }

    fun addBusiness(
        name: String,
        phone: String = "",
        address: String = "",
        panVat: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val newId = businessRepository.insertBusiness(
                BusinessEntity(
                    name = name,
                    phone = phone,
                    address = address,
                    panVat = panVat,
                    notes = notes
                )
            )
            bookRepository.insertBook(BookEntity(name = "Main Cash Book", businessId = newId))
            selectBusiness(newId)
        }
    }

    fun updateBusiness(business: BusinessEntity) {
        viewModelScope.launch {
            businessRepository.updateBusiness(business)
        }
    }

    /**
     * Returns null on success, or a user-facing reason the deletion was blocked.
     * Refuses to delete the last remaining business or a business whose cashbooks
     * already contain transactions, so financial data is never silently destroyed.
     */
    suspend fun deleteBusiness(business: BusinessEntity): String? {
        if (allBusinesses.value.size <= 1) {
            return "You must have at least one business."
        }
        val transactionCount = repository.getTransactionCountByBusiness(business.id)
        if (transactionCount > 0) {
            return "Cannot delete: this business has $transactionCount transaction(s). Delete or move them first."
        }
        bookRepository.deleteBooksByBusiness(business.id)
        businessRepository.deleteBusiness(business)
        return null
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