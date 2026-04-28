package com.azhar.sabzishop.presentation.user.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azhar.sabzishop.presentation.components.*
import com.azhar.sabzishop.utils.toRupees

@Composable
fun CartScreen(
    onBackClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { SabziTopBar(title = "Your Cart", canNavigateBack = true, onNavigateBack = onBackClick) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Login prompt if not logged in
            if (!uiState.isLoggedIn) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Login to your account\nto proceed to checkout",
                            style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        SabziButton("Login / Sign Up", onClick = onLoginClick,
                            containerColor = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            when {
                uiState.isLoading -> LoadingView()
                uiState.items.isEmpty() -> EmptyStateView("Your cart is empty")
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.items, key = { it.itemId }) { item ->
                            CartItemRow(
                                cartItem = item,
                                onIncrease = { viewModel.increaseQty(item) },
                                onDecrease = { viewModel.decreaseQty(item) },
                                onRemove = { viewModel.removeItem(item) }
                            )
                        }
                    }

                    // Order summary
                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SummaryRow("Subtotal (${uiState.items.size} items)", uiState.subtotal.toRupees())
                            SummaryRow("Delivery Charge", uiState.deliveryCharge.toRupees())
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            SummaryRow("Total Amount", uiState.total.toRupees(), isBold = true)
                            Spacer(Modifier.height(16.dp))
                            SabziButton(
                                text = "Place Order",
                                onClick = {
                                    if (uiState.isLoggedIn) onCheckoutClick() else onLoginClick()
                                },
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                            if (!uiState.isLoggedIn) {
                                Spacer(Modifier.height(8.dp))
                                Text("🔒 Login required to place order",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.align(Alignment.CenterHorizontally))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}

