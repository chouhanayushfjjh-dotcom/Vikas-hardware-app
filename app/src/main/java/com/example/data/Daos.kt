package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'SELLER' LIMIT 1")
    suspend fun getSeller(): UserEntity?

    @Query("SELECT * FROM users ORDER BY role ASC, name ASC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Long)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY isDailySpecial DESC, name ASC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isAvailable = 1 ORDER BY availableQuantity DESC, isDailySpecial DESC, name ASC")
    fun getAvailableProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE category = :category AND isAvailable = 1 ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("UPDATE products SET isAvailable = :isAvailable, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateAvailability(id: Long, isAvailable: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE products SET availableQuantity = :quantity, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateQuantity(id: Long, quantity: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items WHERE buyerId = :buyerId")
    fun getCartItemsByBuyer(buyerId: Long): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE buyerId = :buyerId AND productId = :productId LIMIT 1")
    suspend fun getCartItem(buyerId: Long, productId: Long): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItemEntity): Long

    @Update
    suspend fun updateCartItem(cartItem: CartItemEntity)

    @Delete
    suspend fun deleteCartItem(cartItem: CartItemEntity)

    @Query("UPDATE cart_items SET isSavedForLater = :isSaved WHERE id = :cartItemId")
    suspend fun updateSaveForLater(cartItemId: Long, isSaved: Boolean)

    @Query("DELETE FROM cart_items WHERE id = :cartItemId")
    suspend fun deleteCartItemById(cartItemId: Long)

    @Query("DELETE FROM cart_items WHERE buyerId = :buyerId")
    suspend fun clearCartForBuyer(buyerId: Long)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrdersFlow(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE buyerId = :buyerId ORDER BY createdAt DESC")
    fun getOrdersForBuyer(buyerId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: Long): OrderEntity?

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    fun getOrderFlowById(orderId: Long): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET status = :status, deliveryService = :deliveryService, deliveryDetails = :deliveryDetails, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderDelivery(
        orderId: Long,
        status: String,
        deliveryService: String,
        deliveryDetails: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE orders SET status = :status, deliveryService = :deliveryService, deliveryDetails = :deliveryDetails, trackingNumber = :trackingNumber, courierName = :courierName, transportVehicleName = :transportVehicleName, trackingStatus = :trackingStatus, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderTracking(
        orderId: Long,
        status: String,
        deliveryService: String,
        deliveryDetails: String,
        trackingNumber: String,
        courierName: String,
        transportVehicleName: String,
        trackingStatus: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE orders SET isLatePaymentAllowed = :isAllowed, latePaymentStatus = :lateStatus, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderLatePaymentAllowed(
        orderId: Long,
        isAllowed: Boolean,
        lateStatus: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE orders SET totalLots = :totalLots, deliveryFee = :deliveryFee, totalPrice = subtotalPrice + :deliveryFee, lotStatus = :lotStatus, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderLotCountAndFee(
        orderId: Long,
        totalLots: Int,
        deliveryFee: Double,
        lotStatus: String = "CALCULATED",
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE orders SET status = 'CANCELLED', cancelReason = :reason, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun cancelOrder(orderId: Long, reason: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET callVerificationStatus = :status, isOrderedByMistake = :isOrderedByMistake, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateCallVerification(
        orderId: Long,
        status: String,
        isOrderedByMistake: Boolean,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItemsForOrder(orderId: Long): List<OrderItemEntity>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItemsFlowForOrder(orderId: Long): Flow<List<OrderItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId OR userRole = :role ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: Long, role: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId OR userRole = :role")
    suspend fun markAllAsRead(userId: Long, role: String)
}

@Dao
interface DirectContactDao {
    @Query("SELECT * FROM direct_contact_config WHERE id = 1 LIMIT 1")
    fun getDirectContactConfigFlow(): Flow<DirectContactConfigEntity?>

    @Query("SELECT * FROM direct_contact_config WHERE id = 1 LIMIT 1")
    suspend fun getDirectContactConfig(): DirectContactConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: DirectContactConfigEntity)
}

@Dao
interface DirectCallInquiryDao {
    @Query("SELECT * FROM direct_call_inquiries ORDER BY timestamp DESC")
    fun getAllInquiriesFlow(): Flow<List<DirectCallInquiryEntity>>

    @Query("SELECT * FROM direct_call_inquiries WHERE status = 'PENDING_SELLER_RESPONSE' ORDER BY timestamp DESC")
    fun getPendingInquiriesFlow(): Flow<List<DirectCallInquiryEntity>>

    @Query("SELECT * FROM direct_call_inquiries WHERE buyerId = :buyerId ORDER BY timestamp DESC")
    fun getInquiriesForBuyerFlow(buyerId: Long): Flow<List<DirectCallInquiryEntity>>

    @Query("SELECT * FROM direct_call_inquiries WHERE id = :id LIMIT 1")
    suspend fun getInquiryById(id: Long): DirectCallInquiryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInquiry(inquiry: DirectCallInquiryEntity): Long

    @Update
    suspend fun updateInquiry(inquiry: DirectCallInquiryEntity)

    @Query("UPDATE direct_call_inquiries SET status = :status, relatedOrderId = :relatedOrderId WHERE id = :inquiryId")
    suspend fun updateInquiryStatus(inquiryId: Long, status: String, relatedOrderId: Long? = null)
}

@Dao
interface UserDeviceDao {
    @Query("SELECT * FROM user_devices WHERE userId = :userId ORDER BY isCurrent DESC, id DESC")
    fun getDevicesForUser(userId: Long): Flow<List<UserDeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: UserDeviceEntity): Long

    @Query("DELETE FROM user_devices WHERE id = :deviceId")
    suspend fun deleteDevice(deviceId: Long)
}

@Dao
interface SavedAddressDao {
    @Query("SELECT * FROM saved_addresses WHERE userId = :userId ORDER BY isDefault DESC, id DESC")
    fun getSavedAddressesForUser(userId: Long): Flow<List<SavedAddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: SavedAddressEntity): Long

    @Query("UPDATE saved_addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultAddress(userId: Long)

    @Query("UPDATE saved_addresses SET isDefault = 1 WHERE id = :addressId")
    suspend fun setDefaultAddress(addressId: Long)

    @Query("DELETE FROM saved_addresses WHERE id = :addressId")
    suspend fun deleteAddress(addressId: Long)
}

@Dao
interface NotificationPreferenceDao {
    @Query("SELECT * FROM notification_preferences WHERE userId = :userId LIMIT 1")
    fun getPreferencesFlow(userId: Long): Flow<NotificationPreferenceEntity?>

    @Query("SELECT * FROM notification_preferences WHERE userId = :userId LIMIT 1")
    suspend fun getPreferences(userId: Long): NotificationPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreferences(pref: NotificationPreferenceEntity)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long
}

@Dao
interface RemoteConfigDao {
    @Query("SELECT * FROM remote_config WHERE id = 1 LIMIT 1")
    fun getRemoteConfigFlow(): Flow<RemoteConfigEntity?>

    @Query("SELECT * FROM remote_config WHERE id = 1 LIMIT 1")
    suspend fun getRemoteConfig(): RemoteConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRemoteConfig(config: RemoteConfigEntity)
}

@Dao
interface ProductReviewDao {
    @Query("SELECT * FROM product_reviews WHERE productId = :productId AND isApproved = 1 ORDER BY createdAt DESC")
    fun getReviewsForProductFlow(productId: Long): Flow<List<ProductReviewEntity>>

    @Query("SELECT * FROM product_reviews ORDER BY createdAt DESC")
    fun getAllReviewsFlow(): Flow<List<ProductReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ProductReviewEntity): Long

    @Query("UPDATE product_reviews SET isApproved = :isApproved WHERE id = :reviewId")
    suspend fun updateApproval(reviewId: Long, isApproved: Boolean)

    @Query("DELETE FROM product_reviews WHERE id = :reviewId")
    suspend fun deleteReview(reviewId: Long)
}

@Dao
interface RouteDetailDao {
    @Query("SELECT * FROM route_details ORDER BY routeName ASC")
    fun getAllRoutesFlow(): Flow<List<RouteDetailEntity>>

    @Query("SELECT * FROM route_details WHERE id = :routeId LIMIT 1")
    suspend fun getRouteById(routeId: Long): RouteDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteDetailEntity): Long

    @Delete
    suspend fun deleteRoute(route: RouteDetailEntity)
}

@Dao
interface SellerPaymentConfigDao {
    @Query("SELECT * FROM seller_payment_config WHERE id = 1 LIMIT 1")
    fun getPaymentConfigFlow(): Flow<SellerPaymentConfigEntity?>

    @Query("SELECT * FROM seller_payment_config WHERE id = 1 LIMIT 1")
    suspend fun getPaymentConfig(): SellerPaymentConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePaymentConfig(config: SellerPaymentConfigEntity)
}

@Dao
interface CategoryPageConfigDao {
    @Query("SELECT * FROM category_page_config WHERE id = 1 LIMIT 1")
    fun getCategoryPageConfigFlow(): Flow<CategoryPageConfigEntity?>

    @Query("SELECT * FROM category_page_config WHERE id = 1 LIMIT 1")
    suspend fun getCategoryPageConfig(): CategoryPageConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCategoryPageConfig(config: CategoryPageConfigEntity)
}
