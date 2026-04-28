package com.azhar.sabzishop.domain.usecase.cart

import com.azhar.sabzishop.domain.model.CartItem
import com.azhar.sabzishop.domain.repository.CartRepository
import com.azhar.sabzishop.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCartUseCase @Inject constructor(private val repo: CartRepository) {
    operator fun invoke(userId: String): Flow<Resource<List<CartItem>>> = repo.getCartItems(userId)
}

class AddToCartUseCase @Inject constructor(private val repo: CartRepository) {
    suspend operator fun invoke(userId: String, cartItem: CartItem): Resource<Unit> =
        repo.addToCart(userId, cartItem)
}

class UpdateCartItemUseCase @Inject constructor(private val repo: CartRepository) {
    suspend operator fun invoke(userId: String, itemId: String, qty: Double): Resource<Unit> =
        repo.updateCartItemQty(userId, itemId, qty)
}

class RemoveFromCartUseCase @Inject constructor(private val repo: CartRepository) {
    suspend operator fun invoke(userId: String, itemId: String): Resource<Unit> =
        repo.removeFromCart(userId, itemId)
}

class ClearCartUseCase @Inject constructor(private val repo: CartRepository) {
    suspend operator fun invoke(userId: String): Resource<Unit> = repo.clearCart(userId)
}

