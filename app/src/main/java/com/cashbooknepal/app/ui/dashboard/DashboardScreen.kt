@file:OptIn(ExperimentalMaterial3Api::class)

package com.cashbooknepal.app.ui.dashboard

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cashbooknepal.app.data.local.entity.BookEntity
import com.cashbooknepal.app.data.local.entity.TransactionEntity
import com.cashbooknepal.app.settings.DateSettingsRepository
import com.cashbooknepal.app.ui.common.AdDatePickerDialog
import com.cashbooknepal.app.ui.common.BsDatePickerDialog
import com.cashbooknepal.app.utils.date.DateFormatter
import com.cashbooknepal.app.viewmodel.DateFilterOption
import com.cashbooknepal.app.viewmodel.TransactionFilters
import com.cashbooknepal.app.viewmodel.TransactionTypeFilter
import com.cashbooknepal.app.viewmodel.TransactionViewModel

@Composable
fun DashboardScreen(
    viewModel: TransactionViewModel = viewModel(),
    onAddCashIn: () -> Unit,
    onAddCashOut: () -> Unit,
    onEditTransaction: (Long, String) -> Unit,
    onOpenSettings: () -> Unit,
    onManageBusinesses: () -> Unit
) {
    val context = LocalContext.current
    val dateSettingsRepository = remember { DateSettingsRepository(context) }
    val useBsCalendar by dateSettingsRepository.useBsCalendar.collectAsState(initial = true)

    val transactions by viewModel.filteredTransactions.collectAsState()
    val totalCashIn by viewModel.totalCashIn.collectAsState()
    val totalCashOut by viewModel.totalCashOut.collectAsState()
    val balance = totalCashIn - totalCashOut
    val allBusinesses by viewModel.allBusinesses.collectAsState()
    val currentBusinessId by viewModel.currentBusinessId.collectAsState()
    val booksForCurrentBusiness by viewModel.booksForCurrentBusiness.collectAsState()
    val currentBookId by viewModel.currentBookId.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val paymentMethods by viewModel.allPaymentMethods.collectAsState()

    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var showBookDropdown by remember { mutableStateOf(false) }
    var showBusinessDropdown by remember { mutableStateOf(false) }
    var showAddBookDialog by remember { mutableStateOf(false) }
    var showAddBusinessDialog by remember { mutableStateOf(false) }
    var newBookName by remember { mutableStateOf("") }
    var newBusinessName by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }

    val currentBookName = booksForCurrentBusiness.find { it.id == currentBookId }?.name ?: "Main Cash Book"
    val currentBusinessName = allBusinesses.find { it.id == currentBusinessId }?.name ?: "My Business"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Box {
                            TextButton(
                                onClick = { showBusinessDropdown = true },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    currentBusinessName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Switch business",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            DropdownMenu(
                                expanded = showBusinessDropdown,
                                onDismissRequest = { showBusinessDropdown = false }
                            ) {
                                allBusinesses.forEach { business ->
                                    DropdownMenuItem(
                                        text = { Text(business.name) },
                                        onClick = {
                                            viewModel.selectBusiness(business.id)
                                            showBusinessDropdown = false
                                        }
                                    )
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("+ Add New Business") },
                                    onClick = {
                                        showBusinessDropdown = false
                                        showAddBusinessDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Manage Businesses") },
                                    onClick = {
                                        showBusinessDropdown = false
                                        onManageBusinesses()
                                    }
                                )
                            }
                        }
                        Box {
                            TextButton(
                                onClick = { showBookDropdown = true },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text(currentBookName, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Switch book")
                            }
                            DropdownMenu(
                                expanded = showBookDropdown,
                                onDismissRequest = { showBookDropdown = false }
                            ) {
                                booksForCurrentBusiness.forEach { book ->
                                    DropdownMenuItem(
                                        text = { Text(book.name) },
                                        onClick = {
                                            viewModel.selectBook(book.id)
                                            showBookDropdown = false
                                        }
                                    )
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("+ Add New Book") },
                                    onClick = {
                                        showBookDropdown = false
                                        showAddBookDialog = true
                                    }
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Current Balance",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rs. ${"%.2f".format(balance)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Color(0xFF2E7D32))
                        Text("Cash In", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Rs. ${"%.2f".format(totalCashIn)}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFFC62828))
                        Text("Cash Out", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Rs. ${"%.2f".format(totalCashOut)}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAddCashIn,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cash In")
                }
                Button(
                    onClick = onAddCashOut,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cash Out")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                BadgedBox(
                    badge = { if (filters.isActive) Badge() }
                ) {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Filters")
                    }
                }
            }

            OutlinedTextField(
                value = filters.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                placeholder = { Text("Search transactions") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (filters.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tap to edit, tap and hold to delete",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (filters.isActive)
                            "No transactions match your search or filters."
                        else
                            "No transactions yet.\nTap Cash In or Cash Out to add one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { transaction ->
                        TransactionRow(
                            transaction = transaction,
                            useBs = useBsCalendar,
                            onTap = { onEditTransaction(transaction.id, transaction.type) },
                            onLongPress = { transactionToDelete = transaction }
                        )
                    }
                }
            }
        }
    }

    transactionToDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete transaction?") },
            text = {
                Text("Delete this transaction of Rs. ${"%.2f".format(transaction.amount)}? This cannot be undone.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(transaction)
                    transactionToDelete = null
                }) {
                    Text("Delete", color = Color(0xFFC62828))
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddBookDialog) {
        AlertDialog(
            onDismissRequest = { showAddBookDialog = false; newBookName = "" },
            title = { Text("Add New Book") },
            text = {
                OutlinedTextField(
                    value = newBookName,
                    onValueChange = { newBookName = it },
                    label = { Text("Book name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newBookName.isNotBlank()) {
                        viewModel.addBook(newBookName.trim())
                    }
                    newBookName = ""
                    showAddBookDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    newBookName = ""
                    showAddBookDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddBusinessDialog) {
        AlertDialog(
            onDismissRequest = { showAddBusinessDialog = false; newBusinessName = "" },
            title = { Text("Add New Business") },
            text = {
                OutlinedTextField(
                    value = newBusinessName,
                    onValueChange = { newBusinessName = it },
                    label = { Text("Business name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newBusinessName.isNotBlank()) {
                        viewModel.addBusiness(newBusinessName.trim())
                    }
                    newBusinessName = ""
                    showAddBusinessDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    newBusinessName = ""
                    showAddBusinessDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showFilterDialog) {
        TransactionFilterDialog(
            filters = filters,
            useBsCalendar = useBsCalendar,
            availableCategories = availableCategories,
            availablePaymentMethods = paymentMethods.map { it.name },
            allBooks = booksForCurrentBusiness,
            currentBookId = currentBookId,
            onSelectBook = { viewModel.selectBook(it) },
            onApply = { updated ->
                viewModel.setSearchQuery(updated.searchQuery)
                viewModel.setTypeFilter(updated.typeFilter)
                viewModel.setCategoryFilter(updated.categoryFilter)
                viewModel.setPaymentMethodFilter(updated.paymentMethodFilter)
                viewModel.setDateFilter(updated.dateFilter, updated.customRangeStart, updated.customRangeEnd)
                showFilterDialog = false
            },
            onClear = {
                viewModel.clearFilters()
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TransactionRow(
    transaction: TransactionEntity,
    useBs: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    val isCashIn = transaction.type == "CASH_IN"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.description.ifBlank { transaction.category },
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = DateFormatter.format(transaction.dateMillis, useBs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = (if (isCashIn) "+ Rs. " else "- Rs. ") + "%.2f".format(transaction.amount),
                fontWeight = FontWeight.Bold,
                color = if (isCashIn) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFilterDialog(
    filters: TransactionFilters,
    useBsCalendar: Boolean,
    availableCategories: List<String>,
    availablePaymentMethods: List<String>,
    allBooks: List<BookEntity>,
    currentBookId: Long,
    onSelectBook: (Long) -> Unit,
    onApply: (TransactionFilters) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember { mutableStateOf(filters) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showPaymentDropdown by remember { mutableStateOf(false) }
    var showBookDropdown by remember { mutableStateOf(false) }
    var showCustomStartPicker by remember { mutableStateOf(false) }
    var showCustomEndPicker by remember { mutableStateOf(false) }

    val currentBookName = allBooks.find { it.id == currentBookId }?.name ?: "Main Cash Book"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filters") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = draft.typeFilter == TransactionTypeFilter.ALL,
                        onClick = { draft = draft.copy(typeFilter = TransactionTypeFilter.ALL) },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = draft.typeFilter == TransactionTypeFilter.CASH_IN,
                        onClick = { draft = draft.copy(typeFilter = TransactionTypeFilter.CASH_IN) },
                        label = { Text("Cash In") }
                    )
                    FilterChip(
                        selected = draft.typeFilter == TransactionTypeFilter.CASH_OUT,
                        onClick = { draft = draft.copy(typeFilter = TransactionTypeFilter.CASH_OUT) },
                        label = { Text("Cash Out") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Cashbook", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded = showBookDropdown,
                    onExpandedChange = { showBookDropdown = it }
                ) {
                    OutlinedTextField(
                        value = currentBookName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBookDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = showBookDropdown,
                        onDismissRequest = { showBookDropdown = false }
                    ) {
                        allBooks.forEach { book ->
                            DropdownMenuItem(
                                text = { Text(book.name) },
                                onClick = {
                                    onSelectBook(book.id)
                                    showBookDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Category", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = draft.categoryFilter ?: "All Categories",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                draft = draft.copy(categoryFilter = null)
                                showCategoryDropdown = false
                            }
                        )
                        availableCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    draft = draft.copy(categoryFilter = cat)
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Payment Method", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded = showPaymentDropdown,
                    onExpandedChange = { showPaymentDropdown = it }
                ) {
                    OutlinedTextField(
                        value = draft.paymentMethodFilter ?: "All Payment Methods",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPaymentDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = showPaymentDropdown,
                        onDismissRequest = { showPaymentDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Payment Methods") },
                            onClick = {
                                draft = draft.copy(paymentMethodFilter = null)
                                showPaymentDropdown = false
                            }
                        )
                        availablePaymentMethods.forEach { pm ->
                            DropdownMenuItem(
                                text = { Text(pm) },
                                onClick = {
                                    draft = draft.copy(paymentMethodFilter = pm)
                                    showPaymentDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Date Range", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))

                val dateOptions = listOf(
                    DateFilterOption.ALL to "All",
                    DateFilterOption.TODAY to "Today",
                    DateFilterOption.YESTERDAY to "Yesterday",
                    DateFilterOption.THIS_WEEK to "This Week",
                    DateFilterOption.THIS_MONTH to "This Month",
                    DateFilterOption.LAST_MONTH to "Last Month",
                    DateFilterOption.THIS_YEAR to "This Year",
                    DateFilterOption.CUSTOM to "Custom Range"
                )

                FlowRowFallback(dateOptions) { option, label ->
                    FilterChip(
                        selected = draft.dateFilter == option,
                        onClick = { draft = draft.copy(dateFilter = option) },
                        label = { Text(label) }
                    )
                }

                if (draft.dateFilter == DateFilterOption.CUSTOM) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showCustomStartPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            draft.customRangeStart?.let { "From: " + DateFormatter.format(it, useBsCalendar) }
                                ?: "Select start date"
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showCustomEndPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            draft.customRangeEnd?.let { "To: " + DateFormatter.format(it, useBsCalendar) }
                                ?: "Select end date"
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(draft) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClear) {
                    Text("Clear All")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )

    if (showCustomStartPicker) {
        if (useBsCalendar) {
            BsDatePickerDialog(
                initialDateMillis = draft.customRangeStart ?: System.currentTimeMillis(),
                onDateSelected = {
                    draft = draft.copy(customRangeStart = it)
                    showCustomStartPicker = false
                },
                onDismiss = { showCustomStartPicker = false }
            )
        } else {
            AdDatePickerDialog(
                initialDateMillis = draft.customRangeStart ?: System.currentTimeMillis(),
                onDateSelected = {
                    draft = draft.copy(customRangeStart = it)
                    showCustomStartPicker = false
                },
                onDismiss = { showCustomStartPicker = false }
            )
        }
    }

    if (showCustomEndPicker) {
        if (useBsCalendar) {
            BsDatePickerDialog(
                initialDateMillis = draft.customRangeEnd ?: System.currentTimeMillis(),
                onDateSelected = {
                    draft = draft.copy(customRangeEnd = it)
                    showCustomEndPicker = false
                },
                onDismiss = { showCustomEndPicker = false }
            )
        } else {
            AdDatePickerDialog(
                initialDateMillis = draft.customRangeEnd ?: System.currentTimeMillis(),
                onDateSelected = {
                    draft = draft.copy(customRangeEnd = it)
                    showCustomEndPicker = false
                },
                onDismiss = { showCustomEndPicker = false }
            )
        }
    }
}

@Composable
private fun FlowRowFallback(
    options: List<Pair<DateFilterOption, String>>,
    chip: @Composable (DateFilterOption, String) -> Unit
) {
    // Simple two-column wrap layout (avoids requiring the experimental FlowRow API).
    Column {
        options.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                rowItems.forEach { (option, label) ->
                    chip(option, label)
                }
            }
        }
    }
}