package com.azhar.sabzishop.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azhar.sabzishop.domain.model.Order
import com.azhar.sabzishop.utils.toRupees
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showCustomerName: Boolean = false
) {
    val dateStr = order.createdAt?.toDate()?.let {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)
    } ?: ""
    val timeStr = order.createdAt?.toDate()?.let {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it)
    } ?: ""
    val itemsSummary = order.items.take(3).joinToString(" • ") {
        "${it.name} (${String.format("%.1f", it.qty)} kg)"
    } + if (order.items.size > 3) " +${order.items.size - 3} more" else ""

    val statusColor = when (order.status) {
        "Pending" -> Color(0xFFFF9800)
        "Confirmed" -> Color(0xFF2196F3)
        "Delivered" -> Color(0xFF2E7D32)
        "Cancelled" -> Color(0xFFD32F2F)
        else -> Color.Gray
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        onClick = onClick,
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top row: ID + Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#${order.orderId.take(8).uppercase()}",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                StatusBadge(status = order.status)
            }

            // Customer name (admin only)
            if (showCustomerName && order.customerName.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(order.customerName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Items summary
            Spacer(Modifier.height(6.dp))
            Text(itemsSummary, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2, overflow = TextOverflow.Ellipsis)

            Spacer(Modifier.height(10.dp))

            // Bottom row: date + time + item count + total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date & Time
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(13.dp),
                            tint = Color.Gray)
                        Spacer(Modifier.width(3.dp))
                        Text(dateStr, fontSize = 11.sp, color = Color.Gray)
                    }
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(13.dp),
                            tint = Color.Gray)
                        Spacer(Modifier.width(3.dp))
                        Text(timeStr, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }
                // Items count
                Text("${order.items.size} item(s)", fontSize = 12.sp, color = Color.Gray)
                // Total
                Text(order.totalAmount.toRupees(), fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val color = when (status) {
        "Pending" -> Color(0xFFFF9800)
        "Confirmed" -> Color(0xFF2196F3)
        "Delivered" -> Color(0xFF2E7D32)
        "Cancelled" -> Color(0xFFD32F2F)
        else -> Color.Gray
    }
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}
