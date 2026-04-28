package com.azhar.sabzishop.presentation.admin.itemsales

import android.graphics.Bitmap
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azhar.sabzishop.domain.model.ItemSalesData
import com.azhar.sabzishop.presentation.components.*
import com.azhar.sabzishop.utils.ImageUtils
import com.azhar.sabzishop.utils.toRupees

// Vivid palette
private val VGreen = Color(0xFF2E7D32)
private val VOrange = Color(0xFFE65100)
private val VBlue = Color(0xFF1565C0)
private val VTeal = Color(0xFF00796B)
private val VGold = Color(0xFFFF8F00)
private val VRed = Color(0xFFD32F2F)
private val VPurple = Color(0xFF6A1B9A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSalesScreen(
    onBackClick: () -> Unit,
    viewModel: ItemSalesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SabziTopBar(title = "Item-wise Sales", canNavigateBack = true, onNavigateBack = onBackClick)
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingView()
            uiState.errorMessage != null -> ErrorView(uiState.errorMessage!!)
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date filter chips
                item {
                    DateFilterRow(
                        selected = uiState.dateFilter,
                        onSelect = { viewModel.setDateFilter(it) },
                        onCustomClick = { showDatePicker = true }
                    )
                }

                // Summary cards row 1
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GradientSummary("Orders", "${uiState.totalOrders}",
                            Icons.Default.ShoppingCart, VBlue, Color(0xFF42A5F5), Modifier.weight(1f))
                        GradientSummary("Revenue", uiState.totalRevenue.toRupees(),
                            Icons.Default.Payments, VOrange, VGold, Modifier.weight(1f))
                    }
                }
                // Summary cards row 2
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GradientSummary("Items Sold", "${String.format("%.1f", uiState.totalItemsSold)} kg",
                            Icons.Default.Scale, VTeal, Color(0xFF4DB6AC), Modifier.weight(1f))
                        GradientSummary("Profit (5%)", uiState.totalProfit.toRupees(),
                            Icons.Default.TrendingUp, VGreen, Color(0xFF66BB6A), Modifier.weight(1f))
                    }
                }
                // Low stock alert
                if (uiState.lowStockItems.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = VRed, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("${uiState.lowStockItems.size} items with low stock (< 5 kg)",
                                    color = VRed, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Search
                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchChange,
                        placeholder = { Text("Search item...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                }

                // Top Selling Bar Chart
                if (uiState.itemSalesList.isNotEmpty()) {
                    item {
                        Text("Top Selling Items", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                    }
                    item { TopSellingBarChart(uiState.itemSalesList.take(5)) }
                }

                // Item-wise table
                item {
                    Text("Item-wise Order Record", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)) {
                            Text("Item", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1.4f))
                            Text("Orders", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.7f))
                            Text("Qty", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.8f))
                            Text("Revenue", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.9f))
                            Text("Stock", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.7f))
                        }
                    }
                }
                items(uiState.itemSalesList, key = { it.productId }) { item ->
                    ItemSalesRow(
                        item = item,
                        isSelected = uiState.selectedItem?.productId == item.productId,
                        onClick = { viewModel.selectItem(item) }
                    )
                }

                // Selected item detail
                uiState.selectedItem?.let { sel ->
                    item { SelectedItemDetailCard(sel) }
                }

                // Low stock list
                if (uiState.lowStockItems.isNotEmpty()) {
                    item {
                        Text("⚠ Low Stock Alerts", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold, color = VRed)
                    }
                    items(uiState.lowStockItems, key = { "low_${it.productId}" }) { item ->
                        LowStockRow(item)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        CustomDateRangeDialog(
            onDismiss = { showDatePicker = false },
            onConfirm = { start, end ->
                viewModel.setCustomRange(start, end)
                showDatePicker = false
            }
        )
    }
}

