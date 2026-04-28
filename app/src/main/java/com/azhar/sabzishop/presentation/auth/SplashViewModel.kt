package com.azhar.sabzishop.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.sabzishop.domain.usecase.auth.GetCurrentUserUseCase
import com.azhar.sabzishop.utils.Constants
import com.azhar.sabzishop.notification.NotificationManager as SabziNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplashUiState(
    val isLoading: Boolean = true,
    val destination: String? = null // "user", "admin", "auth"
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val sabziNotificationManager: SabziNotificationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            if (!getCurrentUserUseCase.isLoggedIn()) {
                _uiState.value = SplashUiState(isLoading = false, destination = "auth")
                return@launch
            }
            getCurrentUserUseCase().collect { user ->
                val dest = when {
                    user == null -> "auth"
                    user.role == Constants.ROLE_ADMIN -> "admin"
                    else -> "user"
                }
                // Start notification listener
                if (user != null) {
                    sabziNotificationManager.start(user.uid, user.role)
                }
                _uiState.value = SplashUiState(isLoading = false, destination = dest)
            }
        }
    }
}

