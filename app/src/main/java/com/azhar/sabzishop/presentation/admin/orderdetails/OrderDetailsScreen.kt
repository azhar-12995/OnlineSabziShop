package com.azhar.sabzishop.presentation.admin.orderdetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azhar.sabzishop.presentation.components.*
import com.azhar.sabzishop.utils.Constants
import com.azhar.sabzishop.utils.ImageUtils
import com.azhar.sabzishop.utils.formatQty
import com.azhar.sabzishop.utils.toRupees

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    onBackClick: () -> Unit,
    viewModel: OrderDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var statusMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSnackbar() }
    }

    Scaffold(
        topBar = { SabziTopBar(title = "Order Details", canNavigateBack = true, onNavigateBack = onBackClick) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                uiState.isLoading -> LoadingView()
                uiState.order == null -> ErrorView(uiState.errorMessage ?: "Not found")
                else -> {
                    val order = uiState.order!!
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Order Header Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text("Order", style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                        Text("#${order.orderId.take(12)}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                    StatusBadge(order.status)
                                }
                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                                Spacer(Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("Total Amount", style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                        Text(order.totalAmount.toRupees(),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Items", style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                        Text("${order.items.size}",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Order Status Timeline
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Order Progress", style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(16.dp))
                                val statuses = listOf(
                                    Constants.STATUS_PENDING, Constants.STATUS_CONFIRMED,
                                    Constants.STATUS_DELIVERED
                                )
                                val currentIdx = statuses.indexOf(order.status)
                                val isCancelled = order.status == Constants.STATUS_CANCELLED
                                statuses.forEachIndexed { idx, status ->
                                    val isCompleted = !isCancelled && idx <= currentIdx
                                    val isCurrent = !isCancelled && idx == currentIdx
                                    Row(verticalAlignment = Alignment.Top) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier.size(28.dp)
                                                    .background(
                                                        if (isCompleted) MaterialTheme.colorScheme.primary
                                                        else MaterialTheme.colorScheme.surfaceVariant,
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isCompleted) {
                                                    Icon(Icons.Default.Check, null,
                                                        tint = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            if (idx < statuses.lastIndex) {
                                                Box(modifier = Modifier.width(2.dp).height(28.dp)
                                                    .background(
                                                        if (!isCancelled && idx < currentIdx) MaterialTheme.colorScheme.primary
                                                        else MaterialTheme.colorScheme.surfaceVariant
                                                    ))
                                            }
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(status, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isCompleted) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (isCurrent) {
                                                Text("Current status", style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                                if (isCancelled) {
                                    Spacer(Modifier.height(8.dp))
                                    Surface(color = Color(0xFFF44336).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Cancel, null, tint = Color(0xFFF44336))
                                            Spacer(Modifier.width(8.dp))
                                            Text("This order has been cancelled", color = Color(0xFFF44336),
                                                fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }

                        // Customer Info
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Customer", style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold)
                                HorizontalDivider()
                                InfoRow(Icons.Default.Person, "Name", order.customerName)
                                InfoRow(Icons.Default.Phone, "Phone", order.customerPhone)
                                InfoRow(Icons.Default.LocationOn, "Address", order.deliveryAddress)
                            }
                        }

                        // Items Ordered - with images
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Items Ordered", style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                order.items.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Item image
                                        Box(
                                            modifier = Modifier.size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val bitmap = remember(item.imageBase64) {
                                                if (item.imageBase64.isNotBlank()) ImageUtils.base64ToBitmap(item.imageBase64) else null
                                            }
                                            if (bitmap != null) {
                                                Image(bitmap.asImageBitmap(), item.name,
                                                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                            } else {
                                                Icon(Icons.Default.Eco, null,
                                                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                            }
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.name, fontWeight = FontWeight.Medium)
                                            Text("${item.qty.formatQty()} × ${item.pricePerKg.toRupees()}/kg",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text(item.lineTotal.toRupees(), fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                // Totals
                                PriceRow("Subtotal", order.subtotal.toRupees())
                                PriceRow("Delivery", order.deliveryCharges.toRupees())
                                Spacer(Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total", fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium)
                                    Text(order.totalAmount.toRupees(), fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        // Status Update Section
                        if (viewModel.isAdmin) {
                            // Admin: full status dropdown
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text("Update Status", style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(12.dp))
                                    ExposedDropdownMenuBox(
                                        expanded = statusMenuExpanded,
                                        onExpandedChange = { statusMenuExpanded = it }
                                    ) {
                                        OutlinedTextField(
                                            value = order.status, onValueChange = {}, readOnly = true,
                                            label = { Text("Order Status") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenuExpanded) },
                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = statusMenuExpanded,
                                            onDismissRequest = { statusMenuExpanded = false }
                                        ) {
                                            viewModel.orderStatuses.forEach { status ->
                                                DropdownMenuItem(
                                                    text = { Text(status) },
                                                    onClick = {
                                                        viewModel.updateStatus(status)
                                                        statusMenuExpanded = false
                                                    },
                                                    leadingIcon = {
                                                        if (status == order.status) {
                                                            Icon(Icons.Default.Check, null,
                                                                tint = MaterialTheme.colorScheme.primary)
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (order.status == Constants.STATUS_PENDING || order.status == Constants.STATUS_CONFIRMED) {
                            // User: cancel button when order is Pending or Confirmed
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Button(
                                        onClick = { viewModel.updateStatus(Constants.STATUS_CANCELLED) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                                    ) {
                                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Cancel Order")
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
            // Overlay loader for status update
            OverlayLoader(isLoading = uiState.isUpdatingStatus, message = "Updating status...")
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
