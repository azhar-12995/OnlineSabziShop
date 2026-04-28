package com.azhar.sabzishop.data.datasource

import com.azhar.sabzishop.domain.model.Feedback
import com.azhar.sabzishop.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("feedbacks")

    suspend fun submitFeedback(feedback: Feedback): Resource<String> {
        return try {
            val ref = collection.document()
            val fb = feedback.copy(feedbackId = ref.id, createdAt = Timestamp.now())
            ref.set(
                mapOf(
                    "feedbackId" to fb.feedbackId,
                    "userId" to fb.userId,
                    "userName" to fb.userName,
                    "userEmail" to fb.userEmail,
                    "rating" to fb.rating,
                    "message" to fb.message,
                    "createdAt" to fb.createdAt
                )
            ).await()
            Resource.Success(ref.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to submit feedback")
        }
    }

    fun getAllFeedbacks(): Flow<Resource<List<Feedback>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection.orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load feedbacks"))
                    return@addSnapshotListener
                }
                val feedbacks = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.toFeedback(doc.id)
                } ?: emptyList()
                trySend(Resource.Success(feedbacks))
            }
        awaitClose { listener.remove() }
    }

    fun getUserFeedbacks(userId: String): Flow<Resource<List<Feedback>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load feedbacks"))
                    return@addSnapshotListener
                }
                val feedbacks = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.toFeedback(doc.id)
                } ?: emptyList()
                trySend(Resource.Success(feedbacks.sortedByDescending { it.createdAt?.seconds }))
            }
        awaitClose { listener.remove() }
    }

    private fun Map<String, Any?>.toFeedback(id: String) = Feedback(
        feedbackId = id,
        userId = this["userId"] as? String ?: "",
        userName = this["userName"] as? String ?: "",
        userEmail = this["userEmail"] as? String ?: "",
        rating = (this["rating"] as? Number)?.toInt() ?: 5,
        message = this["message"] as? String ?: "",
        createdAt = this["createdAt"] as? Timestamp
    )
}

