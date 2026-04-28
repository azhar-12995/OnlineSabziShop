package com.azhar.sabzishop.presentation.admin.dashboard

import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azhar.sabzishop.presentation.components.*
import com.azhar.sabzishop.utils.toRupees

// Vivid colors for dashboard cards
private val CardGreen = Color(0xFF2E7D32)
private val CardOrange = Color(0xFFE65100)
private val CardBlue = Color(0xFF1565C0)
private val CardTeal = Color(0xFF00796B)
private val CardPurple = Color(0xFF6A1B9A)
private val CardGold = Color(0xFFFF8F00)
private val CardRed = Color(0xFFD32F2F)
private val CardPink = Color(0xFFC2185B)

@Composable
fun AdminDashboardScreen(
    onManageProductsClick: () -> Unit,
    onOrderListClick: () -> Unit,
    onOrderClick: (String) -> Unit,
    onItemSalesClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onRevenueClick: () -> Unit,
    onStockPlanningClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.loggedOut) {
        if (uiState.loggedOut) onLogout()
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; viewModel.logout() }) {
                    Text("Yes, Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            SabziTopBar(
                title = "Admin Dashboard",
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) { Icon(Icons.Default.Logout, null) }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingView()
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ===== TODAY'S SALES BANNER =====
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(listOf(CardGreen, CardTeal)),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Today, null, tint = Color.White,
                                        modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Today's Sales", color = Color.White,
                                        fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                }
                                Spacer(Modifier.height(16.dp))
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    TodayMiniStat("Orders", "${uiState.todayOrders}", Icons.Default.ShoppingBag)
                                    TodayMiniStat("Revenue", uiState.todayRevenue.toRupees(), Icons.Default.AccountBalanceWallet)
                                    TodayMiniStat("Profit", uiState.todayProfit.toRupees(), Icons.Default.TrendingUp)
                                }
                            }
                        }
                    }
                }

                // ===== OVERVIEW STAT GRID =====
                item {
                    Text("Overview", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GradientStatCard("Total Orders", "${uiState.totalOrders}",
                            Icons.Default.ShoppingCart, CardBlue, Color(0xFF42A5F5), Modifier.weight(1f))
                        GradientStatCard("Pending", "${uiState.pendingOrders}",
                            Icons.Default.HourglassTop, CardOrange, CardGold, Modifier.weight(1f))
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GradientStatCard("Delivered", "${uiState.completedOrders}",
                            Icons.Default.CheckCircle, CardGreen, Color(0xFF66BB6A), Modifier.weight(1f))
                        GradientStatCard("Cancelled", "${uiState.cancelledOrders}",
                            Icons.Default.Cancel, CardRed, CardPink, Modifier.weight(1f))
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GradientStatCard("Products", "${uiState.totalProducts}",
                            Icons.Default.Inventory, CardPurple, Color(0xFFAB47BC), Modifier.weight(1f))
                        GradientStatCard("Revenue", uiState.totalRevenue.toRupees(),
                            Icons.Default.Payments, CardTeal, Color(0xFF4DB6AC), Modifier.weight(1f))
                    }
                }

                // ===== PROFIT CARD =====
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(listOf(CardGold, CardOrange)),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(18.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    Text("Total Profit (5%)", color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 13.sp)
                                    Text(uiState.totalProfit.toRupees(), color = Color.White,
                                        fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
                                    Text("Based on delivered orders", color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 10.sp)
                                }
                                Icon(Icons.Default.TrendingUp, null, tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp))
                            }
                        }
                    }
                }

                // ===== QUICK ACTIONS =====
                item {
                    Text("Quick Actions", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickActionButton("Products", Icons.Default.Inventory, CardPurple,
                            onManageProductsClick, Modifier.weight(1f))
                        QuickActionButton("Orders", Icons.Default.ListAlt, CardBlue,
                            onOrderListClick, Modifier.weight(1f))
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickActionButton("Item Sales", Icons.Default.BarChart, CardTeal,
                            onItemSalesClick, Modifier.weight(1f))
                        QuickActionButton("Feedbacks", Icons.Default.Feedback, CardGold,
                            onFeedbackClick, Modifier.weight(1f))
                    }
                }
                item {
                    QuickActionButton("Revenue & Profit", Icons.Default.AccountBalance, CardBlue,
                        onRevenueClick, Modifier.fillMaxWidth())
                }

                // ===== STOCK PLANNING SUMMARY + BUTTON =====
                val criticalCount = uiState.stockPlanningItems.count { it.daysOfStockLeft in 0.0..3.0 }
                if (criticalCount > 0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            border = BorderStroke(1.dp, CardRed.copy(alpha = 0.3f))
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = CardRed, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("⚠ $criticalCount item(s) need restocking!",
                                        fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CardRed)
                                    Text("Less than 3 days of stock left", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickActionButton("Stock Planning", Icons.Default.Inventory2, CardTeal,
                            onStockPlanningClick, Modifier.weight(1f))
                    }
                }

                // ===== RECENT ORDERS =====
                item {
                    Text("Recent Orders", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                }
                if (uiState.recentOrders.isEmpty()) {
                    item { EmptyStateView("No orders yet", modifier = Modifier.height(100.dp)) }
                } else {
                    items(uiState.recentOrders, key = { it.orderId }) { order ->
                        OrderCard(order = order, onClick = { onOrderClick(order.orderId) },
                            showCustomerName = true)
                    }
                }
            }
        }
    }
}

// ===== HELPER COMPOSABLES =====

@Composable
private fun TodayMiniStat(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
    }
}

@Composable
private fun GradientStatCard(
    title: String, value: String, icon: ImageVector,
    color1: Color, color2: Color, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(color1, color2)),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Icon(icon, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(22.dp))
                Spacer(Modifier.height(8.dp))
                Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Text(title, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String, icon: ImageVector, color: Color,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 13.sp)
    }
}

