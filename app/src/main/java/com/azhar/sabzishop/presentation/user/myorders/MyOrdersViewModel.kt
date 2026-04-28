package com.azhar.sabzishop.presentation.user.myorders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.sabzishop.domain.model.Order
import com.azhar.sabzishop.domain.usecase.auth.GetCurrentUserUseCase
import com.azhar.sabzishop.domain.usecase.order.GetMyOrdersUseCase
import com.azhar.sabzishop.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

enum class CustomerDateFilter { ALL, TODAY, YESTERDAY }

data class MyOrdersUiState(
    val isLoading: Boolean = false,
    val allOrders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val selectedTab: Int = 0, // 0=All, 1=Pending, 2=Delivered
    val selectedDateFilter: CustomerDateFilter = CustomerDateFilter.ALL,
    val errorMessage: String? = null
)

@HiltViewModel
class MyOrdersViewModel @Inject constructor(
    private val getMyOrdersUseCase: GetMyOrdersUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyOrdersUiState())
    val uiState: StateFlow<MyOrdersUiState> = _uiState.asStateFlow()

    companion object {
        val TABS = listOf("All", "Pending", "Delivered")
    }

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                if (user != null) loadOrders(user.uid)
            }
        }
    }

    private fun loadOrders(userId: String) {
        viewModelScope.launch {
            getMyOrdersUseCase(userId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, allOrders = resource.data)
                        refilter()
                    }
                    is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = resource.message)
                }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
        refilter()
    }

    fun selectDateFilter(filter: CustomerDateFilter) {
        _uiState.value = _uiState.value.copy(selectedDateFilter = filter)
        refilter()
    }

    private fun refilter() {
        val state = _uiState.value
        var orders = state.allOrders

        // Date filter
        orders = when (state.selectedDateFilter) {
            CustomerDateFilter.TODAY -> {
                val start = todayStartMillis()
                orders.filter { (it.createdAt?.toDate()?.time ?: 0L) >= start }
            }
            CustomerDateFilter.YESTERDAY -> {
                val todayStart = todayStartMillis()
                val yesterdayStart = todayStart - 24L * 60 * 60 * 1000
                orders.filter {
                    val ms = it.createdAt?.toDate()?.time ?: 0L
                    ms in yesterdayStart until todayStart
                }
            }
            CustomerDateFilter.ALL -> orders
        }

        // Status filter
        orders = when (state.selectedTab) {
            1 -> orders.filter { it.status == "Pending" || it.status == "Confirmed" }
            2 -> orders.filter { it.status == "Delivered" }
            else -> orders
        }

        // Latest first
        orders = orders.sortedByDescending { it.createdAt?.seconds ?: 0L }

        _uiState.value = state.copy(filteredOrders = orders)
    }

    private fun todayStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
