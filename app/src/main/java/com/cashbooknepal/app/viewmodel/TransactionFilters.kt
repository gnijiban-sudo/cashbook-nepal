package com.cashbooknepal.app.viewmodel

import java.util.Calendar

enum class TransactionTypeFilter {
    ALL, CASH_IN, CASH_OUT
}

enum class DateFilterOption {
    ALL, TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH, LAST_MONTH, THIS_YEAR, CUSTOM
}

data class TransactionFilters(
    val searchQuery: String = "",
    val typeFilter: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val categoryFilter: String? = null,
    val paymentMethodFilter: String? = null,
    val dateFilter: DateFilterOption = DateFilterOption.ALL,
    val customRangeStart: Long? = null,
    val customRangeEnd: Long? = null
) {
    val isActive: Boolean
        get() = searchQuery.isNotBlank() ||
            typeFilter != TransactionTypeFilter.ALL ||
            categoryFilter != null ||
            paymentMethodFilter != null ||
            dateFilter != DateFilterOption.ALL
}

/**
 * Resolves a [DateFilterOption] into a concrete [start, end] epoch-millis range.
 * Ranges are computed against the device's actual (AD) calendar since dateMillis
 * is always stored as real AD epoch millis regardless of the BS/AD display setting.
 */
object DateRangeCalculator {

    fun resolve(filters: TransactionFilters): Pair<Long, Long>? {
        val cal = Calendar.getInstance()

        fun startOfDay(c: Calendar): Long {
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            return c.timeInMillis
        }

        fun endOfDay(c: Calendar): Long {
            c.set(Calendar.HOUR_OF_DAY, 23)
            c.set(Calendar.MINUTE, 59)
            c.set(Calendar.SECOND, 59)
            c.set(Calendar.MILLISECOND, 999)
            return c.timeInMillis
        }

        return when (filters.dateFilter) {
            DateFilterOption.ALL -> null

            DateFilterOption.TODAY -> {
                val start = startOfDay(cal.clone() as Calendar)
                val end = endOfDay(cal.clone() as Calendar)
                start to end
            }

            DateFilterOption.YESTERDAY -> {
                val yesterday = cal.clone() as Calendar
                yesterday.add(Calendar.DAY_OF_YEAR, -1)
                startOfDay(yesterday.clone() as Calendar) to endOfDay(yesterday)
            }

            DateFilterOption.THIS_WEEK -> {
                val weekStart = cal.clone() as Calendar
                weekStart.set(Calendar.DAY_OF_WEEK, weekStart.firstDayOfWeek)
                val weekEnd = weekStart.clone() as Calendar
                weekEnd.add(Calendar.DAY_OF_YEAR, 6)
                startOfDay(weekStart.clone() as Calendar) to endOfDay(weekEnd)
            }

            DateFilterOption.THIS_MONTH -> {
                val monthStart = cal.clone() as Calendar
                monthStart.set(Calendar.DAY_OF_MONTH, 1)
                val monthEnd = monthStart.clone() as Calendar
                monthEnd.set(Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH))
                startOfDay(monthStart.clone() as Calendar) to endOfDay(monthEnd)
            }

            DateFilterOption.LAST_MONTH -> {
                val monthStart = cal.clone() as Calendar
                monthStart.add(Calendar.MONTH, -1)
                monthStart.set(Calendar.DAY_OF_MONTH, 1)
                val monthEnd = monthStart.clone() as Calendar
                monthEnd.set(Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH))
                startOfDay(monthStart.clone() as Calendar) to endOfDay(monthEnd)
            }

            DateFilterOption.THIS_YEAR -> {
                val yearStart = cal.clone() as Calendar
                yearStart.set(Calendar.DAY_OF_YEAR, 1)
                val yearEnd = yearStart.clone() as Calendar
                yearEnd.set(Calendar.DAY_OF_YEAR, yearEnd.getActualMaximum(Calendar.DAY_OF_YEAR))
                startOfDay(yearStart.clone() as Calendar) to endOfDay(yearEnd)
            }

            DateFilterOption.CUSTOM -> {
                val start = filters.customRangeStart
                val end = filters.customRangeEnd
                if (start == null || end == null) null
                else {
                    val startCal = Calendar.getInstance().apply { timeInMillis = start }
                    val endCal = Calendar.getInstance().apply { timeInMillis = end }
                    startOfDay(startCal) to endOfDay(endCal)
                }
            }
        }
    }
}
