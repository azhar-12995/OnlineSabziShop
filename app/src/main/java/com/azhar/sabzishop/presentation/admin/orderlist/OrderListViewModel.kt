package com.azhar.sabzishop.presentation.admin.orderlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.sabzishop.domain.model.Order
import com.azhar.sabzishop.domain.usecase.order.GetAllOrdersUseCase
import com.azhar.sabzishop.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

enum class DateFilterOption { ALL, TODAY, YESTERDAY }

data class OrderListUiState(
    val isLoading: Boolean = false,
    val allOrders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val selectedStatusTab: Int = 0,
    val selectedDateFilter: DateFilterOption = DateFilterOption.ALL,
    val errorMessage: String? = null
)

@HiltViewModel
class OrderListViewModel @Inject constructor(
    private val getAllOrdersUseCase: GetAllOrdersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderListUiState())
    val uiState: StateFlow<OrderListUiState> = _uiState.asStateFlow()

    companion object {
        val STATUS_TABS = listOf("All", "Pending", "Confirmed", "Delivered", "Cancelled")
    }

    init {
        viewModelScope.launch {
            getAllOrdersUseCase().collect { resource ->
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

    fun selectStatusTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedStatusTab = index)
        refilter()
    }

    fun selectDateFilter(filter: DateFilterOption) {
        _uiState.value = _uiState.value.copy(selectedDateFilter = filter)
        refilter()
    }

    private fun refilter() {
        val state = _uiState.value
        var orders = state.allOrders

        // Date filter
        orders = when (state.selectedDateFilter) {
            DateFilterOption.TODAY -> {
                val start = todayStartMillis()
                orders.filter { (it.createdAt?.toDate()?.time ?: 0L) >= start }
            }
            DateFilterOption.YESTERDAY -> {
                val todayStart = todayStartMillis()
                val yesterdayStart = todayStart - 24L * 60 * 60 * 1000
                orders.filter {
                    val ms = it.createdAt?.toDate()?.time ?: 0L
                    ms in yesterdayStart until todayStart
                }
            }
            DateFilterOption.ALL -> orders
        }

        // Status filter
        val tab = STATUS_TABS.getOrNull(state.selectedStatusTab) ?: "All"
        orders = if (tab == "All") orders else orders.filter { it.status == tab }

        // Sort: latest first. For Delivered tab, latest delivered on top.
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
