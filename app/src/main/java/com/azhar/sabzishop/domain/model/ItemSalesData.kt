package com.azhar.sabzishop.domain.model

/**
 * Represents aggregated sales data for a single product item.
 * Used on the Admin Item-wise Sales Analytics screen.
 */
data class ItemSalesData(
    val productId: String = "",
    val productName: String = "",
    val imageBase64: String = "",
    val ordersCount: Int = 0,
    val totalQtySold: Double = 0.0, // in kg
    val totalRevenue: Double = 0.0,
    val currentStock: Int = 0
)

