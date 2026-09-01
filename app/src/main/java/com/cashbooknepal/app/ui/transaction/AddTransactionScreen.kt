@file:OptIn(ExperimentalMaterial3Api::class)

package com.cashbooknepal.app.ui.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cashbooknepal.app.settings.DateSettingsRepository
import com.cashbooknepal.app.ui.common.AdDatePickerDialog
import com.cashbooknepal.app.ui.common.BsDatePickerDialog
import com.cashbooknepal.app.utils.date.DateFormatter
import com.cashbooknepal.app.viewmodel.TransactionViewModel

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

    val categories by viewModel.getCategoriesByType(transactionType).collectAsState(initial = emptyList())
    val paymentMethods by viewModel.allPaymentMethods.collectAsState()
    val contacts by viewModel.allContacts.collectAsState()

    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var contactName by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(editTransactionId != null) }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showBsDatePicker by remember { mutableStateOf(false) }
    var showAdDatePicker by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showPaymentDropdown by remember { mutableStateOf(false) }
    var showContactDropdown by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newPaymentName by remember { mutableStateOf("") }
    var newContactName by remember { mutableStateOf("") }

    val isCashIn = transactionType == "CASH_IN"
    val isEditMode = editTransactionId != null
    val screenTitle = when {
        isEditMode && isCashIn -> "Edit Cash In"
        isEditMode -> "Edit Cash Out"
        isCashIn -> "Add Cash In"
        else -> "Add Cash Out"
    }

    LaunchedEffect(editTransactionId) {
        if (editTransactionId != null) {
            val existing = viewModel.getTransactionById(editTransactionId)
            if (existing != null) {
                amount = existing.amount.toString()
                description = existing.description
                category = existing.category
                paymentMethod = existing.paymentMethod
                contactName = existing.contactName
                selectedDateMillis = existing.dateMillis
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                }
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
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it; amountError = null },
                    label = { Text("Amount (Rs.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = amountError != null,
                    supportingText = { if (amountError != null) Text(amountError!!) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (useBsCalendar) "Nepali Date (BS)" else "English Date (AD)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = {
                        if (useBsCalendar) showBsDatePicker = true else showAdDatePicker = true
                    },
                    modifier = Modifier.fillMaxWidth().height(58.dp)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Choose date")
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = DateFormatter.format(selectedDateMillis, useBsCalendar),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (useBsCalendar) "BS" else "AD",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val amountValue = amount.toDoubleOrNull()
                        if (amountValue == null || amountValue <= 0.0) {
                            amountError = "Enter a valid amount greater than zero"
                            return@Button
                        }
                        if (isEditMode) {
                            viewModel.updateTransactionFull(
                                id = editTransactionId!!,
                                type = transactionType,
                                amount = amountValue,
                                description = description,
                                category = category.ifBlank { "Other" },
                                paymentMethod = paymentMethod,
                                contactName = contactName,
                                dateMillis = selectedDateMillis
                            )
                        } else {
                            viewModel.addTransaction(
                                type = transactionType,
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

    if (showBsDatePicker) {
        BsDatePickerDialog(
            initialDateMillis = selectedDateMillis,
            onDateSelected = {
                selectedDateMillis = it
                showBsDatePicker = false
            },
            onDismiss = { showBsDatePicker = false }
        )
    }

    if (showAdDatePicker) {
        AdDatePickerDialog(
            initialDateMillis = selectedDateMillis,
            onDateSelected = {
                selectedDateMillis = it
                showAdDatePicker = false
            },
            onDismiss = { showAdDatePicker = false }
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
                        viewModel.addCustomCategory(newCategoryName.trim(), transactionType)
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
