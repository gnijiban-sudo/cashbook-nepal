@file:OptIn(ExperimentalMaterial3Api::class)

package com.cashbooknepal.app.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cashbooknepal.app.settings.DateSettingsRepository
import com.cashbooknepal.app.ui.common.AdDatePickerDialog
import com.cashbooknepal.app.ui.common.AppTimePickerDialog
import com.cashbooknepal.app.ui.common.BsDatePickerDialog
import com.cashbooknepal.app.utils.calculator.ExpressionEvaluator
import com.cashbooknepal.app.utils.date.DateFormatter
import com.cashbooknepal.app.viewmodel.TransactionViewModel
import java.util.Calendar
import java.util.Locale

private val CashInGreen = Color(0xFF2E7D32)
private val CashOutRed = Color(0xFFC62828)

private fun combineDateAndTime(dateMillis: Long, hour: Int, minute: Int): Long {
    val dateCal = Calendar.getInstance().apply { timeInMillis = dateMillis }
    val result = Calendar.getInstance()
    result.set(
        dateCal.get(Calendar.YEAR),
        dateCal.get(Calendar.MONTH),
        dateCal.get(Calendar.DAY_OF_MONTH),
        hour,
        minute,
        0
    )
    result.set(Calendar.MILLISECOND, 0)
    return result.timeInMillis
}

private val CalculatorOperators = charArrayOf('+', '-', '×', '÷')

private fun calculatorAppendDigit(expression: String, digit: Char): String {
    val currentSegment = expression.substringAfterLast(' ')
    if (digit == '.' && currentSegment.contains('.')) return expression
    return expression + digit
}

private fun calculatorAppendOperator(expression: String, operator: Char): String {
    if (expression.isBlank()) return expression
    return if (expression.endsWith(' ')) {
        expression.dropLast(3) + " $operator "
    } else {
        "$expression $operator "
    }
}

private fun calculatorBackspace(expression: String): String {
    if (expression.isEmpty()) return expression
    return if (expression.endsWith(' ')) {
        expression.dropLast(3)
    } else {
        expression.dropLast(1)
    }
}

private fun formatCalculatorAmount(value: Double): String =
    String.format(Locale.US, "%.2f", value)

