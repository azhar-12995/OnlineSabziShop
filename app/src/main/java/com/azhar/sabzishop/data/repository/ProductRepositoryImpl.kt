package com.azhar.sabzishop.data.repository

import com.azhar.sabzishop.data.datasource.ProductDataSource
import com.azhar.sabzishop.domain.model.Product
import com.azhar.sabzishop.domain.repository.ProductRepository
import com.azhar.sabzishop.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val dataSource: ProductDataSource
) : ProductRepository {
    override fun getProducts() = dataSource.getProducts()
    override suspend fun getProductById(productId: String) = dataSource.getProductById(productId)
    override suspend fun addProduct(product: Product) = dataSource.addProduct(product)
    override suspend fun updateProduct(product: Product) = dataSource.updateProduct(product)
    override suspend fun deleteProduct(productId: String) = dataSource.deleteProduct(productId)
    override suspend fun toggleAvailability(productId: String, isAvailable: Boolean) =
        dataSource.toggleAvailability(productId, isAvailable)
}

