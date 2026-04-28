package com.azhar.sabzishop.presentation.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.sabzishop.domain.model.Order
import com.azhar.sabzishop.domain.model.Product
import com.azhar.sabzishop.domain.usecase.auth.LogoutUseCase
import com.azhar.sabzishop.domain.usecase.order.GetAllOrdersUseCase
import com.azhar.sabzishop.domain.usecase.product.GetProductsUseCase
import com.azhar.sabzishop.notification.NotificationManager as SabziNotificationManager
import com.azhar.sabzishop.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Per-item stock planning row.
 * Shows every product with today's sales + 7-day avg demand + current stock.
 */
data class StockPlanningItem(
    val productId: String,
    val productName: String,
    val imageBase64: String,
    val todayQtyKg: Double,       // how much was ordered today
    val todayOrders: Int,          // in how many orders today
    val todayRevenue: Double,
    val currentStock: Int,
    val avgDailyDemand: Double,    // 7-day average daily qty
    val daysOfStockLeft: Double,   // currentStock / avgDailyDemand (how many days stock will last)
    val totalOrderedQtyKg: Double = 0.0  // total qty ordered by all customers (all time)
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalOrders: Int = 0,
    val pendingOrders: Int = 0,
    val completedOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val totalProducts: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalProfit: Double = 0.0,
    // Today
    val todayOrders: Int = 0,
    val todayRevenue: Double = 0.0,
    val todayProfit: Double = 0.0,
    // Stock planning: ALL products with today's sales + demand info
    val stockPlanningItems: List<StockPlanningItem> = emptyList(),
    val recentOrders: List<Order> = emptyList(),
    val stockItems: List<Pair<String, Int>> = emptyList(),
    val loggedOut: Boolean = false
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val getAllOrdersUseCase: GetAllOrdersUseCase,
    private val getProductsUseCase: GetProductsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val sabziNotificationManager: SabziNotificationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Keep raw data to recompute when either orders or products update
    private var cachedOrders: List<Order> = emptyList()
    private var cachedProducts: List<Product> = emptyList()

    companion object {
        const val PROFIT_PERCENT = 0.05
    }

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            getAllOrdersUseCase().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Resource.Success -> {
                        cachedOrders = resource.data
                        recompute()
                    }
                    is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
        viewModelScope.launch {
            getProductsUseCase().collect { resource ->
                if (resource is Resource.Success) {
                    cachedProducts = resource.data
                    recompute()
                }
            }
        }
    }

    /**
     * Recompute all dashboard stats whenever orders OR products update.
     * This fixes the race condition — both data sources feed into one computation.
     */
    private fun recompute() {
        val orders = cachedOrders
        val products = cachedProducts

        val delivered = orders.filter { it.status == "Delivered" }
        val cancelled = orders.filter { it.status == "Cancelled" }
        val profit = delivered.sumOf { it.totalAmount * PROFIT_PERCENT }

        // === Today ===
        val todayStart = todayStartMillis()
        val todayOrders = orders.filter { (it.createdAt?.toDate()?.time ?: 0L) >= todayStart }
        val todayDelivered = todayOrders.filter { it.status == "Delivered" }

        // Today's per-item accumulation
        val todayItemMap = mutableMapOf<String, ItemAccumulator>()
        for (order in todayOrders) {
            for (item in order.items) {
                val key = item.productId.ifBlank { item.name }
                val acc = todayItemMap.getOrPut(key) { ItemAccumulator() }
                acc.totalQty += item.qty
                acc.totalRevenue += item.lineTotal
                acc.orderIds.add(order.orderId)
            }
        }

        // === All-time total ordered qty per product ===
        val totalOrderedMap = mutableMapOf<String, Double>()
        for (order in orders) {
            for (item in order.items) {
                val key = item.productId.ifBlank { item.name }
                totalOrderedMap[key] = (totalOrderedMap[key] ?: 0.0) + item.qty
            }
        }

        // === Last 7 days demand for avg/day ===
        val weekAgoMs = todayStart - 7L * 24 * 60 * 60 * 1000
        val weekDemandMap = mutableMapOf<String, Double>()
        for (order in orders.filter { (it.createdAt?.toDate()?.time ?: 0L) >= weekAgoMs }) {
            for (item in order.items) {
                val key = item.productId.ifBlank { item.name }
                weekDemandMap[key] = (weekDemandMap[key] ?: 0.0) + item.qty
            }
        }

        // === Build stock planning list: ALL products ===
        val stockPlanningItems = if (products.isNotEmpty()) {
            products.map { product ->
                val todayAcc = todayItemMap[product.id]
                val avgDaily = (weekDemandMap[product.id] ?: 0.0) / 7.0
                val daysLeft = if (avgDaily > 0) product.stockQty / avgDaily else Double.MAX_VALUE
                StockPlanningItem(
                    productId = product.id,
                    productName = product.name,
                    imageBase64 = product.imageBase64,
                    todayQtyKg = todayAcc?.totalQty ?: 0.0,
                    todayOrders = todayAcc?.orderIds?.size ?: 0,
                    todayRevenue = todayAcc?.totalRevenue ?: 0.0,
                    currentStock = product.stockQty,
                    avgDailyDemand = avgDaily,
                    daysOfStockLeft = if (daysLeft == Double.MAX_VALUE) -1.0 else daysLeft,
                    totalOrderedQtyKg = totalOrderedMap[product.id] ?: 0.0
                )
            }.sortedByDescending { it.todayQtyKg } // Items ordered today first, then rest
        } else {
            // Products haven't loaded yet — build from today's orders only
            todayItemMap.map { (key, acc) ->
                StockPlanningItem(
                    productId = key,
                    productName = acc.orderIds.firstOrNull() ?: key, // fallback
                    imageBase64 = "",
                    todayQtyKg = acc.totalQty,
                    todayOrders = acc.orderIds.size,
                    todayRevenue = acc.totalRevenue,
                    currentStock = 0,
                    avgDailyDemand = (weekDemandMap[key] ?: 0.0) / 7.0,
                    daysOfStockLeft = -1.0,
                    totalOrderedQtyKg = totalOrderedMap[key] ?: 0.0
                )
            }
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            totalOrders = orders.size,
            pendingOrders = orders.count { it.status == "Pending" || it.status == "Confirmed" },
            completedOrders = delivered.size,
            cancelledOrders = cancelled.size,
            totalRevenue = orders.filter { it.status != "Cancelled" }.sumOf { it.totalAmount },
            totalProfit = profit,
            todayOrders = todayOrders.size,
            todayRevenue = todayOrders.filter { it.status != "Cancelled" }.sumOf { it.totalAmount },
            todayProfit = todayDelivered.sumOf { it.totalAmount * PROFIT_PERCENT },
            stockPlanningItems = stockPlanningItems,
            recentOrders = orders.take(5),
            totalProducts = products.size,
            stockItems = products.map { it.name to it.stockQty }
        )
    }

    private fun todayStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun logout() {
        viewModelScope.launch {
            sabziNotificationManager.stop()
            logoutUseCase()
            _uiState.value = _uiState.value.copy(loggedOut = true)
        }
    }

    private class ItemAccumulator {
        var totalQty = 0.0
        var totalRevenue = 0.0
        val orderIds = mutableSetOf<String>()
    }
}
