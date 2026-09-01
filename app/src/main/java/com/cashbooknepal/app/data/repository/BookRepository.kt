package com.cashbooknepal.app.data.repository

import com.cashbooknepal.app.data.local.dao.BookDao
import com.cashbooknepal.app.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

class BookRepository(private val bookDao: BookDao) {

    val allBooks: Flow<List<BookEntity>> = bookDao.getAllBooks()

    suspend fun insertBook(book: BookEntity) {
        bookDao.insertBook(book)
    }

    suspend fun deleteBook(book: BookEntity) {
        bookDao.deleteBook(book)
    }
}