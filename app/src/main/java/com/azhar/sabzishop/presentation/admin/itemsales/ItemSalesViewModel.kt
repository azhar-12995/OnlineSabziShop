package com.azhar.sabzishop.presentation.admin.itemsales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.sabzishop.domain.model.ItemSalesData
import com.azhar.sabzishop.domain.model.Order
import com.azhar.sabzishop.domain.model.Product
import com.azhar.sabzishop.domain.usecase.order.GetAllOrdersUseCase
import com.azhar.sabzishop.domain.usecase.product.GetProductsUseCase
import com.azhar.sabzishop.utils.Resource
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

enum class DateFilter { TODAY, WEEKLY, MONTHLY, CUSTOM }

data class ItemSalesUiState(
    val isLoading: Boolean = true,
    val dateFilter: DateFilter = DateFilter.TODAY,
    val customStartMillis: Long? = null,
    val customEndMillis: Long? = null,
    val allOrders: List<Order> = emptyList(),
    val products: List<Product> = emptyList(),
    val itemSalesList: List<ItemSalesData> = emptyList(),
    val selectedItem: ItemSalesData? = null,
    val totalOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalProfit: Double = 0.0, // 5% of delivered orders
    val totalItemsSold: Double = 0.0,
    val lowStockItems: List<ItemSalesData> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class ItemSalesViewModel @Inject constructor(
    private val getAllOrdersUseCase: GetAllOrdersUseCase,
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemSalesUiState())
    val uiState: StateFlow<ItemSalesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            getAllOrdersUseCase().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(allOrders = resource.data)
                        recalculate()
                    }
                    is Resource.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false, errorMessage = resource.message
                    )
                }
            }
        }
        viewModelScope.launch {
            getProductsUseCase().collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.value = _uiState.value.copy(products = resource.data)
                    recalculate()
                }
            }
        }
    }

    fun setDateFilter(filter: DateFilter) {
        _uiState.value = _uiState.value.copy(dateFilter = filter)
        recalculate()
    }

    fun setCustomRange(startMillis: Long, endMillis: Long) {
        _uiState.value = _uiState.value.copy(
            dateFilter = DateFilter.CUSTOM,
            customStartMillis = startMillis,
            customEndMillis = endMillis
        )
        recalculate()
    }

    fun onSearchChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        recalculate()
    }

    fun selectItem(item: ItemSalesData?) {
        _uiState.value = _uiState.value.copy(selectedItem = item)
    }

    private fun recalculate() {
        val state = _uiState.value
        val filteredOrders = filterOrdersByDate(state.allOrders, state.dateFilter, state.customStartMillis, state.customEndMillis)

        // Build a map of productId -> aggregated data
        val salesMap = mutableMapOf<String, MutableTriple>()

        for (order in filteredOrders) {
            for (item in order.items) {
                val key = item.productId.ifBlank { item.name }
                val entry = salesMap.getOrPut(key) { MutableTriple(item.name, item.imageBase64) }
                entry.ordersSet.add(order.orderId)
                entry.totalQty += item.qty
                entry.totalRevenue += item.lineTotal
            }
        }

        // Build product stock lookup
        val stockMap = state.products.associate { it.id to it }

        val itemSalesList = salesMap.map { (productId, data) ->
            val product = stockMap[productId]
            ItemSalesData(
                productId = productId,
                productName = data.name,
                imageBase64 = product?.imageBase64 ?: data.imageBase64,
                ordersCount = data.ordersSet.size,
                totalQtySold = data.totalQty,
                totalRevenue = data.totalRevenue,
                currentStock = product?.stockQty ?: 0
            )
        }.let { list ->
            if (state.searchQuery.isBlank()) list
            else list.filter { it.productName.contains(state.searchQuery, ignoreCase = true) }
        }.sortedByDescending { it.totalQtySold }

        val lowStock = itemSalesList.filter { it.currentStock < 5 }

        // Profit = 5% of revenue from delivered orders in the filtered range
        val deliveredRevenue = filteredOrders
            .filter { it.status == "Delivered" }
            .sumOf { it.totalAmount }
        val profit = deliveredRevenue * 0.05

        _uiState.value = state.copy(
            isLoading = false,
            itemSalesList = itemSalesList,
            totalOrders = filteredOrders.size,
            totalRevenue = filteredOrders.filter { it.status != "Cancelled" }.sumOf { it.totalAmount },
            totalProfit = profit,
            totalItemsSold = itemSalesList.sumOf { it.totalQtySold },
            lowStockItems = lowStock,
            selectedItem = state.selectedItem?.let { sel ->
                itemSalesList.find { it.productId == sel.productId }
            }
        )
    }

    private fun filterOrdersByDate(
        orders: List<Order>, filter: DateFilter, customStart: Long?, customEnd: Long?
    ): List<Order> {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        val (startMs, endMs) = when (filter) {
            DateFilter.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis to now
            }
            DateFilter.WEEKLY -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                cal.timeInMillis to now
            }
            DateFilter.MONTHLY -> {
                cal.add(Calendar.MONTH, -1)
                cal.timeInMillis to now
            }
            DateFilter.CUSTOM -> {
                (customStart ?: 0L) to (customEnd ?: now)
            }
        }

        return orders.filter { order ->
            val orderMs = order.createdAt?.toDate()?.time ?: 0L
            orderMs in startMs..endMs
        }
    }

    /** Helper class for aggregating sales */
    private class MutableTriple(val name: String, val imageBase64: String) {
        val ordersSet = mutableSetOf<String>()
        var totalQty = 0.0
        var totalRevenue = 0.0
    }
}

