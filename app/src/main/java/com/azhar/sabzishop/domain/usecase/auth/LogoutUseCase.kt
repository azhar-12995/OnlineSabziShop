package com.azhar.sabzishop.domain.usecase.auth

import com.azhar.sabzishop.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke() = repo.logout()
}

