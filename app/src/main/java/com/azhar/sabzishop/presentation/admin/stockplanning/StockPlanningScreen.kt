package com.azhar.sabzishop.presentation.admin.stockplanning

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azhar.sabzishop.presentation.admin.dashboard.AdminDashboardViewModel
import com.azhar.sabzishop.presentation.admin.dashboard.StockPlanningItem
import com.azhar.sabzishop.presentation.components.*
import com.azhar.sabzishop.utils.ImageUtils
import com.azhar.sabzishop.utils.toRupees

private val VGreen = Color(0xFF2E7D32)
private val VOrange = Color(0xFFE65100)
private val VBlue = Color(0xFF1565C0)
private val VTeal = Color(0xFF00796B)
private val VGold = Color(0xFFFF8F00)
private val VRed = Color(0xFFD32F2F)

@Composable
fun StockPlanningScreen(
    onBackClick: () -> Unit,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyLowStock by remember { mutableStateOf(false) }

    val items = uiState.stockPlanningItems.let { list ->
        var filtered = list
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { it.productName.contains(searchQuery, ignoreCase = true) }
        }
        if (showOnlyLowStock) {
            filtered = filtered.filter { it.daysOfStockLeft in 0.0..7.0 }
        }
        filtered
    }

    val criticalCount = uiState.stockPlanningItems.count { it.daysOfStockLeft in 0.0..3.0 }
    val lowCount = uiState.stockPlanningItems.count { it.daysOfStockLeft in 0.0..7.0 }
    val healthyCount = uiState.stockPlanningItems.count { it.daysOfStockLeft < 0 || it.daysOfStockLeft > 7 }

    Scaffold(
        topBar = { SabziTopBar(title = "Stock Planning", canNavigateBack = true, onNavigateBack = onBackClick) }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingView()
            uiState.stockPlanningItems.isEmpty() -> EmptyStateView("No products found")
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Summary banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(listOf(VGreen, VTeal)),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(20.dp)
                        ) {
                            Column {
                                Text("📦 Stock Overview", color = Color.White,
                                    fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(14.dp))
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly) {
                                    MiniStat("Total", "${uiState.stockPlanningItems.size}", Color.White)
                                    MiniStat("Critical", "$criticalCount", Color(0xFFFF8A80))
                                    MiniStat("Low", "$lowCount", Color(0xFFFFE082))
                                    MiniStat("Healthy", "$healthyCount", Color(0xFFA5D6A7))
                                }
                            }
                        }
                    }
                }

                // How to read
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("How to read:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VGreen)
                            Spacer(Modifier.height(4.dp))
                            Text("• Ordered = total kg ordered by customers (all time)", fontSize = 12.sp, color = Color.DarkGray)
                            Text("• Stock = current stock in kg", fontSize = 12.sp, color = Color.DarkGray)
                            Text("• Today = kg ordered today", fontSize = 12.sp, color = Color.DarkGray)
                            Text("• Avg/Day = average daily demand (last 7 days)", fontSize = 12.sp, color = Color.DarkGray)
                            Text("• Days Left = how many days current stock will last", fontSize = 12.sp, color = Color.DarkGray)
                            Text("• 🔴 Red = restock urgently (< 3 days)", fontSize = 12.sp, color = VRed)
                            Text("• 🟠 Orange = restock soon (< 7 days)", fontSize = 12.sp, color = VOrange)
                            Text("• 🟢 Green = stock is healthy", fontSize = 12.sp, color = VGreen)
                        }
                    }
                }

                // Search + filter
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search product...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !showOnlyLowStock,
                            onClick = { showOnlyLowStock = false },
                            label = { Text("All (${uiState.stockPlanningItems.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = showOnlyLowStock,
                            onClick = { showOnlyLowStock = true },
                            label = { Text("Low Stock ($lowCount)", color = if (showOnlyLowStock) Color.White else VRed) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VRed,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Stock bar chart
                if (uiState.stockItems.isNotEmpty()) {
                    item {
                        Text("Stock Bar Chart", fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium)
                    }
                    item {
                        StockBarChartLarge(uiState.stockItems)
                    }
                }

                // Product cards
                item {
                    Text("${items.size} product(s)", fontSize = 13.sp, color = Color.Gray)
                }

                items(items, key = { it.productId }) { item ->
                    StockProductCard(item)
                }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        Text(label, color = color.copy(alpha = 0.8f), fontSize = 11.sp)
    }
}

@Composable
private fun StockProductCard(item: StockPlanningItem) {
    val stockColor = when {
        item.currentStock < 5 -> VRed
        item.currentStock < 15 -> VOrange
        else -> VGreen
    }
    val daysColor = when {
        item.daysOfStockLeft < 0 -> Color.Gray
        item.daysOfStockLeft <= 3 -> VRed
        item.daysOfStockLeft <= 7 -> VOrange
        else -> VGreen
    }
    val borderColor = when {
        item.daysOfStockLeft in 0.0..3.0 -> VRed
        item.daysOfStockLeft in 0.0..7.0 -> VOrange
        else -> Color.Transparent
    }
    val bgColor = when {
        item.daysOfStockLeft in 0.0..3.0 -> Color(0xFFFFEBEE)
        item.daysOfStockLeft in 0.0..7.0 -> Color(0xFFFFF8E1)
        else -> Color.White
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = if (borderColor != Color.Transparent) BorderStroke(1.5.dp, borderColor) else null,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top: image + name + status indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                val bitmap: Bitmap? = remember(item.imageBase64) {
                    if (item.imageBase64.isNotBlank()) ImageUtils.base64ToBitmap(item.imageBase64) else null
                }
                if (bitmap != null) {
                    Image(bitmap.asImageBitmap(), null,
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop)
                } else {
                    Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE8F5E9)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Grass, null, tint = VGreen, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.productName, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    // Status label
                    val statusText = when {
                        item.daysOfStockLeft in 0.0..3.0 -> "⚠️ Critical — restock now!"
                        item.daysOfStockLeft in 0.0..7.0 -> "🟡 Low — restock soon"
                        item.daysOfStockLeft < 0 -> "✅ No recent demand"
                        else -> "✅ Stock healthy"
                    }
                    Text(statusText, fontSize = 11.sp, color = daysColor, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Stats grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatBox("Ordered", if (item.totalOrderedQtyKg > 0) "${String.format("%.1f", item.totalOrderedQtyKg)} kg" else "—",
                    if (item.totalOrderedQtyKg > 0) VBlue else Color.Gray, Modifier.weight(1f))
                StatBox("Stock", "${item.currentStock} kg", stockColor, Modifier.weight(1f))
                StatBox("Today", if (item.todayQtyKg > 0) "${String.format("%.1f", item.todayQtyKg)} kg" else "—",
                    if (item.todayQtyKg > 0) VOrange else Color.Gray, Modifier.weight(1f))
                StatBox("Avg/Day", if (item.avgDailyDemand > 0) "${String.format("%.1f", item.avgDailyDemand)} kg" else "—",
                    if (item.avgDailyDemand > 0) VBlue else Color.Gray, Modifier.weight(1f))
                StatBox("Days Left",
                    if (item.daysOfStockLeft < 0) "∞" else "${String.format("%.0f", item.daysOfStockLeft)}d",
                    daysColor, Modifier.weight(1f))
            }

            // Today's revenue if any
            if (item.todayRevenue > 0) {
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Today: ${item.todayOrders} order(s)", fontSize = 12.sp, color = Color.Gray)
                    Text("Revenue: ${item.todayRevenue.toRupees()}", fontSize = 13.sp,
                        fontWeight = FontWeight.Bold, color = VOrange)
                }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
private fun StockBarChartLarge(stockItems: List<Pair<String, Int>>) {
    val maxStock = stockItems.maxOfOrNull { it.second } ?: 1
    val greenColor = Color(0xFF43A047)
    val orangeColor = Color(0xFFFF9800)
    val redColor = Color(0xFFE53935)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBE7)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                stockItems.forEach { (name, qty) ->
                    val barColor = when {
                        qty < 5 -> redColor
                        qty < 15 -> orangeColor
                        else -> greenColor
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.width(52.dp).fillMaxHeight()
                    ) {
                        Text("$qty", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = barColor)
                        Spacer(Modifier.height(2.dp))
                        val fraction = if (maxStock > 0) qty.toFloat() / maxStock else 0f
                        Canvas(modifier = Modifier.width(36.dp).fillMaxHeight(fraction.coerceAtLeast(0.02f))) {
                            drawRect(color = barColor, size = size)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(if (name.length > 7) name.take(7) + ".." else name,
                            fontSize = 9.sp, maxLines = 1, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            // Legend
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically) {
                LegendDot(redColor, "< 5 kg")
                Spacer(Modifier.width(12.dp))
                LegendDot(orangeColor, "< 15 kg")
                Spacer(Modifier.width(12.dp))
                LegendDot(greenColor, "Healthy")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(10.dp), shape = CircleShape, color = color) {}
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

