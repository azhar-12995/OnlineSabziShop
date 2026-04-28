package com.azhar.sabzishop.domain.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val street: String = "",
    val houseNumber: String = "",
    val profileImageBase64: String = "",
    val role: String = "customer",
    val createdAt: Timestamp? = null
)
