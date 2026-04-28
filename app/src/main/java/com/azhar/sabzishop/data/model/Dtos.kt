package com.azhar.sabzishop.data.model

import com.google.firebase.Timestamp

/** Firestore DTO for users/{uid} */
data class UserDto(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val street: String = "",
    val houseNumber: String = "",
    val profileImageBase64: String = "",
    val role: String = "customer",
    val createdAt: Timestamp? = null
)

/** Firestore DTO for products/{productId} */
data class ProductDto(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val pricePerKg: Double = 0.0,
    val stockQty: Int = 0,
    val imageBase64: String = "",
    val isAvailable: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

/** Firestore DTO for users/{uid}/cart/items/{itemId} */
data class CartItemDto(
    val itemId: String = "",
    val productId: String = "",
    val name: String = "",
    val pricePerKg: Double = 0.0,
    val qty: Double = 1.0,
    val imageBase64: String = "",
    val lineTotal: Double = 0.0
)

/** Embedded in Order document */
data class OrderItemDto(
    val productId: String = "",
    val name: String = "",
    val pricePerKg: Double = 0.0,
    val qty: Double = 1.0,
    val imageBase64: String = "",
    val lineTotal: Double = 0.0
)

/** Firestore DTO for orders/{orderId} */
data class OrderDto(
    val orderId: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val deliveryAddress: String = "",
    val items: List<Map<String, Any>> = emptyList(),
    val subtotal: Double = 0.0,
    val deliveryCharges: Double = 20.0,
    val totalAmount: Double = 0.0,
    val status: String = "Pending",
    val createdAt: Timestamp? = null
)

