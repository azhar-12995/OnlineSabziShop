package com.azhar.sabzishop.presentation.user.productdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.sabzishop.domain.model.CartItem
import com.azhar.sabzishop.domain.model.Product
import com.azhar.sabzishop.domain.usecase.auth.GetCurrentUserUseCase
import com.azhar.sabzishop.domain.usecase.cart.AddToCartUseCase
import com.azhar.sabzishop.domain.usecase.cart.GetCartUseCase
import com.azhar.sabzishop.domain.usecase.product.GetProductByIdUseCase
import com.azhar.sabzishop.domain.usecase.product.GetProductsUseCase
import com.azhar.sabzishop.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Represents a quantity preset chip (e.g. 50g, 1kg).
 * [valueKg] is always in kg (e.g. 0.05 for 50g, 1.0 for 1kg).
 */
data class QtyPreset(val label: String, val valueKg: Double)

val QUANTITY_PRESETS = listOf(
    QtyPreset("50g", 0.05),
    QtyPreset("100g", 0.1),
    QtyPreset("200g", 0.2),
    QtyPreset("500g", 0.5),
    QtyPreset("1 kg", 1.0),
    QtyPreset("2 kg", 2.0),
    QtyPreset("5 kg", 5.0),
    QtyPreset("10 kg", 10.0),
)

data class ProductDetailsUiState(
    val isLoading: Boolean = false,
    val product: Product? = null,
    val selectedQtyKg: Double = 1.0,
    val relatedProducts: List<Product> = emptyList(),
    val isLoadingRelated: Boolean = false,
    val isAddingToCart: Boolean = false,
    val cartItemCount: Int = 0,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
    val requiresLogin: Boolean = false
)

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val getProductsUseCase: GetProductsUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailsUiState())
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()
    private var currentUserId: String? = null
    private val productId: String = savedStateHandle.get<String>("productId") ?: ""

    init {
        loadProduct(productId)
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                currentUserId = user?.uid
                user?.uid?.let { observeCartCount(it) }
            }
        }
    }

    private fun observeCartCount(uid: String) {
        viewModelScope.launch {
            getCartUseCase(uid).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.value = _uiState.value.copy(cartItemCount = resource.data.size)
                }
            }
        }
    }

    private fun loadProduct(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val r = getProductByIdUseCase(id)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, product = r.data)
                    loadRelatedProducts(r.data)
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = r.message)
                else -> {}
            }
        }
    }

    private fun loadRelatedProducts(currentProduct: Product) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingRelated = true)
            getProductsUseCase().collect { resource ->
                if (resource is Resource.Success) {
                    val related = resource.data
                        .filter { it.id != currentProduct.id && it.category == currentProduct.category && it.isAvailable }
                        .take(10)
                    _uiState.value = _uiState.value.copy(relatedProducts = related, isLoadingRelated = false)
                } else if (resource is Resource.Error) {
                    _uiState.value = _uiState.value.copy(isLoadingRelated = false)
                }
            }
        }
    }

    fun selectQtyPreset(preset: QtyPreset) {
        _uiState.value = _uiState.value.copy(selectedQtyKg = preset.valueKg)
    }

    fun setCustomQty(kg: Double) {
        _uiState.value = _uiState.value.copy(selectedQtyKg = kg)
    }

    fun addToCart(): Boolean {
        val uid = currentUserId
        if (uid == null) {
            _uiState.value = _uiState.value.copy(requiresLogin = true)
            return false
        }
        val product = _uiState.value.product ?: return false
        val qtyKg = _uiState.value.selectedQtyKg
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAddingToCart = true)
            val cartItem = CartItem(
                itemId = product.id, productId = product.id, name = product.name,
                pricePerKg = product.pricePerKg, qty = qtyKg,
                imageBase64 = product.imageBase64,
                lineTotal = product.pricePerKg * qtyKg
            )
            val result = addToCartUseCase(uid, cartItem)
            val msg = if (result is Resource.Success) "Added to cart!" else "Failed to add to cart"
            _uiState.value = _uiState.value.copy(snackbarMessage = msg, requiresLogin = false, isAddingToCart = false)
        }
        return true
    }

    fun clearSnackbar() { _uiState.value = _uiState.value.copy(snackbarMessage = null) }
    fun clearLoginPrompt() { _uiState.value = _uiState.value.copy(requiresLogin = false) }
}
