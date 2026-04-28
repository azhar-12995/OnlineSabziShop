package com.azhar.sabzishop.domain.model

import com.google.firebase.Timestamp

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val deliveryAddress: String = "",
    val items: List<OrderItem> = emptyList(),
    val subtotal: Double = 0.0,
    val deliveryCharges: Double = 20.0,
    val totalAmount: Double = 0.0,
    val status: String = "Pending",
    val createdAt: Timestamp? = null
)

