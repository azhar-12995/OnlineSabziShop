package com.azhar.sabzishop.domain.repository

import com.azhar.sabzishop.domain.model.User
import com.azhar.sabzishop.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        address: String,
        street: String,
        houseNumber: String
    ): Resource<User>

    suspend fun login(email: String, password: String): Resource<User>
    suspend fun logout()
    suspend fun sendPasswordResetEmail(email: String): Resource<Unit>
    fun getCurrentUser(): Flow<User?>
    fun isLoggedIn(): Boolean
    suspend fun updateUserProfile(user: User): Resource<Unit>
}
