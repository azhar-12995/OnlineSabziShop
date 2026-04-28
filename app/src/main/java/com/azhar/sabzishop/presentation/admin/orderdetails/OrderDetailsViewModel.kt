package com.azhar.sabzishop.presentation.admin.orderdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.sabzishop.domain.model.Order
import com.azhar.sabzishop.domain.usecase.order.GetAllOrdersUseCase
import com.azhar.sabzishop.domain.usecase.order.UpdateOrderStatusUseCase
import com.azhar.sabzishop.utils.Constants
import com.azhar.sabzishop.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderDetailsUiState(
    val order: Order? = null,
    val isLoading: Boolean = false,
    val isUpdatingStatus: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null
)

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    private val getAllOrdersUseCase: GetAllOrdersUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderDetailsUiState())
    val uiState: StateFlow<OrderDetailsUiState> = _uiState.asStateFlow()

    private val orderId = savedStateHandle.get<String>("orderId") ?: ""
    val isAdmin = savedStateHandle.get<Boolean>("isAdmin") ?: false

    init {
        loadOrder()
    }

    private fun loadOrder() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val resource = getAllOrdersUseCase().first { it !is Resource.Loading }
            val order = (resource as? Resource.Success)?.data?.find { it.orderId == orderId }
            _uiState.value = _uiState.value.copy(isLoading = false, order = order,
                errorMessage = if (order == null) "Order not found" else null)
        }
    }

    fun updateStatus(status: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdatingStatus = true)
            when (val r = updateOrderStatusUseCase(orderId, status)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdatingStatus = false,
                        order = _uiState.value.order?.copy(status = status),
                        snackbarMessage = "Status updated to $status")
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isUpdatingStatus = false, snackbarMessage = r.message)
                else -> {}
            }
        }
    }

    fun clearSnackbar() { _uiState.value = _uiState.value.copy(snackbarMessage = null) }

    val orderStatuses = if (isAdmin) {
        listOf(Constants.STATUS_PENDING, Constants.STATUS_CONFIRMED,
            Constants.STATUS_DELIVERED, Constants.STATUS_CANCELLED)
    } else {
        listOf(Constants.STATUS_CANCELLED)
    }
}
