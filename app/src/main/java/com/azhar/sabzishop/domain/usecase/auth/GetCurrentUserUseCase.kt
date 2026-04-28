package com.azhar.sabzishop.domain.usecase.auth

import com.azhar.sabzishop.domain.model.User
import com.azhar.sabzishop.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(private val repo: AuthRepository) {
    operator fun invoke(): Flow<User?> = repo.getCurrentUser()
    fun isLoggedIn(): Boolean = repo.isLoggedIn()
}

