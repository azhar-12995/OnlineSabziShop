package com.azhar.sabzishop.domain.usecase.product

import com.azhar.sabzishop.domain.model.Product
import com.azhar.sabzishop.domain.repository.ProductRepository
import com.azhar.sabzishop.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(private val repo: ProductRepository) {
    operator fun invoke(): Flow<Resource<List<Product>>> = repo.getProducts()
}

class GetProductByIdUseCase @Inject constructor(private val repo: ProductRepository) {
    suspend operator fun invoke(id: String): Resource<Product> = repo.getProductById(id)
}

class AddProductUseCase @Inject constructor(private val repo: ProductRepository) {
    suspend operator fun invoke(product: Product): Resource<Unit> = repo.addProduct(product)
}

class UpdateProductUseCase @Inject constructor(private val repo: ProductRepository) {
    suspend operator fun invoke(product: Product): Resource<Unit> = repo.updateProduct(product)
}

class DeleteProductUseCase @Inject constructor(private val repo: ProductRepository) {
    suspend operator fun invoke(productId: String): Resource<Unit> = repo.deleteProduct(productId)
}

class ToggleProductAvailabilityUseCase @Inject constructor(private val repo: ProductRepository) {
    suspend operator fun invoke(productId: String, isAvailable: Boolean): Resource<Unit> =
        repo.toggleAvailability(productId, isAvailable)
}

