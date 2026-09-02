@file:OptIn(ExperimentalMaterial3Api::class)

package com.cashbooknepal.app.ui.business

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cashbooknepal.app.data.local.entity.BusinessEntity
import com.cashbooknepal.app.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch

@Composable
fun BusinessManagementScreen(
    viewModel: TransactionViewModel = viewModel(),
    onBack: () -> Unit
) {
    val businesses by viewModel.allBusinesses.collectAsState()
    val currentBusinessId by viewModel.currentBusinessId.collectAsState()
    val scope = rememberCoroutineScope()

    var showEditor by remember { mutableStateOf(false) }
    var editingBusiness by remember { mutableStateOf<BusinessEntity?>(null) }
    var businessToDelete by remember { mutableStateOf<BusinessEntity?>(null) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Businesses") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingBusiness = null
                showEditor = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add business")
            }
        }
    ) { paddingValues ->
        if (businesses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No businesses yet.\nTap + to add one.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(businesses) { business ->
                    val isCurrent = business.id == currentBusinessId
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(business.name, fontWeight = FontWeight.Bold)
                                    if (isCurrent) {
                                        Text(
                                            "Current business",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                IconButton(onClick = {
                                    editingBusiness = business
                                    showEditor = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit ${business.name}")
                                }
                                IconButton(onClick = {
                                    businessToDelete = business
                                    deleteError = null
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete ${business.name}",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            if (business.phone.isNotBlank() || business.address.isNotBlank() ||
                                business.panVat.isNotBlank()
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))
                                if (business.phone.isNotBlank()) {
                                    Text(
                                        "Phone: ${business.phone}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (business.address.isNotBlank()) {
                                    Text(
                                        "Address: ${business.address}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (business.panVat.isNotBlank()) {
                                    Text(
                                        "PAN/VAT: ${business.panVat}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (!isCurrent) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(onClick = { viewModel.selectBusiness(business.id) }) {
                                    Text("Switch to this business")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        BusinessEditorDialog(
            business = editingBusiness,
            onDismiss = { showEditor = false },
            onSave = { name, phone, address, panVat, notes ->
                val existing = editingBusiness
                if (existing != null) {
                    viewModel.updateBusiness(
                        existing.copy(
                            name = name,
                            phone = phone,
                            address = address,
                            panVat = panVat,
                            notes = notes
                        )
                    )
                } else {
                    viewModel.addBusiness(name, phone, address, panVat, notes)
                }
                showEditor = false
            }
        )
    }

    businessToDelete?.let { business ->
        AlertDialog(
            onDismissRequest = { businessToDelete = null },
            title = { Text("Delete ${business.name}?") },
            text = {
                Column {
                    Text("This will permanently remove this business and its empty cashbooks. This cannot be undone.")
                    deleteError?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val error = viewModel.deleteBusiness(business)
                        if (error == null) {
                            businessToDelete = null
                        } else {
                            deleteError = error
                        }
                    }
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { businessToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun BusinessEditorDialog(
    business: BusinessEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, address: String, panVat: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(business?.name ?: "") }
    var phone by remember { mutableStateOf(business?.phone ?: "") }
    var address by remember { mutableStateOf(business?.address ?: "") }
    var panVat by remember { mutableStateOf(business?.panVat ?: "") }
    var notes by remember { mutableStateOf(business?.notes ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (business == null) "Add Business" else "Edit Business") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Business name") },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = { if (nameError != null) Text(nameError!!) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = panVat,
                    onValueChange = { panVat = it },
                    label = { Text("PAN/VAT (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) {
                    nameError = "Business name is required"
                    return@TextButton
                }
                onSave(name.trim(), phone.trim(), address.trim(), panVat.trim(), notes.trim())
            }) {
                Text(if (business == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
