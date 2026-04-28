package com.azhar.sabzishop.data.repository

import com.azhar.sabzishop.data.datasource.OrderDataSource
import com.azhar.sabzishop.domain.model.Order
import com.azhar.sabzishop.domain.repository.OrderRepository
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val dataSource: OrderDataSource
) : OrderRepository {
    override suspend fun placeOrder(order: Order) = dataSource.placeOrder(order)
    override fun getUserOrders(userId: String) = dataSource.getUserOrders(userId)
    override fun getAllOrders() = dataSource.getAllOrders()
    override suspend fun updateOrderStatus(orderId: String, status: String) =
        dataSource.updateOrderStatus(orderId, status)
}

