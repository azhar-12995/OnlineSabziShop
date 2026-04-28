package com.azhar.sabzishop.presentation.admin.editproduct

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.sabzishop.domain.model.Product
import com.azhar.sabzishop.domain.usecase.product.GetProductByIdUseCase
import com.azhar.sabzishop.domain.usecase.product.UpdateProductUseCase
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

data class EditProductUiState(
    val productId: String = "",
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
class EditProductViewModel @Inject constructor(
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProductUiState())
    val uiState: StateFlow<EditProductUiState> = _uiState.asStateFlow()

    init {
        val productId = savedStateHandle.get<String>("productId") ?: ""
        loadProduct(productId)
    }

    private fun loadProduct(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val r = getProductByIdUseCase(id)) {
                is Resource.Success -> {
                    val p = r.data
                    _uiState.value = EditProductUiState(
                        productId = p.id, name = p.name, category = p.category,
                        description = p.description, price = p.pricePerKg.toString(),
                        stock = p.stockQty.toString(), imageBase64 = p.imageBase64,
                        isAvailable = p.isAvailable, isLoading = false
                    )
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = r.message)
                else -> {}
            }
        }
    }

    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(name = v) }
    fun onCategoryChange(v: String) { _uiState.value = _uiState.value.copy(category = v) }
    fun onDescriptionChange(v: String) { _uiState.value = _uiState.value.copy(description = v) }
    fun onPriceChange(v: String) { _uiState.value = _uiState.value.copy(price = v) }
    fun onStockChange(v: String) { _uiState.value = _uiState.value.copy(stock = v) }
    fun onAvailabilityChange(v: Boolean) { _uiState.value = _uiState.value.copy(isAvailable = v) }

    fun onImageSelected(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val base64 = ImageUtils.uriToBase64(context, uri) ?: ""
            withContext(Dispatchers.Main) { _uiState.value = _uiState.value.copy(imageBase64 = base64) }
        }
    }

    fun updateProduct() {
        val s = _uiState.value
        val price = s.price.toDoubleOrNull()
        if (s.name.isBlank()) { _uiState.value = s.copy(errorMessage = "Name is required"); return }
        if (price == null || price <= 0) { _uiState.value = s.copy(errorMessage = "Valid price is required"); return }
        val stock = s.stock.toIntOrNull() ?: 0

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val product = Product(
                id = s.productId, name = s.name.trim(), category = s.category,
                description = s.description.trim(), pricePerKg = price, stockQty = stock,
                imageBase64 = s.imageBase64, isAvailable = s.isAvailable
            )
            when (val r = updateProductUseCase(product)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = r.message)
                else -> {}
            }
        }
    }
}

