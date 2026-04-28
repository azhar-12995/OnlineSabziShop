package com.azhar.sabzishop.domain.model

import com.google.firebase.Timestamp

data class Product(
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

