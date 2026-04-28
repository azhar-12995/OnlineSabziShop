package com.azhar.sabzishop.presentation.user.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.sabzishop.domain.model.Product
import com.azhar.sabzishop.domain.usecase.auth.GetCurrentUserUseCase
import com.azhar.sabzishop.domain.usecase.cart.GetCartUseCase
import com.azhar.sabzishop.domain.usecase.product.GetProductsUseCase
import com.azhar.sabzishop.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val cartItemCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getCartUseCase: GetCartUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        observeCartCount()
    }

    private fun observeCartCount() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                user?.uid?.let { uid ->
                    getCartUseCase(uid).collect { resource ->
                        if (resource is Resource.Success) {
                            _uiState.value = _uiState.value.copy(cartItemCount = resource.data.size)
                        }
                    }
                }
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            getProductsUseCase().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Resource.Success -> {
                        val products = resource.data.filter { it.isAvailable }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false, products = products,
                            filteredProducts = applyFilter(products, _uiState.value.selectedCategory, _uiState.value.searchQuery)
                        )
                    }
                    is Resource.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false, errorMessage = resource.message)
                }
            }
        }
    }

    fun onCategorySelect(category: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            filteredProducts = applyFilter(_uiState.value.products, category, _uiState.value.searchQuery)
        )
    }

    fun onSearchChange(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredProducts = applyFilter(_uiState.value.products, _uiState.value.selectedCategory, query)
        )
    }

    private fun applyFilter(products: List<Product>, category: String, query: String): List<Product> {
        return products
            .filter { if (category == "All") true else it.category.equals(category, ignoreCase = true) }
            .filter { if (query.isBlank()) true else it.name.contains(query, ignoreCase = true) }
    }
}
