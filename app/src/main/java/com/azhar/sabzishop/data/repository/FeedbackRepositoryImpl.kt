package com.azhar.sabzishop.data.repository

import com.azhar.sabzishop.data.datasource.FeedbackDataSource
import com.azhar.sabzishop.domain.model.Feedback
import com.azhar.sabzishop.domain.repository.FeedbackRepository
import com.azhar.sabzishop.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FeedbackRepositoryImpl @Inject constructor(
    private val dataSource: FeedbackDataSource
) : FeedbackRepository {
    override suspend fun submitFeedback(feedback: Feedback): Resource<String> = dataSource.submitFeedback(feedback)
    override fun getAllFeedbacks(): Flow<Resource<List<Feedback>>> = dataSource.getAllFeedbacks()
    override fun getUserFeedbacks(userId: String): Flow<Resource<List<Feedback>>> = dataSource.getUserFeedbacks(userId)
}

