package com.azhar.sabzishop.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SabziFCMService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "Sabzi Shop"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        if (body.isNotBlank()) {
            NotificationHelper.showNotification(this, title, body)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token can be stored in Firestore if needed for server-side FCM
    }
}

