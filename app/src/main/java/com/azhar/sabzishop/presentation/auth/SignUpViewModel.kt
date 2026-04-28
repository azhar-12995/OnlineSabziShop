package com.azhar.sabzishop.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.sabzishop.domain.usecase.auth.SignUpUseCase
import com.azhar.sabzishop.utils.Resource
import com.azhar.sabzishop.notification.NotificationManager as SabziNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val street: String = "",
    val houseNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val sabziNotificationManager: SabziNotificationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onFullNameChange(v: String) { _uiState.value = _uiState.value.copy(fullName = v, errorMessage = null) }
    fun onEmailChange(v: String) { _uiState.value = _uiState.value.copy(email = v, errorMessage = null) }
    fun onPhoneChange(v: String) { _uiState.value = _uiState.value.copy(phone = v, errorMessage = null) }
    fun onAddressChange(v: String) { _uiState.value = _uiState.value.copy(address = v, errorMessage = null) }
    fun onStreetChange(v: String) { _uiState.value = _uiState.value.copy(street = v, errorMessage = null) }
    fun onHouseNumberChange(v: String) { _uiState.value = _uiState.value.copy(houseNumber = v, errorMessage = null) }
    fun onPasswordChange(v: String) { _uiState.value = _uiState.value.copy(password = v, errorMessage = null) }
    fun onConfirmPasswordChange(v: String) { _uiState.value = _uiState.value.copy(confirmPassword = v, errorMessage = null) }

    fun signUp() {
        val s = _uiState.value
        if (s.password != s.confirmPassword) {
            _uiState.value = s.copy(errorMessage = "Passwords do not match")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = signUpUseCase(
                s.fullName.trim(), s.email.trim(), s.phone.trim(), s.password,
                s.address.trim(), s.street.trim(), s.houseNumber.trim()
            )
            when (result) {
                is Resource.Success -> {
                    // Start notification listener after signup
                    val user = result.data
                    sabziNotificationManager.start(user.uid, user.role)
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                else -> {}
            }
        }
    }
}
