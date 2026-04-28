package com.azhar.sabzishop.domain.model

import com.google.firebase.Timestamp

data class Feedback(
    val feedbackId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val rating: Int = 5, // 1-5 stars
    val message: String = "",
    val createdAt: Timestamp? = null
)

