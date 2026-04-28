package com.azhar.sabzishop.domain.usecase.auth

import com.azhar.sabzishop.domain.model.User
import com.azhar.sabzishop.domain.repository.AuthRepository
import com.azhar.sabzishop.utils.Resource
import javax.inject.Inject

class SignUpUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(
        fullName: String, email: String, phone: String, password: String,
        address: String, street: String, houseNumber: String
    ): Resource<User> {
        if (fullName.isBlank()) return Resource.Error("Full name is required")
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return Resource.Error("Valid email is required")
        if (phone.isBlank() || phone.length < 10)
            return Resource.Error("Valid phone number is required")
        if (password.length < 6) return Resource.Error("Password must be at least 6 characters")
        if (address.isBlank()) return Resource.Error("Address is required")
        if (street.isBlank()) return Resource.Error("Street is required")
        if (houseNumber.isBlank()) return Resource.Error("House number is required")
        return repo.signUp(fullName, email, phone, password, address, street, houseNumber)
    }
}
