package com.azhar.sabzishop.presentation.admin.revenue

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
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class DailyRevenue(
    val date: String,
    val orders: Int,
    val revenue: Double,
    val profit: Double // 5% of delivered
)

data class RevenueUiState(
    val isLoading: Boolean = true,
    val allOrders: List<Order> = emptyList(),
    val totalRevenue: Double = 0.0,
    val totalProfit: Double = 0.0,
    val deliveredRevenue: Double = 0.0,
    val cancelledRevenue: Double = 0.0,
    val dailyBreakdown: List<DailyRevenue> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class RevenueViewModel @Inject constructor(
    private val getAllOrdersUseCase: GetAllOrdersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RevenueUiState())
    val uiState: StateFlow<RevenueUiState> = _uiState.asStateFlow()

    companion object {
        const val PROFIT_PERCENT = 0.05
    }

    init {
        viewModelScope.launch {
            getAllOrdersUseCase().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Resource.Success -> {
                        val orders = resource.data
                        val delivered = orders.filter { it.status == "Delivered" }
                        val cancelled = orders.filter { it.status == "Cancelled" }
                        val active = orders.filter { it.status != "Cancelled" }

                        val totalRevenue = active.sumOf { it.totalAmount }
                        val deliveredRevenue = delivered.sumOf { it.totalAmount }
                        val cancelledRevenue = cancelled.sumOf { it.totalAmount }
                        val totalProfit = deliveredRevenue * PROFIT_PERCENT

                        // Daily breakdown (last 30 days)
                        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                        val dailyMap = mutableMapOf<String, DailyAccumulator>()
                        for (order in orders) {
                            val dateKey = order.createdAt?.toDate()?.let { sdf.format(it) } ?: "Unknown"
                            val acc = dailyMap.getOrPut(dateKey) { DailyAccumulator() }
                            acc.orders++
                            if (order.status != "Cancelled") {
                                acc.revenue += order.totalAmount
                            }
                            if (order.status == "Delivered") {
                                acc.profit += order.totalAmount * PROFIT_PERCENT
                            }
                        }
                        val daily = dailyMap.entries.map {
                            DailyRevenue(it.key, it.value.orders, it.value.revenue, it.value.profit)
                        }.take(30)

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            allOrders = orders,
                            totalRevenue = totalRevenue,
                            totalProfit = totalProfit,
                            deliveredRevenue = deliveredRevenue,
                            cancelledRevenue = cancelledRevenue,
                            dailyBreakdown = daily
                        )
                    }
                    is Resource.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false, errorMessage = resource.message
                    )
                }
            }
        }
    }

    private class DailyAccumulator {
        var orders = 0
        var revenue = 0.0
        var profit = 0.0
    }
}