@Composable
private fun DateFilterRow(selected: DateFilter, onSelect: (DateFilter) -> Unit, onCustomClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DateFilter.entries.forEach { filter ->
            val colors = when (filter) {
                DateFilter.TODAY -> VGreen
                DateFilter.WEEKLY -> VBlue
                DateFilter.MONTHLY -> VPurple
                DateFilter.CUSTOM -> VTeal
            }
            FilterChip(
                selected = selected == filter,
                onClick = {
                    if (filter == DateFilter.CUSTOM) onCustomClick() else onSelect(filter)
                },
                label = {
                    Text(when (filter) {
                        DateFilter.TODAY -> "Today"
                        DateFilter.WEEKLY -> "This Week"
                        DateFilter.MONTHLY -> "This Month"
                        DateFilter.CUSTOM -> "Custom Range"
                    }, fontWeight = if (selected == filter) FontWeight.Bold else FontWeight.Normal)
                },
                leadingIcon = if (selected == filter) {
                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun GradientSummary(
    title: String, value: String, icon: ImageVector,
    c1: Color, c2: Color, modifier: Modifier = Modifier
) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(c1, c2)), shape = RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column {
                Icon(icon, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                Spacer(Modifier.height(6.dp))
                Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                Text(title, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun ItemSalesRow(item: ItemSalesData, isSelected: Boolean, onClick: () -> Unit) {
    val stockColor = if (item.currentStock < 5) VRed else VGreen
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.White
        ),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        border = if (isSelected) BorderStroke(2.dp, VGreen) else null
    ) {
        Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1.4f), verticalAlignment = Alignment.CenterVertically) {
                val bitmap: Bitmap? = remember(item.imageBase64) {
                    if (item.imageBase64.isNotBlank()) ImageUtils.base64ToBitmap(item.imageBase64) else null
                }
                if (bitmap != null) {
                    Image(bitmap.asImageBitmap(), item.productName,
                        modifier = Modifier.size(34.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop)
                } else {
                    Surface(modifier = Modifier.size(34.dp), shape = CircleShape,
                        color = Color(0xFFE8F5E9)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Grass, null, modifier = Modifier.size(18.dp), tint = VGreen)
                        }
                    }
                }
                Spacer(Modifier.width(6.dp))
                Text(item.productName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("${item.ordersCount}", fontSize = 12.sp, modifier = Modifier.weight(0.7f),
                fontWeight = FontWeight.Bold, color = VBlue)
            Text("${String.format("%.1f", item.totalQtySold)} kg", fontSize = 12.sp,
                modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold, color = VTeal)
            Text(item.totalRevenue.toRupees(), fontSize = 11.sp, modifier = Modifier.weight(0.9f),
                fontWeight = FontWeight.Bold, color = VOrange)
            Text("${item.currentStock} kg", fontSize = 12.sp, color = stockColor,
                fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(0.7f))
        }
    }
}

@Composable
private fun SelectedItemDetailCard(item: ItemSalesData) {
    val profit = item.totalRevenue * 0.05
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF1B5E20), Color(0xFF43A047))),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("📊 Selected Item Details", fontWeight = FontWeight.Bold,
                    color = Color.White, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))

                val bitmap: Bitmap? = remember(item.imageBase64) {
                    if (item.imageBase64.isNotBlank()) ImageUtils.base64ToBitmap(item.imageBase64) else null
                }
                if (bitmap != null) {
                    Image(bitmap.asImageBitmap(), item.productName,
                        modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop)
                    Spacer(Modifier.height(8.dp))
                }
                Text(item.productName, fontWeight = FontWeight.ExtraBold,
                    color = Color.White, fontSize = 18.sp)
                Spacer(Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailMiniCard("Orders", "${item.ordersCount}", VBlue, Modifier.weight(1f))
                    DetailMiniCard("Qty Sold", "${String.format("%.1f", item.totalQtySold)} kg", VTeal, Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailMiniCard("Revenue", item.totalRevenue.toRupees(), VOrange, Modifier.weight(1f))
                    DetailMiniCard("Profit (5%)", profit.toRupees(), VGold, Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                val stockColor = if (item.currentStock < 5) VRed else Color(0xFF66BB6A)
                DetailMiniCard("Current Stock", "${item.currentStock} kg", stockColor,
                    Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun DetailMiniCard(label: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = accentColor)
        }
    }
}

@Composable
private fun TopSellingBarChart(items: List<ItemSalesData>) {
    val maxQty = items.maxOfOrNull { it.totalQtySold } ?: 1.0
    val barColors = listOf(VGreen, VBlue, VOrange, VTeal, VPurple)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                items.forEachIndexed { index, item ->
                    val fraction = (item.totalQtySold / maxQty).toFloat().coerceAtLeast(0.03f)
                    val color = barColors[index % barColors.size]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.width(56.dp).fillMaxHeight()
                    ) {
                        Text(String.format("%.0f", item.totalQtySold),
                            fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
                        Spacer(Modifier.height(2.dp))
                        Canvas(modifier = Modifier.width(40.dp).fillMaxHeight(fraction)) {
                            drawRect(color = color, size = size)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (item.productName.length > 7) item.productName.take(7) + ".." else item.productName,
                            fontSize = 9.sp, maxLines = 1, fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text("Quantity Sold (kg)", fontSize = 10.sp,
                color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun LowStockRow(item: ItemSalesData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, VRed.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val bitmap: Bitmap? = remember(item.imageBase64) {
                    if (item.imageBase64.isNotBlank()) ImageUtils.base64ToBitmap(item.imageBase64) else null
                }
                if (bitmap != null) {
                    Image(bitmap.asImageBitmap(), null,
                        modifier = Modifier.size(36.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(8.dp))
                }
                Text(item.productName, fontWeight = FontWeight.SemiBold)
            }
            Text("${item.currentStock} kg", color = VRed,
                fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomDateRangeDialog(onDismiss: () -> Unit, onConfirm: (Long, Long) -> Unit) {
    val dateRangePickerState = rememberDateRangePickerState()
    AlertDialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().heightIn(max = 550.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Date Range", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier.weight(1f, fill = false).heightIn(max = 400.dp)
                )
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val start = dateRangePickerState.selectedStartDateMillis
                        val end = dateRangePickerState.selectedEndDateMillis
                        if (start != null && end != null) onConfirm(start, end)
                    }) { Text("Apply") }
                }
            }
        }
    }
}
