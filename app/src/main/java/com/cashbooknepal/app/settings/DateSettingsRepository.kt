package com.cashbooknepal.app.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dateSettingsDataStore by preferencesDataStore(name = "date_settings")

class DateSettingsRepository(private val context: Context) {

    companion object {
        private val USE_BS_CALENDAR = booleanPreferencesKey("use_bs_calendar")
    }

    // true = show/use BS (Nepali) dates, false = show/use AD (English) dates
    val useBsCalendar: Flow<Boolean> = context.dateSettingsDataStore.data
        .map { preferences -> preferences[USE_BS_CALENDAR] ?: true } // default to BS

    suspend fun setUseBsCalendar(useBs: Boolean) {
        context.dateSettingsDataStore.edit { preferences ->
            preferences[USE_BS_CALENDAR] = useBs
        }
    }
}