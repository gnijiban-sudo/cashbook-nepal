package com.cashbooknepal.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.cashbooknepal.app.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Insert
    suspend fun insertBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("SELECT * FROM books ORDER BY createdAt ASC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBookCount(): Int
}