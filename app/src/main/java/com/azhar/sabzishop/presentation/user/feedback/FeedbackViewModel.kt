package com.azhar.sabzishop.presentation.user.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.sabzishop.domain.model.Feedback
import com.azhar.sabzishop.domain.model.User
import com.azhar.sabzishop.domain.usecase.auth.GetCurrentUserUseCase
import com.azhar.sabzishop.domain.usecase.feedback.GetUserFeedbacksUseCase
import com.azhar.sabzishop.domain.usecase.feedback.SubmitFeedbackUseCase
import com.azhar.sabzishop.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedbackUiState(
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val rating: Int = 5,
    val message: String = "",
    val myFeedbacks: List<Feedback> = emptyList(),
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val user: User? = null
)

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val submitFeedbackUseCase: SubmitFeedbackUseCase,
    private val getUserFeedbacksUseCase: GetUserFeedbacksUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().firstOrNull()
            _uiState.value = _uiState.value.copy(user = user)
            if (user != null) {
                getUserFeedbacksUseCase(user.uid).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                        is Resource.Success -> _uiState.value = _uiState.value.copy(
                            isLoading = false, myFeedbacks = resource.data
                        )
                        is Resource.Error -> _uiState.value = _uiState.value.copy(
                            isLoading = false, errorMessage = resource.message
                        )
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onRatingChange(rating: Int) {
        _uiState.value = _uiState.value.copy(rating = rating)
    }

    fun onMessageChange(msg: String) {
        _uiState.value = _uiState.value.copy(message = msg)
    }

    fun submitFeedback() {
        val state = _uiState.value
        val user = state.user ?: return
        if (state.message.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please write your feedback message")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true, errorMessage = null)
            val feedback = Feedback(
                userId = user.uid,
                userName = user.fullName,
                userEmail = user.email,
                rating = state.rating,
                message = state.message
            )
            when (val result = submitFeedbackUseCase(feedback)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    isSending = false, message = "", rating = 5,
                    successMessage = "Thank you for your feedback!"
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isSending = false, errorMessage = result.message
                )
                else -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}

