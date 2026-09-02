package com.cashbooknepal.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cashbooknepal.app.data.local.entity.BusinessEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {

    @Insert
    suspend fun insertBusiness(business: BusinessEntity): Long

    @Update
    suspend fun updateBusiness(business: BusinessEntity)

    @Delete
    suspend fun deleteBusiness(business: BusinessEntity)

    @Query("SELECT * FROM businesses ORDER BY createdAt ASC")
    fun getAllBusinesses(): Flow<List<BusinessEntity>>

    @Query("SELECT COUNT(*) FROM businesses")
    suspend fun getBusinessCount(): Int

    @Query("SELECT id FROM businesses ORDER BY createdAt ASC LIMIT 1")
    suspend fun getFirstBusinessId(): Long?
}