@Composable
fun AddTransactionScreen(
    transactionType: String,
    editTransactionId: Long? = null,
    viewModel: TransactionViewModel = viewModel(),
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val dateSettingsRepository = remember { DateSettingsRepository(context) }
    val useBsCalendar by dateSettingsRepository.useBsCalendar.collectAsState(initial = true)

    var selectedType by remember { mutableStateOf(transactionType) }

    val categories by remember(selectedType) { viewModel.getCategoriesByType(selectedType) }
        .collectAsState(initial = emptyList())
    val paymentMethods by viewModel.allPaymentMethods.collectAsState()
    val contacts by viewModel.allContacts.collectAsState()

    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var contactName by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(editTransactionId != null) }
    val nowCalendar = remember { Calendar.getInstance() }
    var selectedDateMillis by remember { mutableStateOf(nowCalendar.timeInMillis) }
    var selectedHour by remember { mutableStateOf(nowCalendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(nowCalendar.get(Calendar.MINUTE)) }
    var showBsDatePicker by remember { mutableStateOf(false) }
    var showAdDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showPaymentDropdown by remember { mutableStateOf(false) }
    var showContactDropdown by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newPaymentName by remember { mutableStateOf("") }
    var newContactName by remember { mutableStateOf("") }
    var showCalculatorSheet by remember { mutableStateOf(false) }
    var calculatorExpression by remember { mutableStateOf("") }
    var calculatorError by remember { mutableStateOf<String?>(null) }

    val isCashIn = selectedType == "CASH_IN"
    val isEditMode = editTransactionId != null
    val screenTitle = when {
        isEditMode && isCashIn -> "Edit Cash In"
        isEditMode -> "Edit Cash Out"
        isCashIn -> "Add Cash In"
        else -> "Add Cash Out"
    }
    val accentColor = if (isCashIn) CashInGreen else CashOutRed

    LaunchedEffect(editTransactionId) {
        if (editTransactionId != null) {
            val existing = viewModel.getTransactionById(editTransactionId)
            if (existing != null) {
                selectedType = existing.type
                amount = existing.amount.toString()
                description = existing.description
                category = existing.category
                paymentMethod = existing.paymentMethod
                contactName = existing.contactName
                selectedDateMillis = existing.dateMillis
                val existingCal = Calendar.getInstance().apply { timeInMillis = existing.dateMillis }
                selectedHour = existingCal.get(Calendar.HOUR_OF_DAY)
                selectedMinute = existingCal.get(Calendar.MINUTE)
            }
            isLoading = false
        }
    }

    val fieldShape = RoundedCornerShape(16.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Color.Transparent,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val cashInSelectedColor = CashInGreen.copy(alpha = 0.16f)
                    val cashOutSelectedColor = CashOutRed.copy(alpha = 0.16f)

                    Surface(
                        onClick = { selectedType = "CASH_IN" },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCashIn) cashInSelectedColor else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cash In",
                                color = if (isCashIn) CashInGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isCashIn) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                    Surface(
                        onClick = { selectedType = "CASH_OUT" },
                        shape = RoundedCornerShape(12.dp),
                        color = if (!isCashIn) cashOutSelectedColor else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cash Out",
                                color = if (!isCashIn) CashOutRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (!isCashIn) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val amountFieldColors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    disabledBorderColor = Color.Transparent,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = accentColor,
                    disabledSupportingTextColor = MaterialTheme.colorScheme.error
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            calculatorExpression = amount
                            calculatorError = null
                            showCalculatorSheet = true
                        }
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Amount") },
                        placeholder = { Text("Tap to calculate") },
                        leadingIcon = { Text("Rs.", fontWeight = FontWeight.Bold) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Calculate,
                                contentDescription = "Open calculator",
                                tint = accentColor
                            )
                        },
                        textStyle = MaterialTheme.typography.headlineSmall,
                        isError = amountError != null,
                        supportingText = { if (amountError != null) Text(amountError!!) },
                        shape = fieldShape,
                        colors = amountFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                ExposedDropdownMenuBox(
                    expanded = showContactDropdown,
                    onExpandedChange = { showContactDropdown = it }
                ) {
                    OutlinedTextField(
                        value = contactName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Contact Name (optional)") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showContactDropdown)
                        },
                        shape = fieldShape,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = showContactDropdown,
                        onDismissRequest = { showContactDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("(None)") },
                            onClick = {
                                contactName = ""
                                showContactDropdown = false
                            }
                        )
                        contacts.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = {
                                    contactName = c.name
                                    showContactDropdown = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("+ Add New Contact") },
                            onClick = {
                                showContactDropdown = false
                                showAddContactDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    shape = fieldShape,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown)
                        },
                        shape = fieldShape,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    category = cat.name
                                    showCategoryDropdown = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("+ Add New Category") },
                            onClick = {
                                showCategoryDropdown = false
                                showAddCategoryDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = showPaymentDropdown,
                    onExpandedChange = { showPaymentDropdown = it }
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPaymentDropdown)
                        },
                        shape = fieldShape,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = showPaymentDropdown,
                        onDismissRequest = { showPaymentDropdown = false }
                    ) {
                        paymentMethods.forEach { pm ->
                            DropdownMenuItem(
                                text = { Text(pm.name) },
                                onClick = {
                                    paymentMethod = pm.name
                                    showPaymentDropdown = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("+ Add New Payment Method") },
                            onClick = {
                                showPaymentDropdown = false
                                showAddPaymentDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        onClick = {
                            if (useBsCalendar) showBsDatePicker = true else showAdDatePicker = true
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = "Choose date",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (useBsCalendar) "BS" else "AD",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = DateFormatter.format(selectedDateMillis, useBsCalendar),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Card(
                        onClick = { showTimePicker = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = "Choose time",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = DateFormatter.formatTime(selectedDateMillis),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCashIn) CashInGreen else CashOutRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    onClick = {
                        val amountValue = ExpressionEvaluator.evaluate(amount)
                        if (amountValue == null || amountValue <= 0.0) {
                            amountError = "Enter a valid amount greater than zero"
                            return@Button
                        }
                        if (isEditMode) {
                            viewModel.updateTransactionFull(
                                id = editTransactionId!!,
                                type = selectedType,
                                amount = amountValue,
                                description = description,
                                category = category.ifBlank { "Other" },
                                paymentMethod = paymentMethod,
                                contactName = contactName,
                                dateMillis = selectedDateMillis
                            )
                        } else {
                            viewModel.addTransaction(
                                type = selectedType,
                                amount = amountValue,
                                description = description,
                                category = category.ifBlank { "Other" },
                                paymentMethod = paymentMethod,
                                contactName = contactName,
                                dateMillis = selectedDateMillis
                            )
                        }
                        onDone()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        text = if (isEditMode) "Update" else "Save",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showCalculatorSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var lastValidPreview by remember(showCalculatorSheet) {
            mutableStateOf(ExpressionEvaluator.evaluate(calculatorExpression))
        }

        LaunchedEffect(calculatorExpression) {
            val result = ExpressionEvaluator.evaluate(calculatorExpression)
            if (result != null) {
                lastValidPreview = result
                calculatorError = null
            }
        }

        fun closeSheet() {
            showCalculatorSheet = false
        }

        fun onKeyPress(key: String) {
            calculatorError = null
            when (key) {
                "⌫" -> calculatorExpression = calculatorBackspace(calculatorExpression)
                "+", "-", "×", "÷" -> calculatorExpression =
                    calculatorAppendOperator(calculatorExpression, key[0])
                else -> calculatorExpression = calculatorAppendDigit(calculatorExpression, key[0])
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showCalculatorSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Amount",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Rs. ${calculatorExpression.ifBlank { "0" }}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        calculatorError != null -> calculatorError!!
                        lastValidPreview != null -> "= Rs. ${formatCalculatorAmount(lastValidPreview!!)}"
                        else -> " "
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (calculatorError != null) MaterialTheme.colorScheme.error else accentColor
                )

                Spacer(modifier = Modifier.height(20.dp))

                val keypadRows = listOf(
                    listOf("7", "8", "9", "÷"),
                    listOf("4", "5", "6", "×"),
                    listOf("1", "2", "3", "-"),
                    listOf(".", "0", "⌫", "+")
                )

                keypadRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { key ->
                            val isOperator = key.length == 1 && key[0] in CalculatorOperators
                            val isBackspace = key == "⌫"
                            val containerColor = when {
                                isOperator -> accentColor.copy(alpha = 0.14f)
                                isBackspace -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surfaceContainerHigh
                            }
                            val contentColor = when {
                                isOperator -> accentColor
                                isBackspace -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                            Surface(
                                onClick = { onKeyPress(key) },
                                shape = RoundedCornerShape(14.dp),
                                color = containerColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = contentColor
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            calculatorExpression = ""
                            calculatorError = null
                            lastValidPreview = null
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Text("Clear", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val finalValue = ExpressionEvaluator.evaluate(calculatorExpression)
                            when {
                                finalValue == null -> calculatorError = "Enter a valid amount"
                                finalValue <= 0.0 -> calculatorError = "Amount must be greater than zero"
                                else -> {
                                    amount = formatCalculatorAmount(finalValue)
                                    amountError = null
                                    closeSheet()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(2f)
                            .height(56.dp)
                    ) {
                        Text("= Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showBsDatePicker) {
        BsDatePickerDialog(
            initialDateMillis = selectedDateMillis,
            onDateSelected = {
                selectedDateMillis = combineDateAndTime(it, selectedHour, selectedMinute)
                showBsDatePicker = false
            },
            onDismiss = { showBsDatePicker = false }
        )
    }

    if (showAdDatePicker) {
        AdDatePickerDialog(
            initialDateMillis = selectedDateMillis,
            onDateSelected = {
                selectedDateMillis = combineDateAndTime(it, selectedHour, selectedMinute)
                showAdDatePicker = false
            },
            onDismiss = { showAdDatePicker = false }
        )
    }

    if (showTimePicker) {
        AppTimePickerDialog(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            onTimeSelected = { hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                selectedDateMillis = combineDateAndTime(selectedDateMillis, hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false; newCategoryName = "" },
            title = { Text("Add New Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCategoryName.isNotBlank()) {
                        viewModel.addCustomCategory(newCategoryName.trim(), selectedType)
                        category = newCategoryName.trim()
                    }
                    newCategoryName = ""
                    showAddCategoryDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    newCategoryName = ""
                    showAddCategoryDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showAddPaymentDialog = false; newPaymentName = "" },
            title = { Text("Add New Payment Method") },
            text = {
                OutlinedTextField(
                    value = newPaymentName,
                    onValueChange = { newPaymentName = it },
                    label = { Text("Payment method name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPaymentName.isNotBlank()) {
                        viewModel.addCustomPaymentMethod(newPaymentName.trim())
                        paymentMethod = newPaymentName.trim()
                    }
                    newPaymentName = ""
                    showAddPaymentDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    newPaymentName = ""
                    showAddPaymentDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddContactDialog) {
        AlertDialog(
            onDismissRequest = { showAddContactDialog = false; newContactName = "" },
            title = { Text("Add New Contact") },
            text = {
                OutlinedTextField(
                    value = newContactName,
                    onValueChange = { newContactName = it },
                    label = { Text("Contact name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newContactName.isNotBlank()) {
                        viewModel.addContact(newContactName.trim(), "Other")
                        contactName = newContactName.trim()
                    }
                    newContactName = ""
                    showAddContactDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    newContactName = ""
                    showAddContactDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
