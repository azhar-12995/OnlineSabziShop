package com.azhar.sabzishop.presentation.user.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.sabzishop.domain.model.CartItem
import com.azhar.sabzishop.domain.usecase.auth.GetCurrentUserUseCase
import com.azhar.sabzishop.domain.usecase.cart.GetCartUseCase
import com.azhar.sabzishop.domain.usecase.cart.RemoveFromCartUseCase
import com.azhar.sabzishop.domain.usecase.cart.UpdateCartItemUseCase
import com.azhar.sabzishop.utils.Constants
import com.azhar.sabzishop.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val isLoading: Boolean = false,
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val deliveryCharge: Double = Constants.DELIVERY_CHARGE,
    val total: Double = 0.0,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val userId: String? = null
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val updateCartItemUseCase: UpdateCartItemUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                val uid = user?.uid
                _uiState.value = _uiState.value.copy(isLoggedIn = uid != null, userId = uid)
                if (uid != null) loadCart(uid)
                else _uiState.value = _uiState.value.copy(isLoading = false, items = emptyList())
            }
        }
    }

    private fun loadCart(userId: String) {
        viewModelScope.launch {
            getCartUseCase(userId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Resource.Success -> {
                        val items = resource.data
                        val subtotal = items.sumOf { it.lineTotal }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false, items = items, subtotal = subtotal,
                            total = subtotal + Constants.DELIVERY_CHARGE
                        )
                    }
                    is Resource.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false, errorMessage = resource.message)
                }
            }
        }
    }

    fun increaseQty(item: CartItem) {
        val uid = _uiState.value.userId ?: return
        viewModelScope.launch {
            updateCartItemUseCase(uid, item.itemId, item.qty + 0.5)
        }
    }

    fun decreaseQty(item: CartItem) {
        val uid = _uiState.value.userId ?: return
        if (item.qty <= 0.1) { removeItem(item); return }
        val newQty = (item.qty - 0.5).coerceAtLeast(0.05)
        viewModelScope.launch {
            updateCartItemUseCase(uid, item.itemId, newQty)
        }
    }

    fun removeItem(item: CartItem) {
        val uid = _uiState.value.userId ?: return
        viewModelScope.launch {
            removeFromCartUseCase(uid, item.itemId)
        }
    }
}
