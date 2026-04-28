package com.azhar.sabzishop.presentation.admin.manageproducts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.sabzishop.domain.model.Product
import com.azhar.sabzishop.domain.usecase.product.DeleteProductUseCase
import com.azhar.sabzishop.domain.usecase.product.GetProductsUseCase
import com.azhar.sabzishop.domain.usecase.product.ToggleProductAvailabilityUseCase
import com.azhar.sabzishop.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageProductsUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val snackbarMessage: String? = null
)

@HiltViewModel
class ManageProductsViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val toggleAvailabilityUseCase: ToggleProductAvailabilityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageProductsUiState())
    val uiState: StateFlow<ManageProductsUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            getProductsUseCase().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Resource.Success -> _uiState.value = _uiState.value.copy(
                        isLoading = false, products = resource.data,
                        filteredProducts = applyFilter(resource.data, _uiState.value.searchQuery))
                    is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = resource.message)
                }
            }
        }
    }

    fun onSearch(q: String) {
        _uiState.value = _uiState.value.copy(searchQuery = q,
            filteredProducts = applyFilter(_uiState.value.products, q))
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            deleteProductUseCase(productId)
            _uiState.value = _uiState.value.copy(snackbarMessage = "Product deleted")
        }
    }

    fun toggleAvailability(productId: String, current: Boolean) {
        viewModelScope.launch { toggleAvailabilityUseCase(productId, !current) }
    }

    fun clearSnackbar() { _uiState.value = _uiState.value.copy(snackbarMessage = null) }

    private fun applyFilter(products: List<Product>, query: String) =
        if (query.isBlank()) products else products.filter { it.name.contains(query, ignoreCase = true) }
}

