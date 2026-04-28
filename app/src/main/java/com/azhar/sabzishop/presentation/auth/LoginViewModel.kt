package com.azhar.sabzishop.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.sabzishop.domain.usecase.auth.LoginUseCase
import com.azhar.sabzishop.utils.Constants
import com.azhar.sabzishop.utils.Resource
import com.azhar.sabzishop.notification.NotificationManager as SabziNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigateTo: String? = null // "user" or "admin"
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val sabziNotificationManager: SabziNotificationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(v: String) { _uiState.value = _uiState.value.copy(email = v, errorMessage = null) }
    fun onPasswordChange(v: String) { _uiState.value = _uiState.value.copy(password = v, errorMessage = null) }

    fun login() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = loginUseCase(_uiState.value.email.trim(), _uiState.value.password)
            when (result) {
                is Resource.Success -> {
                    val user = result.data
                    // Start notification listener immediately after login
                    sabziNotificationManager.start(user.uid, user.role)
                    val dest = if (user.role == Constants.ROLE_ADMIN) "admin" else "user"
                    _uiState.value = _uiState.value.copy(isLoading = false, navigateTo = dest)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
                else -> {}
            }
        }
    }

    fun clearNavigation() { _uiState.value = _uiState.value.copy(navigateTo = null) }
}
