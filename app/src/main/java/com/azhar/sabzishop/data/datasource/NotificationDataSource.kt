package com.azhar.sabzishop.data.datasource

import android.util.Log
import com.azhar.sabzishop.utils.Constants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
    private val TAG = "NotificationDS"

    /**
     * Write a notification document to Firestore.
     * @param targetRole "admin" to notify all admins, or null if targetUserId is set
     * @param targetUserId specific user to notify (for customers)
     */
    suspend fun sendNotification(
        title: String,
        body: String,
        targetRole: String? = null,
        targetUserId: String? = null,
        orderId: String = ""
    ) {
        try {
            val data = mapOf(
                "title" to title,
                "body" to body,
                "targetRole" to (targetRole ?: ""),
                "targetUserId" to (targetUserId ?: ""),
                "orderId" to orderId,
                "read" to false,
                "createdAt" to Timestamp.now()
            )
            collection.add(data).await()
            Log.d(TAG, "Notification sent: $title -> role=$targetRole, userId=$targetUserId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send notification: ${e.message}")
        }
    }

    /**
     * Listen for unread notifications for a specific user or role.
     * Uses simple query without compound index requirement.
     */
    fun listenForNotifications(
        userId: String,
        userRole: String,
        onNotification: (title: String, body: String, docId: String) -> Unit
    ): ListenerRegistration {
        Log.d(TAG, "Starting notification listener for userId=$userId, role=$userRole")

        // Simple query: just listen for unread notifications (no orderBy to avoid index requirement)
        return collection
            .whereEqualTo("read", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Notification listener error: ${error.message}")
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val doc = change.document
                        val targetUserId = doc.getString("targetUserId") ?: ""
                        val targetRole = doc.getString("targetRole") ?: ""

                        // Check if notification is for this user or their role
                        val isForMe = (targetUserId.isNotBlank() && targetUserId == userId) ||
                                (targetRole.isNotBlank() && targetRole == userRole)

                        if (isForMe) {
                            val title = doc.getString("title") ?: ""
                            val body = doc.getString("body") ?: ""
                            Log.d(TAG, "Notification received: $title")
                            onNotification(title, body, doc.id)

                            // Mark as read so it doesn't fire again
                            doc.reference.update("read", true)
                        }
                    }
                }
            }
    }
}
