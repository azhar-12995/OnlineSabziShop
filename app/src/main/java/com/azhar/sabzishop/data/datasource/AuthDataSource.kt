package com.azhar.sabzishop.data.datasource

import com.azhar.sabzishop.data.mapper.toDomain
import com.azhar.sabzishop.data.mapper.toUserDto
import com.azhar.sabzishop.data.model.UserDto
import com.azhar.sabzishop.domain.model.User
import com.azhar.sabzishop.utils.Constants
import com.azhar.sabzishop.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    fun isLoggedIn() = auth.currentUser != null

    fun getCurrentUserFlow(): Flow<User?> = callbackFlow {
        // Listen to auth state changes
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            if (uid == null) {
                trySend(null)
            } else {
                // Fetch user profile from Firestore
                firestore.collection(Constants.COLLECTION_USERS).document(uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        val userDto = doc.data?.toUserDto() ?: UserDto(uid = uid)
                        trySend(userDto.toDomain())
                    }
                    .addOnFailureListener { trySend(null) }
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUp(
        fullName: String, email: String, phone: String, password: String,
        address: String, street: String, houseNumber: String
    ): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("Failed to get user ID")
            val userDto = UserDto(
                uid = uid, fullName = fullName, email = email, phone = phone,
                address = address, street = street, houseNumber = houseNumber,
                role = Constants.ROLE_CUSTOMER
            )
            // Save user profile to Firestore
            firestore.collection(Constants.COLLECTION_USERS).document(uid)
                .set(userDto).await()
            Resource.Success(userDto.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sign up failed")
        }
    }

    suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("Failed to get user ID")
            val doc = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
            val userDto = doc.data?.toUserDto() ?: UserDto(uid = uid, email = email)
            Resource.Success(userDto.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }

    suspend fun logout() {
        auth.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send reset email")
        }
    }

    suspend fun updateUserProfile(user: User): Resource<Unit> {
        return try {
            val data = mapOf(
                "fullName" to user.fullName,
                "phone" to user.phone,
                "address" to user.address,
                "street" to user.street,
                "houseNumber" to user.houseNumber,
                "profileImageBase64" to user.profileImageBase64
            )
            firestore.collection(Constants.COLLECTION_USERS).document(user.uid)
                .update(data).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update profile")
        }
    }
}
