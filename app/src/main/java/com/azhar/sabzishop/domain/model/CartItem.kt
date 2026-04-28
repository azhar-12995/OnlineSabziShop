package com.azhar.sabzishop.domain.model

data class CartItem(
    val itemId: String = "",
    val productId: String = "",
    val name: String = "",
    val pricePerKg: Double = 0.0,
    val qty: Double = 1.0,
    val imageBase64: String = "",
    val lineTotal: Double = pricePerKg * qty
)

