package com.azhar.sabzishop.notification

import android.content.Context
import com.azhar.sabzishop.data.datasource.NotificationDataSource
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages in-app notification listening via Firestore.
 * Call start() when user logs in, stop() on logout.
 */
@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationDataSource: NotificationDataSource
) {
    private var listener: ListenerRegistration? = null

    fun start(userId: String, userRole: String) {
        stop() // Remove any existing listener
        listener = notificationDataSource.listenForNotifications(userId, userRole) { title, body, _ ->
            NotificationHelper.showNotification(context, title, body)
        }
    }

    fun stop() {
        listener?.remove()
        listener = null
    }
}

