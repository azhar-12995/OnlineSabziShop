package com.azhar.sabzishop.data.repository

import com.azhar.sabzishop.data.datasource.AuthDataSource
import com.azhar.sabzishop.domain.model.User
import com.azhar.sabzishop.domain.repository.AuthRepository
import com.azhar.sabzishop.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val dataSource: AuthDataSource
) : AuthRepository {
    override suspend fun signUp(
        fullName: String, email: String, phone: String, password: String,
        address: String, street: String, houseNumber: String
    ) = dataSource.signUp(fullName, email, phone, password, address, street, houseNumber)

    override suspend fun login(email: String, password: String) =
        dataSource.login(email, password)

    override suspend fun logout() = dataSource.logout()

    override suspend fun sendPasswordResetEmail(email: String) =
        dataSource.sendPasswordResetEmail(email)

    override fun getCurrentUser(): Flow<User?> = dataSource.getCurrentUserFlow()

    override fun isLoggedIn() = dataSource.isLoggedIn()

    override suspend fun updateUserProfile(user: User): Resource<Unit> =
        dataSource.updateUserProfile(user)
}
