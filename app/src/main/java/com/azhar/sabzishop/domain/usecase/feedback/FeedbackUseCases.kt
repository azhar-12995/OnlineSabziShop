package com.azhar.sabzishop.domain.usecase.feedback

import com.azhar.sabzishop.domain.model.Feedback
import com.azhar.sabzishop.domain.repository.FeedbackRepository
import com.azhar.sabzishop.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SubmitFeedbackUseCase @Inject constructor(private val repo: FeedbackRepository) {
    suspend operator fun invoke(feedback: Feedback): Resource<String> = repo.submitFeedback(feedback)
}

class GetAllFeedbacksUseCase @Inject constructor(private val repo: FeedbackRepository) {
    operator fun invoke(): Flow<Resource<List<Feedback>>> = repo.getAllFeedbacks()
}

class GetUserFeedbacksUseCase @Inject constructor(private val repo: FeedbackRepository) {
    operator fun invoke(userId: String): Flow<Resource<List<Feedback>>> = repo.getUserFeedbacks(userId)
}

