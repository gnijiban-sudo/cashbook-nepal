package com.cashbooknepal.app.data.repository

import com.cashbooknepal.app.data.local.dao.CategoryDao
import com.cashbooknepal.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>> {
        return categoryDao.getCategoriesByType(type)
    }

    suspend fun insertCategory(category: CategoryEntity) {
        categoryDao.insertCategory(category)
    }
}