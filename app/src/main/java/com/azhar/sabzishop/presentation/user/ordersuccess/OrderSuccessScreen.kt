package com.azhar.sabzishop.presentation.user.ordersuccess

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azhar.sabzishop.presentation.components.SabziButton

@Composable
fun OrderSuccessScreen(
    orderId: String,
    onContinueShopping: () -> Unit,
    onViewOrders: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle, null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(100.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text("Order Placed Successfully!", fontSize = 24.sp, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Thank you for shopping with us.\nYour fresh vegetables are on the way.",
            style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Order ID", style = MaterialTheme.typography.labelSmall)
                Text("#${orderId.take(12)}", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(32.dp))
        SabziButton("Continue Shopping", onClick = onContinueShopping)
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onViewOrders) { Text("View My Orders") }
    }
}

