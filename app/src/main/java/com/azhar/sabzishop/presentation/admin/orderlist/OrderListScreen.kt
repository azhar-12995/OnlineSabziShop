package com.azhar.sabzishop.presentation.admin.orderlist

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azhar.sabzishop.presentation.components.*

@Composable
fun OrderListScreen(
    onBackClick: () -> Unit,
    onOrderClick: (String) -> Unit,
    viewModel: OrderListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { SabziTopBar(title = "All Orders", canNavigateBack = true, onNavigateBack = onBackClick) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Date filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateFilterOption.entries.forEach { option ->
                    FilterChip(
                        selected = uiState.selectedDateFilter == option,
                        onClick = { viewModel.selectDateFilter(option) },
                        label = {
                            Text(
                                when (option) {
                                    DateFilterOption.ALL -> "All Time"
                                    DateFilterOption.TODAY -> "Today"
                                    DateFilterOption.YESTERDAY -> "Yesterday"
                                },
                                fontSize = 13.sp,
                                fontWeight = if (uiState.selectedDateFilter == option) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (uiState.selectedDateFilter == option) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1B5E20),
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }

            // Status tabs (scrollable)
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedStatusTab,
                edgePadding = 12.dp
            ) {
                OrderListViewModel.STATUS_TABS.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedStatusTab == index,
                        onClick = { viewModel.selectStatusTab(index) },
                        text = { Text(title, fontSize = 13.sp) }
                    )
                }
            }

            // Count badge
            Text(
                "${uiState.filteredOrders.size} order(s)",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                fontSize = 12.sp, color = Color.Gray
            )

            when {
                uiState.isLoading -> LoadingView()
                uiState.errorMessage != null -> ErrorView(uiState.errorMessage!!)
                uiState.filteredOrders.isEmpty() -> EmptyStateView("No orders found")
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredOrders, key = { it.orderId }) { order ->
                        OrderCard(
                            order = order,
                            onClick = { onOrderClick(order.orderId) },
                            showCustomerName = true
                        )
                    }
                }
            }
        }
    }
}
