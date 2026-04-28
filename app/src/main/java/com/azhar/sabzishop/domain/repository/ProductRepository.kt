package com.azhar.sabzishop.domain.repository

import com.azhar.sabzishop.domain.model.Product
import com.azhar.sabzishop.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(): Flow<Resource<List<Product>>>
    suspend fun getProductById(productId: String): Resource<Product>
    suspend fun addProduct(product: Product): Resource<Unit>
    suspend fun updateProduct(product: Product): Resource<Unit>
    suspend fun deleteProduct(productId: String): Resource<Unit>
    suspend fun toggleAvailability(productId: String, isAvailable: Boolean): Resource<Unit>
}

