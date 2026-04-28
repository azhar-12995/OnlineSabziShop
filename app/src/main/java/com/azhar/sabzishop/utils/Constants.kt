package com.azhar.sabzishop.utils

object Constants {
    // Firestore Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_PRODUCTS = "products"
    const val COLLECTION_ORDERS = "orders"
    const val COLLECTION_CART = "cart"
    const val COLLECTION_CART_ITEMS = "items"
    const val COLLECTION_NOTIFICATIONS = "notifications"
    const val COLLECTION_FEEDBACKS = "feedbacks"

    // User Roles
    const val ROLE_CUSTOMER = "customer"
    const val ROLE_ADMIN = "admin"

    // Order Statuses
    const val STATUS_PENDING = "Pending"
    const val STATUS_CONFIRMED = "Confirmed"
    const val STATUS_DELIVERED = "Delivered"
    const val STATUS_CANCELLED = "Cancelled"

    // Product Categories
    val CATEGORIES = listOf("All", "Leafy", "Root", "Fruits", "Others")

    // Delivery Charge
    const val DELIVERY_CHARGE = 20.0

    // Image compression
    const val IMAGE_COMPRESSION_QUALITY = 60
    const val IMAGE_MAX_DIMENSION = 300
}

