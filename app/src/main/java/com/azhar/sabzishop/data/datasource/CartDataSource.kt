package com.azhar.sabzishop.data.datasource

import com.azhar.sabzishop.data.mapper.toCartItem
import com.azhar.sabzishop.data.mapper.toMap
import com.azhar.sabzishop.domain.model.CartItem
import com.azhar.sabzishop.utils.Constants
import com.azhar.sabzishop.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun cartRef(userId: String) =
        firestore.collection(Constants.COLLECTION_USERS).document(userId)
            .collection(Constants.COLLECTION_CART).document("items")
            .collection(Constants.COLLECTION_CART_ITEMS)

    fun getCartItems(userId: String): Flow<Resource<List<CartItem>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = cartRef(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Failed to load cart"))
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { doc ->
                doc.data?.toCartItem(doc.id)
            } ?: emptyList()
            trySend(Resource.Success(items))
        }
        awaitClose { listener.remove() }
    }

    suspend fun addToCart(userId: String, cartItem: CartItem): Resource<Unit> {
        return try {
            // Use productId as document ID so adding same product updates it
            val docId = if (cartItem.itemId.isNotEmpty()) cartItem.itemId else cartItem.productId
            cartRef(userId).document(docId).set(cartItem.copy(itemId = docId).toMap()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add to cart")
        }
    }

    suspend fun updateCartItemQty(userId: String, itemId: String, qty: Double): Resource<Unit> {
        return try {
            val lineTotal = cartRef(userId).document(itemId).get().await()
                .getDouble("pricePerKg")?.times(qty) ?: 0.0
            cartRef(userId).document(itemId).update(
                mapOf("qty" to qty, "lineTotal" to lineTotal)
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update cart")
        }
    }

    suspend fun removeFromCart(userId: String, itemId: String): Resource<Unit> {
        return try {
            cartRef(userId).document(itemId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to remove from cart")
        }
    }

    suspend fun clearCart(userId: String): Resource<Unit> {
        return try {
            val docs = cartRef(userId).get().await()
            val batch = firestore.batch()
            docs.forEach { batch.delete(it.reference) }
            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to clear cart")
        }
    }
}

