package com.azhar.sabzishop.presentation.user.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.sabzishop.domain.model.User
import com.azhar.sabzishop.domain.usecase.auth.GetCurrentUserUseCase
import com.azhar.sabzishop.domain.usecase.auth.LogoutUseCase
import com.azhar.sabzishop.domain.usecase.auth.UpdateUserProfileUseCase
import com.azhar.sabzishop.utils.Resource
import com.azhar.sabzishop.notification.NotificationManager as SabziNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    // Editable fields
    val editFullName: String = "",
    val editPhone: String = "",
    val editAddress: String = "",
    val editStreet: String = "",
    val editHouseNumber: String = "",
    val editProfileImageBase64: String = "",
    val selectedImageUri: Uri? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val loggedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val sabziNotificationManager: SabziNotificationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _uiState.value = _uiState.value.copy(user = user, isLoading = false)
            }
        }
    }

    fun startEditing() {
        val user = _uiState.value.user ?: return
        _uiState.value = _uiState.value.copy(
            isEditing = true,
            editFullName = user.fullName,
            editPhone = user.phone,
            editAddress = user.address,
            editStreet = user.street,
            editHouseNumber = user.houseNumber,
            editProfileImageBase64 = user.profileImageBase64,
            errorMessage = null, successMessage = null
        )
    }

    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(isEditing = false, errorMessage = null, selectedImageUri = null)
    }

    fun onFullNameChange(v: String) { _uiState.value = _uiState.value.copy(editFullName = v) }
    fun onPhoneChange(v: String) { _uiState.value = _uiState.value.copy(editPhone = v) }
    fun onAddressChange(v: String) { _uiState.value = _uiState.value.copy(editAddress = v) }
    fun onStreetChange(v: String) { _uiState.value = _uiState.value.copy(editStreet = v) }
    fun onHouseNumberChange(v: String) { _uiState.value = _uiState.value.copy(editHouseNumber = v) }

    fun onProfileImageSelected(base64: String) {
        _uiState.value = _uiState.value.copy(editProfileImageBase64 = base64)
    }

    fun saveProfile() {
        val user = _uiState.value.user ?: return
        val s = _uiState.value
        if (s.editFullName.isBlank()) {
            _uiState.value = s.copy(errorMessage = "Name is required")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            val updatedUser = user.copy(
                fullName = s.editFullName.trim(),
                phone = s.editPhone.trim(),
                address = s.editAddress.trim(),
                street = s.editStreet.trim(),
                houseNumber = s.editHouseNumber.trim(),
                profileImageBase64 = s.editProfileImageBase64
            )
            when (val result = updateUserProfileUseCase(updatedUser)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false, isEditing = false,
                        user = updatedUser, successMessage = "Profile updated!",
                        selectedImageUri = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = result.message)
                }
                else -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun logout() {
        viewModelScope.launch {
            sabziNotificationManager.stop()
            logoutUseCase()
            _uiState.value = _uiState.value.copy(loggedOut = true)
        }
    }
}
