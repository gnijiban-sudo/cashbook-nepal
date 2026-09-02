package com.cashbooknepal.app.data.repository

import com.cashbooknepal.app.data.local.dao.BusinessDao
import com.cashbooknepal.app.data.local.entity.BusinessEntity
import kotlinx.coroutines.flow.Flow

class BusinessRepository(private val businessDao: BusinessDao) {

    val allBusinesses: Flow<List<BusinessEntity>> = businessDao.getAllBusinesses()

    suspend fun insertBusiness(business: BusinessEntity): Long {
        return businessDao.insertBusiness(business)
    }

    suspend fun updateBusiness(business: BusinessEntity) {
        businessDao.updateBusiness(business)
    }

    suspend fun deleteBusiness(business: BusinessEntity) {
        businessDao.deleteBusiness(business)
    }

    suspend fun getBusinessCount(): Int {
        return businessDao.getBusinessCount()
    }
}
