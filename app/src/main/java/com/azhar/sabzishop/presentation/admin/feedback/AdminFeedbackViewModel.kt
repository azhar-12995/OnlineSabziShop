package com.azhar.sabzishop.presentation.admin.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.sabzishop.domain.model.Feedback
import com.azhar.sabzishop.domain.usecase.feedback.GetAllFeedbacksUseCase
import com.azhar.sabzishop.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminFeedbackUiState(
    val isLoading: Boolean = true,
    val feedbacks: List<Feedback> = emptyList(),
    val averageRating: Double = 0.0,
    val totalFeedbacks: Int = 0,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminFeedbackViewModel @Inject constructor(
    private val getAllFeedbacksUseCase: GetAllFeedbacksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminFeedbackUiState())
    val uiState: StateFlow<AdminFeedbackUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAllFeedbacksUseCase().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Resource.Success -> {
                        val feedbacks = resource.data
                        val avg = if (feedbacks.isNotEmpty()) feedbacks.map { it.rating }.average() else 0.0
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            feedbacks = feedbacks,
                            totalFeedbacks = feedbacks.size,
                            averageRating = avg
                        )
                    }
                    is Resource.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false, errorMessage = resource.message
                    )
                }
            }
        }
    }
}

