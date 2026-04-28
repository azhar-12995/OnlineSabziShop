package com.azhar.sabzishop.domain.repository

import com.azhar.sabzishop.domain.model.Order
import com.azhar.sabzishop.utils.Resource
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun placeOrder(order: Order): Resource<String> // returns orderId
    fun getUserOrders(userId: String): Flow<Resource<List<Order>>>
    fun getAllOrders(): Flow<Resource<List<Order>>>
    suspend fun updateOrderStatus(orderId: String, status: String): Resource<Unit>
}

