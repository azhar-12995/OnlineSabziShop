package com.azhar.sabzishop.data.datasource

import com.azhar.sabzishop.data.mapper.toMap
import com.azhar.sabzishop.data.mapper.toProduct
import com.azhar.sabzishop.domain.model.Product
import com.azhar.sabzishop.utils.Constants
import com.azhar.sabzishop.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection(Constants.COLLECTION_PRODUCTS)

    fun getProducts(): Flow<Resource<List<Product>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load products"))
                    return@addSnapshotListener
                }
                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.toProduct(doc.id)
                } ?: emptyList()
                trySend(Resource.Success(products))
            }
        awaitClose { listener.remove() }
    }

    suspend fun getProductById(productId: String): Resource<Product> {
        return try {
            val doc = collection.document(productId).get().await()
            val product = doc.data?.toProduct(doc.id)
                ?: return Resource.Error("Product not found")
            Resource.Success(product)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load product")
        }
    }

    suspend fun addProduct(product: Product): Resource<Unit> {
        return try {
            val ref = collection.document()
            val productWithId = product.copy(id = ref.id, createdAt = Timestamp.now(), updatedAt = Timestamp.now())
            ref.set(productWithId.toMap()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add product")
        }
    }

    suspend fun updateProduct(product: Product): Resource<Unit> {
        return try {
            val updated = product.copy(updatedAt = Timestamp.now())
            collection.document(product.id).set(updated.toMap()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update product")
        }
    }

    suspend fun deleteProduct(productId: String): Resource<Unit> {
        return try {
            collection.document(productId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete product")
        }
    }

    suspend fun toggleAvailability(productId: String, isAvailable: Boolean): Resource<Unit> {
        return try {
            collection.document(productId).update("isAvailable", isAvailable).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update availability")
        }
    }
}

