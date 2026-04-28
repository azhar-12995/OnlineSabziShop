package com.azhar.sabzishop.presentation.user.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.sabzishop.domain.model.Order
import com.azhar.sabzishop.domain.model.OrderItem
import com.azhar.sabzishop.domain.model.User
import com.azhar.sabzishop.domain.usecase.auth.GetCurrentUserUseCase
import com.azhar.sabzishop.domain.usecase.auth.UpdateUserProfileUseCase
import com.azhar.sabzishop.domain.usecase.cart.ClearCartUseCase
import com.azhar.sabzishop.domain.usecase.cart.GetCartUseCase
import com.azhar.sabzishop.domain.usecase.order.PlaceOrderUseCase
import com.azhar.sabzishop.utils.Constants
import com.azhar.sabzishop.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val deliveryAddress: String = "",
    val street: String = "",
    val houseNumber: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val isLoadingUser: Boolean = true,
    val errorMessage: String? = null,
    val placedOrderId: String? = null
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()
    private var currentUser: User? = null

    init {
        // Pre-fill address and phone from user profile
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                currentUser = user
                if (user != null && _uiState.value.isLoadingUser) {
                    val fullAddress = buildString {
                        if (user.houseNumber.isNotBlank()) append("House ${user.houseNumber}, ")
                        if (user.street.isNotBlank()) append("${user.street}, ")
                        append(user.address)
                    }
                    _uiState.value = _uiState.value.copy(
                        deliveryAddress = user.address,
                        street = user.street,
                        houseNumber = user.houseNumber,
                        phone = user.phone,
                        isLoadingUser = false
                    )
                } else if (user == null) {
                    _uiState.value = _uiState.value.copy(isLoadingUser = false)
                }
            }
        }
    }

    fun onAddressChange(v: String) { _uiState.value = _uiState.value.copy(deliveryAddress = v) }
    fun onStreetChange(v: String) { _uiState.value = _uiState.value.copy(street = v) }
    fun onHouseNumberChange(v: String) { _uiState.value = _uiState.value.copy(houseNumber = v) }
    fun onPhoneChange(v: String) { _uiState.value = _uiState.value.copy(phone = v) }

    fun placeOrder() {
        val user = currentUser ?: return
        val s = _uiState.value
        val address = s.deliveryAddress.trim()
        val street = s.street.trim()
        val houseNumber = s.houseNumber.trim()
        val phone = s.phone.trim()
        if (address.isBlank()) { _uiState.value = s.copy(errorMessage = "Delivery address is required"); return }
        if (phone.isBlank()) { _uiState.value = s.copy(errorMessage = "Phone number is required"); return }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Update address in user profile if changed
            val updatedUser = user.copy(
                phone = phone, address = address, street = street, houseNumber = houseNumber
            )
            updateUserProfileUseCase(updatedUser)

            val cartResource = getCartUseCase(user.uid).first { it !is Resource.Loading }
            val cartItems = (cartResource as? Resource.Success)?.data ?: emptyList()
            if (cartItems.isEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Cart is empty")
                return@launch
            }
            val orderItems = cartItems.map {
                OrderItem(it.productId, it.name, it.pricePerKg, it.qty, it.imageBase64, it.lineTotal)
            }
            val subtotal = cartItems.sumOf { it.lineTotal }
            // Build full delivery address string for order
            val fullDeliveryAddress = buildString {
                if (houseNumber.isNotBlank()) append("House $houseNumber, ")
                if (street.isNotBlank()) append("$street, ")
                append(address)
            }
            val order = Order(
                userId = user.uid, customerName = user.fullName,
                customerPhone = phone, deliveryAddress = fullDeliveryAddress,
                items = orderItems, subtotal = subtotal,
                deliveryCharges = Constants.DELIVERY_CHARGE,
                totalAmount = subtotal + Constants.DELIVERY_CHARGE,
                status = Constants.STATUS_PENDING
            )
            when (val result = placeOrderUseCase(order)) {
                is Resource.Success -> {
                    clearCartUseCase(user.uid)
                    _uiState.value = _uiState.value.copy(isLoading = false, placedOrderId = result.data)
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                else -> {}
            }
        }
    }
}
