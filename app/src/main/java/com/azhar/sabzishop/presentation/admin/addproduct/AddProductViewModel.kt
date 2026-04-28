package com.azhar.sabzishop.presentation.admin.addproduct

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.sabzishop.domain.model.Product
import com.azhar.sabzishop.domain.usecase.product.AddProductUseCase
import com.azhar.sabzishop.utils.ImageUtils
import com.azhar.sabzishop.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AddProductUiState(
    val name: String = "",
    val category: String = "Leafy",
    val description: String = "",
    val price: String = "",
    val stock: String = "",
    val imageBase64: String = "",
    val isAvailable: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val addProductUseCase: AddProductUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddProductUiState())
    val uiState: StateFlow<AddProductUiState> = _uiState.asStateFlow()

    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(name = v) }
    fun onCategoryChange(v: String) { _uiState.value = _uiState.value.copy(category = v) }
    fun onDescriptionChange(v: String) { _uiState.value = _uiState.value.copy(description = v) }
    fun onPriceChange(v: String) { _uiState.value = _uiState.value.copy(price = v) }
    fun onStockChange(v: String) { _uiState.value = _uiState.value.copy(stock = v) }
    fun onAvailabilityChange(v: Boolean) { _uiState.value = _uiState.value.copy(isAvailable = v) }

    fun onImageSelected(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val base64 = ImageUtils.uriToBase64(context, uri) ?: ""
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(imageBase64 = base64)
            }
        }
    }

    fun saveProduct() {
        val s = _uiState.value
        if (s.name.isBlank()) { _uiState.value = s.copy(errorMessage = "Name is required"); return }
        val price = s.price.toDoubleOrNull()
        if (price == null || price <= 0) { _uiState.value = s.copy(errorMessage = "Valid price is required"); return }
        val stock = s.stock.toIntOrNull()
        if (stock == null || stock < 0) { _uiState.value = s.copy(errorMessage = "Valid stock quantity is required"); return }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val product = Product(
                name = s.name.trim(), category = s.category,
                description = s.description.trim(), pricePerKg = price,
                stockQty = stock, imageBase64 = s.imageBase64, isAvailable = s.isAvailable
            )
            when (val r = addProductUseCase(product)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = r.message)
                else -> {}
            }
        }
    }
}

