package com.azhar.sabzishop.domain.usecase.auth

import com.azhar.sabzishop.domain.repository.AuthRepository
import com.azhar.sabzishop.utils.Resource
import javax.inject.Inject

class ForgotPasswordUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String): Resource<Unit> {
        if (email.isBlank()) return Resource.Error("Email is required")
        return repo.sendPasswordResetEmail(email)
    }
}

