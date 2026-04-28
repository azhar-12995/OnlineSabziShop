package com.azhar.sabzishop.presentation.admin.manageproducts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azhar.sabzishop.domain.model.Product
import com.azhar.sabzishop.presentation.components.*
import com.azhar.sabzishop.utils.ImageUtils
import com.azhar.sabzishop.utils.toRupees

@Composable
fun ManageProductsScreen(
    onBackClick: () -> Unit,
    onAddProductClick: () -> Unit,
    onEditProductClick: (String) -> Unit,
    viewModel: ManageProductsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteConfirmProduct by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSnackbar() }
    }

    deleteConfirmProduct?.let { product ->
        AlertDialog(
            onDismissRequest = { deleteConfirmProduct = null },
            title = { Text("Delete Product?") },
            text = { Text("Are you sure you want to delete \"${product.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProduct(product.id)
                    deleteConfirmProduct = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmProduct = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = { SabziTopBar(title = "Manage Products", canNavigateBack = true, onNavigateBack = onBackClick) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProductClick,
                containerColor = MaterialTheme.colorScheme.secondary) {
                Icon(Icons.Default.Add, null)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearch,
                placeholder = { Text("Search products...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )
            when {
                uiState.isLoading -> LoadingView()
                uiState.filteredProducts.isEmpty() -> EmptyStateView("No products found. Add one!")
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.filteredProducts, key = { it.id }) { product ->
                        AdminProductCard(
                            product = product,
                            onEdit = { onEditProductClick(product.id) },
                            onDelete = { deleteConfirmProduct = product },
                            onToggle = { viewModel.toggleAvailability(product.id, product.isAvailable) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center) {
                val bitmap = remember(product.imageBase64) {
                    if (product.imageBase64.isNotBlank()) ImageUtils.base64ToBitmap(product.imageBase64) else null
                }
                if (bitmap != null) {
                    Image(bitmap.asImageBitmap(), product.name,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Eco, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("${product.pricePerKg.toRupees()}/kg · Stock: ${product.stockQty}",
                    style = MaterialTheme.typography.bodySmall)
                Text(product.category, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
            // Availability toggle
            Switch(checked = product.isAvailable, onCheckedChange = { onToggle() })
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
        }
    }
}


