package com.cashbooknepal.app.utils.date

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormatter {

    /**
     * Formats a stored epoch-millis date according to the user's chosen calendar system.
     * @param dateMillis the date stored in the database (always epoch millis, AD-based)
     * @param useBs true = show as Nepali (BS) date, false = show as English (AD) date
     */
    fun format(dateMillis: Long, useBs: Boolean): String {
        return if (useBs) {
            try {
                val cal = Calendar.getInstance()
                cal.timeInMillis = dateMillis
                val year = cal.get(Calendar.YEAR)
                val month = cal.get(Calendar.MONTH) + 1 // Calendar months are 0-based
                val day = cal.get(Calendar.DAY_OF_MONTH)

                val nepaliDate = NepaliDateConverter.convertEnglishToNepali(year, month, day)
                NepaliDateConverter.formatNepaliDate(
                    nepaliDate,
                    NepaliDateLocale(language = NepaliDatePickerLang.ENGLISH)
                )
            } catch (e: Exception) {
                formatAd(dateMillis)
            }
        } else {
            formatAd(dateMillis)
        }
    }

    private fun formatAd(dateMillis: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
        return sdf.format(Date(dateMillis))
    }
}