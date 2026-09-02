@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.cashbooknepal.app.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerDialog
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDatePickerState
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import java.util.Calendar

@Composable
fun BsDatePickerDialog(
    initialDateMillis: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val initialNepaliDate = remember(initialDateMillis) {
        val calendar = Calendar.getInstance().apply { timeInMillis = initialDateMillis }
        val nepaliDate = NepaliDateConverter.convertEnglishToNepali(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        SimpleDate(nepaliDate.year, nepaliDate.month, nepaliDate.dayOfMonth)
    }

    val nepaliDatePickerState = rememberNepaliDatePickerState(
        initialSelectedDate = initialNepaliDate,
        initialDisplayedMonth = initialNepaliDate,
        locale = NepaliDateLocale(language = NepaliDatePickerLang.ENGLISH)
    )

    NepaliDatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            NepaliDatePickerDefaults.DialogButton(
                text = "OK",
                onButtonClick = {
                    val picked = nepaliDatePickerState.selectedDate
                    if (picked != null) {
                        val englishDate = NepaliDateConverter.convertNepaliToEnglish(
                            picked.year, picked.month, picked.dayOfMonth
                        )
                        val calendar = Calendar.getInstance()
                        calendar.set(englishDate.year, englishDate.month - 1, englishDate.dayOfMonth, 12, 0, 0)
                        onDateSelected(calendar.timeInMillis)
                    } else {
                        onDismiss()
                    }
                }
            )
        },
        dismissButton = {
            NepaliDatePickerDefaults.DialogButton(text = "Cancel", onButtonClick = onDismiss)
        }
    ) {
        NepaliDatePicker(state = nepaliDatePickerState)
    }
}

@Composable
fun AdDatePickerDialog(
    initialDateMillis: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val picked = datePickerState.selectedDateMillis
                if (picked != null) onDateSelected(picked) else onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun AppTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(timePickerState.hour, timePickerState.minute)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}
