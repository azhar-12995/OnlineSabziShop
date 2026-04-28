package com.azhar.sabzishop.domain.repository

import com.azhar.sabzishop.domain.model.CartItem
import com.azhar.sabzishop.utils.Resource
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItems(userId: String): Flow<Resource<List<CartItem>>>
    suspend fun addToCart(userId: String, cartItem: CartItem): Resource<Unit>
    suspend fun updateCartItemQty(userId: String, itemId: String, qty: Double): Resource<Unit>
    suspend fun removeFromCart(userId: String, itemId: String): Resource<Unit>
    suspend fun clearCart(userId: String): Resource<Unit>
}

