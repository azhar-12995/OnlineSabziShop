package com.azhar.sabzishop.domain.repository

import com.azhar.sabzishop.domain.model.Feedback
import com.azhar.sabzishop.utils.Resource
import kotlinx.coroutines.flow.Flow

interface FeedbackRepository {
    suspend fun submitFeedback(feedback: Feedback): Resource<String>
    fun getAllFeedbacks(): Flow<Resource<List<Feedback>>>
    fun getUserFeedbacks(userId: String): Flow<Resource<List<Feedback>>>
}

