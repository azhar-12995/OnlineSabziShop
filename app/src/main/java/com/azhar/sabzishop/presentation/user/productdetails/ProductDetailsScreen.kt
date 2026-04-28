package com.azhar.sabzishop.presentation.user.productdetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azhar.sabzishop.presentation.components.*
import com.azhar.sabzishop.utils.ImageUtils
import com.azhar.sabzishop.utils.formatQty
import com.azhar.sabzishop.utils.toRupees

@Composable
fun ProductDetailsScreen(
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onLoginRequired: () -> Unit,
    onRelatedProductClick: (String) -> Unit = {},
    viewModel: ProductDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSnackbar() }
    }
    LaunchedEffect(uiState.requiresLogin) {
        if (uiState.requiresLogin) { viewModel.clearLoginPrompt(); onLoginRequired() }
    }

    Scaffold(
        topBar = { SabziTopBar(
            title = uiState.product?.name ?: "Product",
            canNavigateBack = true, onNavigateBack = onBackClick,
            onCartClick = onCartClick,
            cartItemCount = uiState.cartItemCount
        ) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                uiState.isLoading -> LoadingView()
                uiState.errorMessage != null -> ErrorView(uiState.errorMessage!!)
                uiState.product == null -> EmptyStateView("Product not found")
                else -> {
                    val product = uiState.product!!
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        // Product Image
                        Box(modifier = Modifier.fillMaxWidth().height(280.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center) {
                            val bitmap = remember(product.imageBase64) {
                                if (product.imageBase64.isNotBlank()) ImageUtils.base64ToBitmap(product.imageBase64) else null
                            }
                            if (bitmap != null) {
                                Image(bitmap.asImageBitmap(), product.name,
                                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else {
                                Icon(Icons.Default.Eco, null, tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(100.dp))
                            }
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(product.name, style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("${product.pricePerKg.toRupees()}/kg",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp))
                                Text(" Farm Fresh · Stock: ${product.stockQty} kg",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("About this item", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text(product.description, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(16.dp))

                            // Quality badges
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                QualityBadge("100% Fresh")
                                QualityBadge("No Chemicals")
                                QualityBadge("Hand Picked")
                            }

                            // Quantity Presets
                            Spacer(Modifier.height(24.dp))
                            Text("Select Quantity", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(12.dp))

                            // Quantity preset chips in a flow layout
                            val chunked = QUANTITY_PRESETS.chunked(4)
                            chunked.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { preset ->
                                        val isSelected = uiState.selectedQtyKg == preset.valueKg
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { viewModel.selectQtyPreset(preset) },
                                            label = { Text(preset.label, style = MaterialTheme.typography.labelMedium) },
                                            leadingIcon = if (isSelected) {
                                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                            } else null,
                                            modifier = Modifier.weight(1f),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }

                            Spacer(Modifier.height(16.dp))

                            // Custom quantity input
                            var customQtyText by remember { mutableStateOf("") }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = customQtyText,
                                    onValueChange = { newVal ->
                                        // Allow only valid decimal numbers
                                        if (newVal.isEmpty() || newVal.matches(Regex("^\\d*\\.?\\d*$"))) {
                                            customQtyText = newVal
                                        }
                                    },
                                    label = { Text("Custom Qty (kg)") },
                                    placeholder = { Text("e.g. 3.5") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) }
                                )
                                Button(
                                    onClick = {
                                        val kg = customQtyText.toDoubleOrNull()
                                        if (kg != null && kg > 0) {
                                            viewModel.setCustomQty(kg)
                                            customQtyText = ""
                                        }
                                    },
                                    enabled = customQtyText.toDoubleOrNull()?.let { it > 0 } == true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Text("Set", fontSize = 13.sp)
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Price summary card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Selected: ${uiState.selectedQtyKg.formatQty()}",
                                            style = MaterialTheme.typography.bodyMedium)
                                        Text("${product.pricePerKg.toRupees()}/kg × ${uiState.selectedQtyKg.formatQty()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                                    }
                                    val total = product.pricePerKg * uiState.selectedQtyKg
                                    Text(total.toRupees(),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            SabziButton(
                                text = "🛒 Add to Cart",
                                onClick = { viewModel.addToCart() },
                                containerColor = MaterialTheme.colorScheme.secondary,
                                enabled = product.isAvailable && product.stockQty > 0 && !uiState.isAddingToCart
                            )
                            if (!product.isAvailable || product.stockQty == 0) {
                                Spacer(Modifier.height(8.dp))
                                Text("Currently out of stock", color = MaterialTheme.colorScheme.error)
                            }

                            // Related Products Section
                            Spacer(Modifier.height(24.dp))
                            if (uiState.isLoadingRelated) {
                                Text("Related Items", style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(60.dp),
                                    contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                }
                            } else if (uiState.relatedProducts.isNotEmpty()) {
                                Text("Related Items", style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(uiState.relatedProducts, key = { it.id }) { relatedProduct ->
                                        ProductCard(
                                            product = relatedProduct,
                                            onClick = { onRelatedProductClick(relatedProduct.id) }
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
            // Overlay loader when adding to cart
            OverlayLoader(isLoading = uiState.isAddingToCart, message = "Adding to cart...")
        }
    }
}

@Composable
private fun QualityBadge(text: String) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}
