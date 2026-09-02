package com.cashbooknepal.app.data.repository

import com.cashbooknepal.app.data.local.dao.BookDao
import com.cashbooknepal.app.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

class BookRepository(private val bookDao: BookDao) {

    val allBooks: Flow<List<BookEntity>> = bookDao.getAllBooks()

    fun getBooksByBusiness(businessId: Long): Flow<List<BookEntity>> {
        return bookDao.getBooksByBusiness(businessId)
    }

    suspend fun insertBook(book: BookEntity) {
        bookDao.insertBook(book)
    }

    suspend fun deleteBook(book: BookEntity) {
        bookDao.deleteBook(book)
    }

    suspend fun getBookCountByBusiness(businessId: Long): Int {
        return bookDao.getBookCountByBusiness(businessId)
    }

    suspend fun deleteBooksByBusiness(businessId: Long) {
        bookDao.deleteBooksByBusiness(businessId)
    }
}