package com.azhar.sabzishop.presentation.admin.revenue

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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

private val VGreen = Color(0xFF2E7D32)
private val VOrange = Color(0xFFE65100)
private val VBlue = Color(0xFF1565C0)
private val VTeal = Color(0xFF00796B)
private val VGold = Color(0xFFFF8F00)
private val VRed = Color(0xFFD32F2F)

@Composable
fun RevenueScreen(
    onBackClick: () -> Unit,
    viewModel: RevenueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SabziTopBar(title = "Revenue & Profit", canNavigateBack = true, onNavigateBack = onBackClick)
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingView()
            uiState.errorMessage != null -> ErrorView(uiState.errorMessage!!)
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Revenue + Profit top banner
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
                                .padding(24.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()) {
                                Text("Total Revenue", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                                Text(uiState.totalRevenue.toRupees(), color = Color.White,
                                    fontWeight = FontWeight.ExtraBold, fontSize = 32.sp)
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                                Spacer(Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Profit (5%)", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                        Text(uiState.totalProfit.toRupees(), color = Color(0xFFFFD54F),
                                            fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Delivered", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                        Text(uiState.deliveredRevenue.toRupees(), color = Color(0xFFA5D6A7),
                                            fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Stat cards
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RevenueStatCard("Delivered Revenue", uiState.deliveredRevenue.toRupees(),
                            Icons.Default.CheckCircle, VGreen, Color(0xFF66BB6A), Modifier.weight(1f))
                        RevenueStatCard("Cancelled", uiState.cancelledRevenue.toRupees(),
                            Icons.Default.Cancel, VRed, Color(0xFFEF5350), Modifier.weight(1f))
                    }
                }

                // Profit explanation
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = VGold, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Profit = 5% of each delivered order's total",
                                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Cancelled orders do not contribute to profit.",
                                    fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                // Daily Breakdown
                if (uiState.dailyBreakdown.isNotEmpty()) {
                    item {
                        Text("Daily Breakdown", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                    }

                    // Revenue bar chart
                    item {
                        val maxRev = uiState.dailyBreakdown.maxOfOrNull { it.revenue } ?: 1.0
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).height(160.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    uiState.dailyBreakdown.forEach { day ->
                                        val fraction = (day.revenue / maxRev).toFloat().coerceAtLeast(0.03f)
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Bottom,
                                            modifier = Modifier.width(50.dp).fillMaxHeight()
                                        ) {
                                            Text(String.format("%.0f", day.revenue / 1000) + "k",
                                                fontSize = 8.sp, fontWeight = FontWeight.Bold, color = VGreen)
                                            Spacer(Modifier.height(2.dp))
                                            Canvas(modifier = Modifier.width(36.dp).fillMaxHeight(fraction)) {
                                                drawRect(color = Color(0xFF43A047), size = size)
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(day.date, fontSize = 8.sp, maxLines = 1)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("Daily Revenue", fontSize = 10.sp, color = Color.Gray,
                                    modifier = Modifier.align(Alignment.CenterHorizontally))
                            }
                        }
                    }

                    // Table header
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text("Date", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f))
                                Text("Orders", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.7f))
                                Text("Revenue", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f))
                                Text("Profit", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    items(uiState.dailyBreakdown) { day ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(day.date, fontSize = 12.sp, modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Medium)
                                Text("${day.orders}", fontSize = 12.sp, modifier = Modifier.weight(0.7f),
                                    fontWeight = FontWeight.Bold, color = VBlue)
                                Text(day.revenue.toRupees(), fontSize = 12.sp, modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold, color = VGreen)
                                Text(day.profit.toRupees(), fontSize = 12.sp, modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold, color = VGold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RevenueStatCard(
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
                Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text(title, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
            }
        }
    }
}

