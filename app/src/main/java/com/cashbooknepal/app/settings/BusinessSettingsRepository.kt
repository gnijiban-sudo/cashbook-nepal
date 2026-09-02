package com.cashbooknepal.app.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.businessSettingsDataStore by preferencesDataStore(name = "business_settings")

class BusinessSettingsRepository(private val context: Context) {

    companion object {
        private val CURRENT_BUSINESS_ID = longPreferencesKey("current_business_id")
    }

    val currentBusinessId: Flow<Long?> = context.businessSettingsDataStore.data
        .map { preferences -> preferences[CURRENT_BUSINESS_ID] }

    suspend fun setCurrentBusinessId(businessId: Long) {
        context.businessSettingsDataStore.edit { preferences ->
            preferences[CURRENT_BUSINESS_ID] = businessId
        }
    }
}
