package com.azhar.sabzishop.data.repository

import com.azhar.sabzishop.data.datasource.CartDataSource
import com.azhar.sabzishop.domain.model.CartItem
import com.azhar.sabzishop.domain.repository.CartRepository
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val dataSource: CartDataSource
) : CartRepository {
    override fun getCartItems(userId: String) = dataSource.getCartItems(userId)
    override suspend fun addToCart(userId: String, cartItem: CartItem) = dataSource.addToCart(userId, cartItem)
    override suspend fun updateCartItemQty(userId: String, itemId: String, qty: Double) = dataSource.updateCartItemQty(userId, itemId, qty)
    override suspend fun removeFromCart(userId: String, itemId: String) = dataSource.removeFromCart(userId, itemId)
    override suspend fun clearCart(userId: String) = dataSource.clearCart(userId)
}

