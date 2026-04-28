package com.azhar.sabzishop.data.mapper

import com.azhar.sabzishop.data.model.CartItemDto
import com.azhar.sabzishop.data.model.OrderDto
import com.azhar.sabzishop.data.model.OrderItemDto
import com.azhar.sabzishop.data.model.ProductDto
import com.azhar.sabzishop.data.model.UserDto
import com.azhar.sabzishop.domain.model.CartItem
import com.azhar.sabzishop.domain.model.Order
import com.azhar.sabzishop.domain.model.OrderItem
import com.azhar.sabzishop.domain.model.Product
import com.azhar.sabzishop.domain.model.User

// ---- User Mapper ----
fun UserDto.toDomain() = User(uid, fullName, email, phone, address, street, houseNumber, profileImageBase64, role, createdAt)
fun User.toDto() = UserDto(uid, fullName, email, phone, address, street, houseNumber, profileImageBase64, role, createdAt)
fun Map<String, Any?>.toUserDto() = UserDto(
    uid = this["uid"] as? String ?: "",
    fullName = this["fullName"] as? String ?: "",
    email = this["email"] as? String ?: "",
    phone = this["phone"] as? String ?: "",
    address = this["address"] as? String ?: "",
    street = this["street"] as? String ?: "",
    houseNumber = this["houseNumber"] as? String ?: "",
    profileImageBase64 = this["profileImageBase64"] as? String ?: "",
    role = this["role"] as? String ?: "customer"
)

// ---- Product Mapper ----
fun ProductDto.toDomain() = Product(id, name, category, description, pricePerKg, stockQty, imageBase64, isAvailable, createdAt, updatedAt)
fun Product.toDto() = ProductDto(id, name, category, description, pricePerKg, stockQty, imageBase64, isAvailable, createdAt, updatedAt)
fun Product.toMap(): Map<String, Any?> = mapOf(
    "id" to id, "name" to name, "category" to category, "description" to description,
    "pricePerKg" to pricePerKg, "stockQty" to stockQty, "imageBase64" to imageBase64,
    "isAvailable" to isAvailable, "createdAt" to createdAt, "updatedAt" to updatedAt
)
fun Map<String, Any?>.toProduct(id: String) = Product(
    id = id,
    name = this["name"] as? String ?: "",
    category = this["category"] as? String ?: "",
    description = this["description"] as? String ?: "",
    pricePerKg = (this["pricePerKg"] as? Number)?.toDouble() ?: 0.0,
    stockQty = (this["stockQty"] as? Number)?.toInt() ?: 0,
    imageBase64 = this["imageBase64"] as? String ?: "",
    isAvailable = this["isAvailable"] as? Boolean ?: true
)

// ---- CartItem Mapper ----
fun CartItemDto.toDomain() = CartItem(itemId, productId, name, pricePerKg, qty, imageBase64, lineTotal)
fun CartItem.toDto() = CartItemDto(itemId, productId, name, pricePerKg, qty, imageBase64, lineTotal)
fun CartItem.toMap(): Map<String, Any?> = mapOf(
    "itemId" to itemId, "productId" to productId, "name" to name,
    "pricePerKg" to pricePerKg, "qty" to qty, "imageBase64" to imageBase64,
    "lineTotal" to lineTotal
)
fun Map<String, Any?>.toCartItem(id: String) = CartItem(
    itemId = id,
    productId = this["productId"] as? String ?: "",
    name = this["name"] as? String ?: "",
    pricePerKg = (this["pricePerKg"] as? Number)?.toDouble() ?: 0.0,
    qty = (this["qty"] as? Number)?.toDouble() ?: 1.0,
    imageBase64 = this["imageBase64"] as? String ?: "",
    lineTotal = (this["lineTotal"] as? Number)?.toDouble() ?: 0.0
)

// ---- OrderItem Mapper ----
fun OrderItem.toMap(): Map<String, Any?> = mapOf(
    "productId" to productId, "name" to name, "pricePerKg" to pricePerKg,
    "qty" to qty, "imageBase64" to imageBase64, "lineTotal" to lineTotal
)
@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toOrderItem() = OrderItem(
    productId = this["productId"] as? String ?: "",
    name = this["name"] as? String ?: "",
    pricePerKg = (this["pricePerKg"] as? Number)?.toDouble() ?: 0.0,
    qty = (this["qty"] as? Number)?.toDouble() ?: 1.0,
    imageBase64 = this["imageBase64"] as? String ?: "",
    lineTotal = (this["lineTotal"] as? Number)?.toDouble() ?: 0.0
)

// ---- Order Mapper ----
@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toOrder(id: String): Order {
    val rawItems = this["items"] as? List<Map<String, Any?>> ?: emptyList()
    return Order(
        orderId = id,
        userId = this["userId"] as? String ?: "",
        customerName = this["customerName"] as? String ?: "",
        customerPhone = this["customerPhone"] as? String ?: "",
        deliveryAddress = this["deliveryAddress"] as? String ?: "",
        items = rawItems.map { it.toOrderItem() },
        subtotal = (this["subtotal"] as? Number)?.toDouble() ?: 0.0,
        deliveryCharges = (this["deliveryCharges"] as? Number)?.toDouble() ?: 20.0,
        totalAmount = (this["totalAmount"] as? Number)?.toDouble() ?: 0.0,
        status = this["status"] as? String ?: "Pending"
    )
}
fun Order.toMap(): Map<String, Any?> = mapOf(
    "orderId" to orderId, "userId" to userId, "customerName" to customerName,
    "customerPhone" to customerPhone, "deliveryAddress" to deliveryAddress,
    "items" to items.map { it.toMap() }, "subtotal" to subtotal,
    "deliveryCharges" to deliveryCharges, "totalAmount" to totalAmount,
    "status" to status, "createdAt" to createdAt
)

