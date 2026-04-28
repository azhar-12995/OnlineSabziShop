package com.azhar.sabzishop.data.datasource

import com.azhar.sabzishop.data.mapper.toMap
import com.azhar.sabzishop.data.mapper.toOrder
import com.azhar.sabzishop.domain.model.Order
import com.azhar.sabzishop.utils.Constants
import com.azhar.sabzishop.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val notificationDataSource: NotificationDataSource
) {
    private val collection = firestore.collection(Constants.COLLECTION_ORDERS)

    suspend fun placeOrder(order: Order): Resource<String> {
        return try {
            val ref = collection.document()
            val orderWithId = order.copy(orderId = ref.id, createdAt = Timestamp.now())
            ref.set(orderWithId.toMap()).await()

            // Deduct stock for each ordered item
            val productsCollection = firestore.collection(Constants.COLLECTION_PRODUCTS)
            for (item in order.items) {
                if (item.productId.isNotBlank()) {
                    productsCollection.document(item.productId)
                        .update("stockQty", FieldValue.increment(-item.qty.toLong().coerceAtLeast(1)))
                        .await()
                }
            }

            // Notify admin about new order
            notificationDataSource.sendNotification(
                title = "New Order Received!",
                body = "Order #${ref.id.take(8)} from ${order.customerName} — Rs ${order.totalAmount.toLong()}",
                targetRole = Constants.ROLE_ADMIN,
                orderId = ref.id
            )

            Resource.Success(ref.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to place order")
        }
    }

    fun getUserOrders(userId: String): Flow<Resource<List<Order>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load orders"))
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    @Suppress("UNCHECKED_CAST")
                    (doc.data as? Map<String, Any?>)?.toOrder(doc.id)
                } ?: emptyList()
                trySend(Resource.Success(orders.sortedByDescending { it.createdAt?.seconds }))
            }
        awaitClose { listener.remove() }
    }

    fun getAllOrders(): Flow<Resource<List<Order>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Failed to load orders"))
                return@addSnapshotListener
            }
            val orders = snapshot?.documents?.mapNotNull { doc ->
                @Suppress("UNCHECKED_CAST")
                (doc.data as? Map<String, Any?>)?.toOrder(doc.id)
            } ?: emptyList()
            trySend(Resource.Success(orders.sortedByDescending { it.createdAt?.seconds }))
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateOrderStatus(orderId: String, status: String): Resource<Unit> {
        return try {
            collection.document(orderId).update("status", status).await()

            // Get order details for notification
            val doc = collection.document(orderId).get().await()
            val userId = doc.getString("userId") ?: ""
            val customerName = doc.getString("customerName") ?: "Customer"

            // If cancelled, restore stock
            if (status == Constants.STATUS_CANCELLED) {
                @Suppress("UNCHECKED_CAST")
                val items = doc.get("items") as? List<Map<String, Any?>> ?: emptyList()
                val productsCollection = firestore.collection(Constants.COLLECTION_PRODUCTS)
                for (item in items) {
                    val productId = item["productId"] as? String ?: continue
                    val qty = (item["qty"] as? Number)?.toLong()?.coerceAtLeast(1) ?: 1
                    productsCollection.document(productId)
                        .update("stockQty", FieldValue.increment(qty))
                        .await()
                }
            }

            // Notify customer about status update
            if (userId.isNotBlank()) {
                val statusMessage = when (status) {
                    Constants.STATUS_CONFIRMED -> "Your order #${orderId.take(8)} has been confirmed!"
                    Constants.STATUS_DELIVERED -> "Your order #${orderId.take(8)} has been delivered. Enjoy!"
                    Constants.STATUS_CANCELLED -> "Your order #${orderId.take(8)} has been cancelled."
                    else -> "Your order #${orderId.take(8)} status updated to $status."
                }
                notificationDataSource.sendNotification(
                    title = "Order $status",
                    body = statusMessage,
                    targetUserId = userId,
                    orderId = orderId
                )
            }

            // If customer cancelled, also notify admin
            if (status == Constants.STATUS_CANCELLED) {
                notificationDataSource.sendNotification(
                    title = "Order Cancelled",
                    body = "$customerName cancelled order #${orderId.take(8)}",
                    targetRole = Constants.ROLE_ADMIN,
                    orderId = orderId
                )
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update order status")
        }
    }
}
