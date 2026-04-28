package com.azhar.sabzishop.domain.usecase.order

import com.azhar.sabzishop.domain.model.Order
import com.azhar.sabzishop.domain.repository.OrderRepository
import com.azhar.sabzishop.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlaceOrderUseCase @Inject constructor(private val repo: OrderRepository) {
    suspend operator fun invoke(order: Order): Resource<String> = repo.placeOrder(order)
}

class GetMyOrdersUseCase @Inject constructor(private val repo: OrderRepository) {
    operator fun invoke(userId: String): Flow<Resource<List<Order>>> = repo.getUserOrders(userId)
}

class GetAllOrdersUseCase @Inject constructor(private val repo: OrderRepository) {
    operator fun invoke(): Flow<Resource<List<Order>>> = repo.getAllOrders()
}

class UpdateOrderStatusUseCase @Inject constructor(private val repo: OrderRepository) {
    suspend operator fun invoke(orderId: String, status: String): Resource<Unit> =
        repo.updateOrderStatus(orderId, status)
}

