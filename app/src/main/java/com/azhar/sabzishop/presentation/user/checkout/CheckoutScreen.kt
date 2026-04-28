package com.azhar.sabzishop.presentation.user.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azhar.sabzishop.presentation.components.*

@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    onOrderPlaced: (String) -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.placedOrderId) {
        uiState.placedOrderId?.let { onOrderPlaced(it) }
    }

    Scaffold(
        topBar = { SabziTopBar(title = "Checkout", canNavigateBack = true, onNavigateBack = onBackClick) }
    ) { padding ->
        if (uiState.isLoadingUser) {
            LoadingView()
        } else {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize()
                    .verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Delivery Details", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Your saved address is shown below. You can edit it here — changes will also update your profile.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SabziTextField(
                    value = uiState.deliveryAddress,
                    onValueChange = viewModel::onAddressChange,
                    label = "Area / City",
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) }
                )

                SabziTextField(
                    value = uiState.street,
                    onValueChange = viewModel::onStreetChange,
                    label = "Street",
                    leadingIcon = { Icon(Icons.Default.Signpost, null) }
                )

                SabziTextField(
                    value = uiState.houseNumber,
                    onValueChange = viewModel::onHouseNumberChange,
                    label = "House Number",
                    leadingIcon = { Icon(Icons.Default.Home, null) }
                )

                SabziTextField(
                    value = uiState.phone,
                    onValueChange = viewModel::onPhoneChange,
                    label = "Phone Number",
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                uiState.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(8.dp))

                SabziButton(
                    text = "Place Order",
                    onClick = viewModel::placeOrder,
                    isLoading = uiState.isLoading,
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
