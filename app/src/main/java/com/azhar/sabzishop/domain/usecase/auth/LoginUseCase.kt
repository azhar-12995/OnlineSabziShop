package com.azhar.sabzishop.domain.usecase.auth

import com.azhar.sabzishop.domain.model.User
import com.azhar.sabzishop.domain.repository.AuthRepository
import com.azhar.sabzishop.utils.Resource
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Resource<User> {
        if (email.isBlank()) return Resource.Error("Email is required")
        if (password.isBlank()) return Resource.Error("Password is required")
        return repo.login(email, password)
    }
}

