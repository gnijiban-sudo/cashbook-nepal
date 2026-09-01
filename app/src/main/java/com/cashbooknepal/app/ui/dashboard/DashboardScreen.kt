@file:OptIn(ExperimentalMaterial3Api::class)

package com.cashbooknepal.app.ui.dashboard

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cashbooknepal.app.data.local.entity.TransactionEntity
import com.cashbooknepal.app.settings.DateSettingsRepository
import com.cashbooknepal.app.utils.date.DateFormatter
import com.cashbooknepal.app.viewmodel.TransactionViewModel

@Composable
fun DashboardScreen(
    viewModel: TransactionViewModel = viewModel(),
    onAddCashIn: () -> Unit,
    onAddCashOut: () -> Unit,
    onEditTransaction: (Long, String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val dateSettingsRepository = remember { DateSettingsRepository(context) }
    val useBsCalendar by dateSettingsRepository.useBsCalendar.collectAsState(initial = true)

    val transactions by viewModel.allTransactions.collectAsState()
    val totalCashIn by viewModel.totalCashIn.collectAsState()
    val totalCashOut by viewModel.totalCashOut.collectAsState()
    val balance = totalCashIn - totalCashOut
    val allBooks by viewModel.allBooks.collectAsState()
    val currentBookId by viewModel.currentBookId.collectAsState()

    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var showBookDropdown by remember { mutableStateOf(false) }
    var showAddBookDialog by remember { mutableStateOf(false) }
    var newBookName by remember { mutableStateOf("") }

    val currentBookName = allBooks.find { it.id == currentBookId }?.name ?: "Main Cash Book"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box {
                        TextButton(onClick = { showBookDropdown = true }) {
                            Text(currentBookName, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Switch book")
                        }
                        DropdownMenu(
                            expanded = showBookDropdown,
                            onDismissRequest = { showBookDropdown = false }
                        ) {
                            allBooks.forEach { book ->
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

            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
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
                        text = "No transactions yet.\nTap Cash In or Cash Out to add one.",
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