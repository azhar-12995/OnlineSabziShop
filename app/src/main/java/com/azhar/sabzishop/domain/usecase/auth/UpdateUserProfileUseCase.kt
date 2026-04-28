package com.azhar.sabzishop.domain.usecase.auth

import com.azhar.sabzishop.domain.model.User
import com.azhar.sabzishop.domain.repository.AuthRepository
import com.azhar.sabzishop.utils.Resource
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(user: User): Resource<Unit> {
        if (user.fullName.isBlank()) return Resource.Error("Full name is required")
        if (user.phone.isBlank()) return Resource.Error("Phone number is required")
        return repo.updateUserProfile(user)
    }
}

