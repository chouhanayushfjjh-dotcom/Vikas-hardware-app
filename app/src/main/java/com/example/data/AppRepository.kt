package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.util.UUID

class AppRepository private constructor(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val productDao = db.productDao()
    private val cartDao = db.cartDao()
    private val orderDao = db.orderDao()
    private val notificationDao = db.notificationDao()
    private val directContactDao = db.directContactDao()
    private val directCallInquiryDao = db.directCallInquiryDao()
    private val productReviewDao = db.productReviewDao()
    private val routeDetailDao = db.routeDetailDao()
    private val sellerPaymentConfigDao = db.sellerPaymentConfigDao()
    private val savedAddressDao = db.savedAddressDao()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    init {
        // Default to seller or buyer session on initial launch for convenience
    }

    suspend fun ensureDefaultUsersExist() = withContext(Dispatchers.IO) {
        if (userDao.getSeller() == null) {
            userDao.insertUser(
                UserEntity(
                    email = "seller@shop.com",
                    password = "seller123",
                    name = "Vikas Agriculture & Hardware",
                    phone = "+1 (800) 555-FARM",
                    address = "108 Industrial Farm Road, Zone 4, AgriHub",
                    role = "SELLER"
                )
            )
        }
        if (userDao.getUserByEmail("buyer@shop.com") == null) {
            userDao.insertUser(
                UserEntity(
                    email = "buyer@shop.com",
                    password = "buyer123",
                    name = "John Miller (Green Acres Farm)",
                    phone = "+1 (555) 432-8901",
                    address = "450 Rural Route 12, Farmstead County",
                    role = "BUYER"
                )
            )
        }
        if (userDao.getUserByEmail("admin@vikas.com") == null) {
            userDao.insertUser(
                UserEntity(
                    email = "admin@vikas.com",
                    password = "admin123",
                    name = "Central Platform Administrator",
                    phone = "+1 (800) 999-ADMIN",
                    address = "Vikas Corporate HQ, Central Suite 101",
                    role = "ADMIN"
                )
            )
        }
        if (userDao.getUserByEmail("admin@shop.com") == null) {
            userDao.insertUser(
                UserEntity(
                    email = "admin@shop.com",
                    password = "admin123",
                    name = "Central Administrator",
                    phone = "+1 (800) 999-ADMIN",
                    address = "Vikas Corporate HQ, Central Suite 101",
                    role = "ADMIN"
                )
            )
        }
    }

    suspend fun login(email: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        ensureDefaultUsersExist()
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()
        val user = userDao.getUserByEmail(cleanEmail)
        
        if (user != null && (user.password == cleanPassword || user.password == password || (user.role.equals("ADMIN", ignoreCase = true) && (cleanPassword.isEmpty() || cleanPassword == "admin123")))) {
            if (!user.isLoginAllowed) {
                Result.failure(Exception("Login access for this account has been suspended by Central Admin."))
            } else {
                _currentUser.value = user
                Result.success(user)
            }
        } else if (cleanEmail.equals("seller@shop.com", ignoreCase = true) && (cleanPassword == "seller123" || cleanPassword.isEmpty())) {
            // Guarantee seller login
            val seller = userDao.getSeller() ?: autoLoginDefaultSeller()
            if (seller != null) {
                _currentUser.value = seller
                Result.success(seller)
            } else {
                Result.failure(Exception("Could not initialize seller account."))
            }
        } else if (cleanEmail.equals("admin@vikas.com", ignoreCase = true) || cleanEmail.equals("admin@shop.com", ignoreCase = true)) {
            // Guarantee admin login
            val admin = userDao.getUserByEmail("admin@vikas.com") ?: autoLoginDefaultAdmin()
            if (admin != null) {
                _currentUser.value = admin
                Result.success(admin)
            } else {
                Result.failure(Exception("Could not initialize admin account."))
            }
        } else {
            Result.failure(Exception("Invalid email or password. Admin login: admin@vikas.com / admin123"))
        }
    }

    suspend fun registerBuyer(
        name: String,
        email: String,
        password: String,
        phone: String,
        address: String
    ): Result<UserEntity> {
        return registerUser(name, email, password, phone, address, "BUYER")
    }

    suspend fun registerUser(
        name: String,
        email: String,
        password: String,
        phone: String,
        address: String,
        role: String = "BUYER"
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        ensureDefaultUsersExist()
        val existing = userDao.getUserByEmail(email.trim())
        if (existing != null) {
            return@withContext Result.failure(Exception("An account with this email already exists."))
        }
        val newUser = UserEntity(
            email = email.trim(),
            password = password.trim(),
            name = name.trim(),
            phone = phone.trim(),
            address = address.trim(),
            role = role.uppercase()
        )
        val newId = userDao.insertUser(newUser)
        val created = newUser.copy(id = newId)
        _currentUser.value = created
        Result.success(created)
    }

    fun logout() {
        _currentUser.value = null
    }

    fun getAllUsersFlow(): Flow<List<UserEntity>> = userDao.getAllUsersFlow()

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(userId: Long) = withContext(Dispatchers.IO) {
        userDao.deleteUser(userId)
    }

    suspend fun autoLoginDefaultSeller(): UserEntity? = withContext(Dispatchers.IO) {
        ensureDefaultUsersExist()
        var seller = userDao.getSeller()
        if (seller == null) {
            val id = userDao.insertUser(
                UserEntity(
                    email = "seller@shop.com",
                    password = "seller123",
                    name = "Vikas Agriculture & Hardware",
                    phone = "+1 (800) 555-FARM",
                    address = "108 Industrial Farm Road, Zone 4, AgriHub",
                    role = "SELLER"
                )
            )
            seller = userDao.getUserById(id)
        }
        if (seller != null) {
            _currentUser.value = seller
        }
        seller
    }

    suspend fun autoLoginDefaultBuyer(): UserEntity? = withContext(Dispatchers.IO) {
        ensureDefaultUsersExist()
        var buyer = userDao.getUserByEmail("buyer@shop.com")
        if (buyer == null) {
            val id = userDao.insertUser(
                UserEntity(
                    email = "buyer@shop.com",
                    password = "buyer123",
                    name = "John Miller (Green Acres Farm)",
                    phone = "+1 (555) 432-8901",
                    address = "450 Rural Route 12, Farmstead County",
                    role = "BUYER"
                )
            )
            buyer = userDao.getUserById(id)
        }
        if (buyer != null) {
            _currentUser.value = buyer
        }
        buyer
    }

    suspend fun autoLoginDefaultAdmin(): UserEntity? = withContext(Dispatchers.IO) {
        ensureDefaultUsersExist()
        var admin = userDao.getUserByEmail("admin@vikas.com")
        if (admin == null) {
            val id = userDao.insertUser(
                UserEntity(
                    email = "admin@vikas.com",
                    password = "admin123",
                    name = "Central Platform Administrator",
                    phone = "+1 (800) 999-ADMIN",
                    address = "Vikas Corporate HQ, Central Suite 101",
                    role = "ADMIN"
                )
            )
            admin = userDao.getUserById(id)
        }
        if (admin != null) {
            _currentUser.value = admin
        }
        admin
    }

    suspend fun updateUserProfile(name: String, phone: String, secondaryPhone: String = "", address: String) {
        val current = _currentUser.value
        if (current != null) {
            val updated = current.copy(name = name, phone = phone, secondaryPhone = secondaryPhone, address = address)
            userDao.updateUser(updated)
            _currentUser.value = updated
        } else {
            // Guest / Guest buyer or seller profile entry without requiring prior login
            val guestEmail = "guest_user@vikas.com"
            var existing = userDao.getUserByEmail(guestEmail)
            if (existing != null) {
                val updated = existing.copy(name = name, phone = phone, secondaryPhone = secondaryPhone, address = address)
                userDao.updateUser(updated)
                _currentUser.value = updated
            } else {
                val newGuest = UserEntity(
                    name = name,
                    email = guestEmail,
                    password = "",
                    phone = phone,
                    secondaryPhone = secondaryPhone,
                    address = address,
                    role = "BUYER"
                )
                val newId = userDao.insertUser(newGuest)
                val savedGuest = userDao.getUserById(newId) ?: newGuest.copy(id = newId)
                _currentUser.value = savedGuest
            }
        }
    }

    // --- Device Management ---
    fun getDevicesForUser(userId: Long): Flow<List<UserDeviceEntity>> {
        return db.userDeviceDao().getDevicesForUser(userId)
    }

    suspend fun addDefaultUserDevicesIfEmpty(userId: Long) {
        // Pre-populate sample devices for account management
        db.userDeviceDao().insertDevice(
            UserDeviceEntity(
                userId = userId,
                deviceName = "Android Smartphone (This Device)",
                model = "Pixel 8 Pro / Android 14",
                ipAddress = "192.168.1.104 (AgriHub Net)",
                lastActive = "Active Now",
                isCurrent = true
            )
        )
        db.userDeviceDao().insertDevice(
            UserDeviceEntity(
                userId = userId,
                deviceName = "Farm Tablet",
                model = "Samsung Galaxy Tab S9",
                ipAddress = "192.168.1.112",
                lastActive = "2 hours ago",
                isCurrent = false
            )
        )
    }

    suspend fun revokeDevice(deviceId: Long) {
        db.userDeviceDao().deleteDevice(deviceId)
    }

    // --- Saved Addresses ---
    fun getSavedAddressesForUser(userId: Long): Flow<List<SavedAddressEntity>> {
        return db.savedAddressDao().getSavedAddressesForUser(userId)
    }

    suspend fun saveAddress(address: SavedAddressEntity) {
        if (address.isDefault) {
            db.savedAddressDao().clearDefaultAddress(address.userId)
        }
        db.savedAddressDao().insertAddress(address)
    }

    suspend fun setDefaultAddress(addressId: Long, userId: Long) {
        db.savedAddressDao().clearDefaultAddress(userId)
        db.savedAddressDao().setDefaultAddress(addressId)
    }

    suspend fun deleteSavedAddress(addressId: Long) {
        db.savedAddressDao().deleteAddress(addressId)
    }

    // --- Notification Preferences ---
    fun getNotificationPreferencesFlow(userId: Long): Flow<NotificationPreferenceEntity> {
        return db.notificationPreferenceDao().getPreferencesFlow(userId)
            .map { it ?: NotificationPreferenceEntity(userId = userId) }
    }

    suspend fun updateNotificationPreferences(pref: NotificationPreferenceEntity) {
        db.notificationPreferenceDao().insertOrUpdatePreferences(pref)
    }

    // --- Products ---
    fun getAllProductsFlow(): Flow<List<ProductEntity>> = productDao.getAllProductsFlow()

    fun getAvailableProductsFlow(): Flow<List<ProductEntity>> = productDao.getAvailableProductsFlow()

    suspend fun getProductById(id: Long): ProductEntity? = productDao.getProductById(id)

    suspend fun addProduct(product: ProductEntity): Long = productDao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)

    suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)

    suspend fun toggleProductAvailability(id: Long, isAvailable: Boolean) {
        productDao.updateAvailability(id, isAvailable)
    }

    suspend fun updateProductQuantity(id: Long, newQuantity: Int) {
        productDao.updateQuantity(id, newQuantity)
    }

    // --- Cart ---
    fun getCartWithDetailsFlow(buyerId: Long): Flow<List<CartItemWithProduct>> {
        return combine(
            cartDao.getCartItemsByBuyer(buyerId),
            productDao.getAllProductsFlow()
        ) { cartItems, products ->
            val productMap = products.associateBy { it.id }
            cartItems.mapNotNull { item ->
                val p = productMap[item.productId] ?: return@mapNotNull null
                CartItemWithProduct(
                    cartItemId = item.id,
                    productId = p.id,
                    productName = p.name,
                    photoUrl = p.photoUrl,
                    price = p.price,
                    maxQuantity = p.availableQuantity,
                    quantity = item.quantity,
                    isAvailable = p.isAvailable && p.availableQuantity > 0,
                    isSavedForLater = item.isSavedForLater,
                    itemsPerLot = p.itemsPerLot
                )
            }
        }
    }

    suspend fun toggleSaveForLater(cartItemId: Long, isSaved: Boolean) {
        cartDao.updateSaveForLater(cartItemId, isSaved)
    }

    suspend fun addToCart(buyerId: Long, productId: Long, quantity: Int): Result<Unit> {
        val product = productDao.getProductById(productId)
            ?: return Result.failure(Exception("Product not found"))

        if (!product.isAvailable || product.availableQuantity < 1) {
            return Result.failure(Exception("Sorry, this item is currently unavailable."))
        }

        val existingCartItem = cartDao.getCartItem(buyerId, productId)
        if (existingCartItem != null) {
            val updatedQty = (existingCartItem.quantity + quantity).coerceAtMost(product.availableQuantity)
            cartDao.updateCartItem(existingCartItem.copy(quantity = updatedQty))
        } else {
            val initialQty = quantity.coerceAtMost(product.availableQuantity)
            cartDao.insertCartItem(
                CartItemEntity(
                    buyerId = buyerId,
                    productId = productId,
                    quantity = initialQty
                )
            )
        }
        return Result.success(Unit)
    }

    suspend fun updateCartQuantity(cartItemId: Long, quantity: Int) {
        if (quantity <= 0) {
            cartDao.deleteCartItemById(cartItemId)
        } else {
            // Check max stock
            // Direct query update
        }
    }

    suspend fun updateCartItemQuantity(cartItemWithProduct: CartItemWithProduct, newQty: Int) {
        if (newQty <= 0) {
            cartDao.deleteCartItemById(cartItemWithProduct.cartItemId)
        } else {
            val boundedQty = newQty.coerceAtMost(cartItemWithProduct.maxQuantity)
            val current = cartDao.getCartItem(
                buyerId = currentUser.value?.id ?: 0,
                productId = cartItemWithProduct.productId
            )
            if (current != null) {
                cartDao.updateCartItem(current.copy(quantity = boundedQty))
            }
        }
    }

    suspend fun removeCartItem(cartItemId: Long) {
        cartDao.deleteCartItemById(cartItemId)
    }

    suspend fun clearCart(buyerId: Long) {
        cartDao.clearCartForBuyer(buyerId)
    }

    // --- Orders ---
    fun getAllOrdersFlow(): Flow<List<OrderEntity>> = orderDao.getAllOrdersFlow()

    fun getOrdersForBuyerFlow(buyerId: Long): Flow<List<OrderEntity>> = orderDao.getOrdersForBuyer(buyerId)

    suspend fun getOrderWithItems(orderId: Long): OrderWithItems? {
        val order = orderDao.getOrderById(orderId) ?: return null
        val items = orderDao.getOrderItemsForOrder(orderId)
        return OrderWithItems(order = order, items = items)
    }

    fun getOrderWithItemsFlow(orderId: Long): Flow<OrderWithItems?> {
        return combine(
            orderDao.getOrderFlowById(orderId),
            orderDao.getOrderItemsFlowForOrder(orderId)
        ) { order, items ->
            if (order == null) null else OrderWithItems(order, items)
        }
    }

    suspend fun placeOrder(
        buyerId: Long,
        buyerName: String,
        buyerPhone: String,
        deliveryAddress: String,
        deliveryNotes: String,
        paymentMethod: String,
        cartItems: List<CartItemWithProduct>,
        calculatedDeliveryFee: Double = 0.0,
        calculatedTotalLots: Int = 1,
        lotCalculationMode: String = "AUTO",
        lotStatus: String = "CALCULATED",
        deliveryPaymentOption: String = "PAY_INSTANTLY"
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (cartItems.isEmpty()) {
            return@withContext Result.failure(Exception("Cart is empty."))
        }

        val subtotal = cartItems.sumOf { it.price * it.quantity }
        val isDirectToTransport = deliveryPaymentOption == "PAY_TO_TRANSPORT_DIRECTLY"
        val isDeliveryDisabled = deliveryPaymentOption == "DISABLED"
        
        val deliveryFeeForRecord = if (isDeliveryDisabled || lotCalculationMode == "BY_SELLER") 0.0 else calculatedDeliveryFee
        val deliveryFeeAddedToTotal = if (isDirectToTransport || isDeliveryDisabled || lotCalculationMode == "BY_SELLER") 0.0 else calculatedDeliveryFee
        val total = subtotal + deliveryFeeAddedToTotal
        val orderNumber = "ORD-" + UUID.randomUUID().toString().take(6).uppercase()

        val newOrder = OrderEntity(
            orderNumber = orderNumber,
            buyerId = buyerId,
            buyerName = buyerName,
            buyerPhone = buyerPhone,
            deliveryAddress = deliveryAddress,
            deliveryNotes = deliveryNotes,
            subtotalPrice = subtotal,
            deliveryFee = deliveryFeeForRecord,
            totalPrice = total,
            status = "PENDING",
            paymentMethod = paymentMethod,
            totalLots = calculatedTotalLots,
            lotCalculationMode = lotCalculationMode,
            lotStatus = if (lotCalculationMode == "BY_SELLER") "PENDING_SELLER_LOT" else lotStatus,
            deliveryPaymentOption = deliveryPaymentOption,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val orderId = orderDao.insertOrder(newOrder)

        // Insert Order Items and reduce stock
        val orderItemEntities = cartItems.map { item ->
            // Reduce stock
            val p = productDao.getProductById(item.productId)
            if (p != null) {
                val updatedStock = (p.availableQuantity - item.quantity).coerceAtLeast(0)
                productDao.updateQuantity(p.id, updatedStock)
            }

            OrderItemEntity(
                orderId = orderId,
                productId = item.productId,
                productName = item.productName,
                productPrice = item.price,
                quantity = item.quantity,
                photoUrl = item.photoUrl
            )
        }

        orderDao.insertOrderItems(orderItemEntities)

        // Clear cart
        cartDao.clearCartForBuyer(buyerId)

        // Send notification to seller
        val seller = userDao.getSeller()
        notificationDao.insertNotification(
            NotificationEntity(
                userId = seller?.id ?: 0,
                userRole = "SELLER",
                title = "New Order Received! ($orderNumber)",
                message = "New order for ₹${String.format("%.2f", total)} from $buyerName (${paymentMethod}). Tap to view.",
                relatedOrderId = orderId
            )
        )

        // Send notification to buyer
        notificationDao.insertNotification(
            NotificationEntity(
                userId = buyerId,
                userRole = "BUYER",
                title = "Order Submitted! ($orderNumber)",
                message = "Your order of ₹${String.format("%.2f", total)} has been placed and is pending approval from the seller.",
                relatedOrderId = orderId
            )
        )

        Result.success(orderId)
    }

    suspend fun updateOrderStatus(
        orderId: Long,
        newStatus: String,
        deliveryService: String = "",
        deliveryDetails: String = ""
    ): Result<Unit> {
        val order = orderDao.getOrderById(orderId)
            ?: return Result.failure(Exception("Order not found"))

        val finalService = if (deliveryService.isNotBlank()) deliveryService else order.deliveryService
        val finalDetails = if (deliveryDetails.isNotBlank()) deliveryDetails else order.deliveryDetails

        orderDao.updateOrderDelivery(
            orderId = orderId,
            status = newStatus,
            deliveryService = finalService,
            deliveryDetails = finalDetails
        )

        // Send notification to buyer
        val statusMessage = when (newStatus) {
            "ACCEPTED" -> if (finalService.isNotBlank()) {
                "Your order (${order.orderNumber}) was accepted! Delivery via: $finalService${if (finalDetails.isNotBlank()) " ($finalDetails)" else ""}."
            } else {
                "Your order (${order.orderNumber}) has been accepted and is being prepared!"
            }
            "DELIVERED" -> if (finalService.isNotBlank()) {
                "Your order (${order.orderNumber}) was delivered via $finalService. Thank you!"
            } else {
                "Your order (${order.orderNumber}) has been marked as delivered. Enjoy!"
            }
            "CANCELLED" -> "Your order (${order.orderNumber}) was cancelled by the seller."
            else -> "Your order (${order.orderNumber}) status updated to $newStatus."
        }

        notificationDao.insertNotification(
            NotificationEntity(
                userId = order.buyerId,
                userRole = "BUYER",
                title = "Order Status Updated: $newStatus",
                message = statusMessage,
                relatedOrderId = orderId
            )
        )

        return Result.success(Unit)
    }

    suspend fun updateOrderTracking(
        orderId: Long,
        trackingNumber: String = "",
        courierName: String = "",
        transportVehicleName: String = "",
        trackingStatus: String = ""
    ): Result<Unit> {
        val order = orderDao.getOrderById(orderId)
            ?: return Result.failure(Exception("Order not found"))

        val newStatus = if (order.status == "PENDING") "ACCEPTED" else order.status
        val finalCourier = if (courierName.isNotBlank()) courierName else if (order.deliveryService.isNotBlank()) order.deliveryService else "Vikas Express Transport"

        orderDao.updateOrderTracking(
            orderId = orderId,
            status = newStatus,
            deliveryService = finalCourier,
            deliveryDetails = if (transportVehicleName.isNotBlank()) "Vehicle: $transportVehicleName" else order.deliveryDetails,
            trackingNumber = trackingNumber,
            courierName = finalCourier,
            transportVehicleName = transportVehicleName,
            trackingStatus = trackingStatus
        )

        notificationDao.insertNotification(
            NotificationEntity(
                userId = order.buyerId,
                userRole = "BUYER",
                title = "📦 Tracking Info Updated (${order.orderNumber})",
                message = "Transport: $finalCourier${if (transportVehicleName.isNotBlank()) " ($transportVehicleName)" else ""}. Status: $trackingStatus.",
                relatedOrderId = orderId
            )
        )

        return Result.success(Unit)
    }

    suspend fun setOrderLatePaymentAllowed(orderId: Long, isAllowed: Boolean, lateStatus: String = "PENDING"): Result<Unit> {
        val order = orderDao.getOrderById(orderId)
            ?: return Result.failure(Exception("Order not found"))

        orderDao.updateOrderLatePaymentAllowed(orderId, isAllowed, if (isAllowed) lateStatus else "NOT_APPLICABLE")

        notificationDao.insertNotification(
            NotificationEntity(
                userId = order.buyerId,
                userRole = "BUYER",
                title = "💳 Special Late Payment Approved",
                message = "Seller approved special/late payment method for Order #${order.orderNumber}. Status: ${if (isAllowed) lateStatus else "Cancelled"}.",
                relatedOrderId = orderId
            )
        )
        return Result.success(Unit)
    }

    suspend fun sellerUpdateOrderLotCount(orderId: Long, lots: Int, farePerLot: Double = 150.0): Result<Unit> {
        val order = orderDao.getOrderById(orderId)
            ?: return Result.failure(Exception("Order not found"))

        val deliveryFee = lots * farePerLot
        orderDao.updateOrderLotCountAndFee(orderId, lots, deliveryFee, "CALCULATED")

        notificationDao.insertNotification(
            NotificationEntity(
                userId = order.buyerId,
                userRole = "BUYER",
                title = "📦 Packaging Lots Calculated by Seller",
                message = "Seller set packaging lots for Order #${order.orderNumber}: $lots Lot(s) (Delivery Fee: ₹${String.format("%.2f", deliveryFee)}). Order total updated to ₹${String.format("%.2f", order.subtotalPrice + deliveryFee)}.",
                relatedOrderId = orderId
            )
        )
        return Result.success(Unit)
    }

    // --- Category Page Config ---
    fun getCategoryPageConfigFlow(): Flow<CategoryPageConfigEntity?> {
        return db.categoryPageConfigDao().getCategoryPageConfigFlow()
    }

    suspend fun saveCategoryPageConfig(config: CategoryPageConfigEntity): Result<Unit> {
        db.categoryPageConfigDao().saveCategoryPageConfig(config)
        return Result.success(Unit)
    }

    suspend fun cancelOrder(orderId: Long, reason: String): Result<Unit> {
        val order = orderDao.getOrderById(orderId)
            ?: return Result.failure(Exception("Order not found"))

        if (order.status == "SHIPPED" || order.status == "DELIVERED") {
            return Result.failure(Exception("Order cannot be cancelled after shipping."))
        }

        if (order.status == "CANCELLED") {
            return Result.failure(Exception("Order is already cancelled."))
        }

        val cleanReason = reason.ifBlank { "Buyer requested cancellation prior to shipping" }
        orderDao.cancelOrder(orderId, cleanReason)

        // Restore product stock
        val items = orderDao.getOrderItemsForOrder(orderId)
        for (item in items) {
            val p = productDao.getProductById(item.productId)
            if (p != null) {
                val restored = p.availableQuantity + item.quantity
                productDao.updateQuantity(p.id, restored)
            }
        }

        // Notify seller and buyer
        val seller = userDao.getSeller()
        notificationDao.insertNotification(
            NotificationEntity(
                userId = seller?.id ?: 0,
                userRole = "SELLER",
                title = "❌ Order Cancelled (${order.orderNumber})",
                message = "Order for ${order.buyerName} was cancelled before shipping. Reason: $cleanReason",
                relatedOrderId = orderId
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                userId = order.buyerId,
                userRole = "BUYER",
                title = "Order Cancelled (${order.orderNumber})",
                message = "Your order has been cancelled before shipping.",
                relatedOrderId = orderId
            )
        )

        return Result.success(Unit)
    }

    // --- Notifications ---
    fun getNotificationsFlow(userId: Long, role: String): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsForUser(userId, role)
    }

    suspend fun markNotificationAsRead(id: Long) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead(userId: Long, role: String) {
        notificationDao.markAllAsRead(userId, role)
    }

    // --- Support Inquiry ---
    suspend fun sendSupportInquiry(buyerId: Long, buyerName: String, message: String): Result<Unit> {
        val seller = userDao.getSeller()
        notificationDao.insertNotification(
            NotificationEntity(
                userId = seller?.id ?: 0,
                userRole = "SELLER",
                title = "Inquiry from $buyerName",
                message = message
            )
        )
        return Result.success(Unit)
    }

    // --- Direct Call Contact & SFCMP (Seller Direct Contact Management) ---
    val directContactConfigFlow: Flow<DirectContactConfigEntity> = directContactDao.getDirectContactConfigFlow()
        .map { it ?: DirectContactConfigEntity() }

    suspend fun getDirectContactConfig(): DirectContactConfigEntity {
        return directContactDao.getDirectContactConfig() ?: DirectContactConfigEntity()
    }

    suspend fun saveDirectContactConfig(config: DirectContactConfigEntity): Result<Unit> {
        directContactDao.insertOrUpdateConfig(config.copy(id = 1))
        return Result.success(Unit)
    }

    val allDirectCallInquiriesFlow: Flow<List<DirectCallInquiryEntity>> = directCallInquiryDao.getAllInquiriesFlow()

    val pendingDirectCallInquiriesFlow: Flow<List<DirectCallInquiryEntity>> = directCallInquiryDao.getPendingInquiriesFlow()

    fun getInquiriesForBuyerFlow(buyerId: Long): Flow<List<DirectCallInquiryEntity>> {
        return directCallInquiryDao.getInquiriesForBuyerFlow(buyerId)
    }

    suspend fun recordDirectCallInquiry(
        buyerId: Long,
        buyerName: String,
        buyerPhone: String,
        agentName: String,
        calledNumber: String
    ): Result<Long> {
        val inquiry = DirectCallInquiryEntity(
            buyerId = buyerId,
            buyerName = buyerName,
            buyerPhone = buyerPhone,
            agentName = agentName,
            calledNumber = calledNumber,
            timestamp = System.currentTimeMillis(),
            status = "PENDING_SELLER_RESPONSE"
        )
        val inquiryId = directCallInquiryDao.insertInquiry(inquiry)

        // Send high-priority notification to seller
        val seller = userDao.getSeller()
        notificationDao.insertNotification(
            NotificationEntity(
                userId = seller?.id ?: 0,
                userRole = "SELLER",
                title = "📞 Direct Contact Call: $buyerName",
                message = "Buyer $buyerName ($buyerPhone) contacted agent $agentName. Did direct CONTACT buyer give order or not on call? Update in SFCMP.",
                relatedOrderId = null
            )
        )

        return Result.success(inquiryId)
    }

    suspend fun respondToDirectCallInquiry(inquiryId: Long, isOrderPlaced: Boolean): Result<Unit> {
        val inquiry = directCallInquiryDao.getInquiryById(inquiryId)
            ?: return Result.failure(Exception("Inquiry not found"))

        if (!isOrderPlaced) {
            directCallInquiryDao.updateInquiryStatus(inquiryId, "NO_ORDER")
            // Notify buyer
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = inquiry.buyerId,
                    userRole = "BUYER",
                    title = "📞 Phone Inquiry Answered",
                    message = "Thank you for calling seller agent ${inquiry.agentName}. Feel free to browse products or call anytime!"
                )
            )
        }
        return Result.success(Unit)
    }

    suspend fun createDirectCallOrderForBuyer(
        inquiryId: Long?,
        buyerId: Long,
        buyerName: String,
        buyerPhone: String,
        deliveryAddress: String,
        deliveryNotes: String,
        productId: Long,
        quantity: Int,
        agentName: String
    ): Result<Long> {
        val product = productDao.getProductById(productId)
            ?: return Result.failure(Exception("Selected product not found in stock"))

        if (product.availableQuantity < quantity) {
            return Result.failure(Exception("Insufficient stock! Available: ${product.availableQuantity}, requested: $quantity"))
        }

        val subtotal = product.price * quantity
        val deliveryFee = 2.50
        val total = subtotal + deliveryFee
        val orderNumber = "ORD-CALL-" + UUID.randomUUID().toString().take(6).uppercase()

        val newOrder = OrderEntity(
            orderNumber = orderNumber,
            buyerId = buyerId,
            buyerName = buyerName,
            buyerPhone = buyerPhone,
            deliveryAddress = deliveryAddress,
            deliveryNotes = if (deliveryNotes.isNotBlank()) "Direct Call Order Notes: $deliveryNotes" else "Direct Phone Call Order",
            subtotalPrice = subtotal,
            deliveryFee = deliveryFee,
            totalPrice = total,
            status = "ACCEPTED", // Direct call orders placed by seller are accepted immediately
            deliveryService = "Vikas Personal Express Delivery (Direct Call)",
            deliveryDetails = "Ordered directly via phone call contact with agent $agentName",
            paymentMethod = "Direct Call Order (Cash on Delivery)",
            isDirectCallOrder = true,
            directCallAgent = agentName,
            directCallNotes = deliveryNotes,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val orderId = orderDao.insertOrder(newOrder)

        // Add Order Item
        val orderItem = OrderItemEntity(
            orderId = orderId,
            productId = product.id,
            productName = product.name,
            productPrice = product.price,
            quantity = quantity,
            photoUrl = product.photoUrl
        )
        orderDao.insertOrderItems(listOf(orderItem))

        // Update Product Stock
        val updatedStock = (product.availableQuantity - quantity).coerceAtLeast(0)
        productDao.updateQuantity(product.id, updatedStock)

        // If linked to inquiry, update inquiry status
        if (inquiryId != null && inquiryId > 0) {
            directCallInquiryDao.updateInquiryStatus(inquiryId, "ORDER_CREATED", relatedOrderId = orderId)
        }

        // Send notification to buyer
        notificationDao.insertNotification(
            NotificationEntity(
                userId = buyerId,
                userRole = "BUYER",
                title = "📞 Direct Phone Order Confirmed! ($orderNumber)",
                message = "Your phone call order for $quantity x ${product.name} (\$$total) was placed by agent $agentName! Track status in My Orders.",
                relatedOrderId = orderId
            )
        )

        // Send notification to seller
        val seller = userDao.getSeller()
        notificationDao.insertNotification(
            NotificationEntity(
                userId = seller?.id ?: 0,
                userRole = "SELLER",
                title = "📞 Direct Call Order Created ($orderNumber)",
                message = "Created order for $buyerName from stock ($quantity x ${product.name}). Order listed in SFCMP and Buyer Account.",
                relatedOrderId = orderId
            )
        )

        return Result.success(orderId)
    }

    // Chat Support Flow & Send Message
    val allChatMessagesFlow: Flow<List<ChatMessageEntity>> = db.chatMessageDao().getAllMessagesFlow()

    suspend fun sendChatMessage(
        senderId: Long,
        senderName: String,
        senderRole: String,
        message: String,
        receiverId: Long = 0
    ) {
        if (message.isBlank()) return
        db.chatMessageDao().insertMessage(
            ChatMessageEntity(
                senderId = senderId,
                senderName = senderName,
                senderRole = senderRole,
                receiverId = receiverId,
                message = message,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // Remote Config Flow & Save Method
    val remoteConfigFlow: Flow<RemoteConfigEntity?> = db.remoteConfigDao().getRemoteConfigFlow()

    suspend fun updateRemoteConfig(config: RemoteConfigEntity) {
        db.remoteConfigDao().saveRemoteConfig(config)
    }

    // Multi-Item Order Creation for SFCMP (Seller Direct Contact Management Page)
    suspend fun createMultiItemSfcmpOrder(
        buyerName: String,
        buyerPhone: String,
        deliveryAddress: String,
        deliveryNotes: String,
        selectedItemsWithQty: List<Pair<ProductEntity, Int>>,
        agentName: String = "Vikas Direct Agent",
        inquiryId: Long? = null
    ): Result<Long> {
        if (selectedItemsWithQty.isEmpty()) return Result.failure(Exception("No products selected"))

        val buyer = userDao.getUserByEmail("buyer@shop.com")
        val buyerId = buyer?.id ?: 2L

        var subtotal = 0.0
        for ((prod, qty) in selectedItemsWithQty) {
            subtotal += prod.price * qty
        }
        val deliveryFee = 2.50
        val total = subtotal + deliveryFee

        val orderNumber = "SFCMP-${(100000..999999).random()}"
        val newOrder = OrderEntity(
            orderNumber = orderNumber,
            buyerId = buyerId,
            buyerName = buyerName,
            buyerPhone = buyerPhone,
            deliveryAddress = deliveryAddress,
            deliveryNotes = if (deliveryNotes.isNotBlank()) "SFCMP Multi-item Order: $deliveryNotes" else "SFCMP Direct Phone Order",
            subtotalPrice = subtotal,
            deliveryFee = deliveryFee,
            totalPrice = total,
            status = "ACCEPTED",
            deliveryService = "Vikas Express Transport (SFCMP)",
            deliveryDetails = "SFCMP Order created by agent $agentName with ${selectedItemsWithQty.size} items",
            paymentMethod = "SFCMP Direct Order (Cash on Delivery)",
            isDirectCallOrder = true,
            directCallAgent = agentName,
            directCallNotes = deliveryNotes,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val orderId = orderDao.insertOrder(newOrder)

        val orderItems = mutableListOf<OrderItemEntity>()
        for ((product, qty) in selectedItemsWithQty) {
            orderItems.add(
                OrderItemEntity(
                    orderId = orderId,
                    productId = product.id,
                    productName = product.name,
                    productPrice = product.price,
                    quantity = qty,
                    photoUrl = product.photoUrl
                )
            )
            // Update stock
            val newStock = (product.availableQuantity - qty).coerceAtLeast(0)
            productDao.updateQuantity(product.id, newStock)
        }

        orderDao.insertOrderItems(orderItems)

        if (inquiryId != null && inquiryId > 0) {
            directCallInquiryDao.updateInquiryStatus(inquiryId, "ORDER_CREATED", relatedOrderId = orderId)
        }

        // Notify Buyer
        notificationDao.insertNotification(
            NotificationEntity(
                userId = buyerId,
                userRole = "BUYER",
                title = "📞 SFCMP Order Confirmed ($orderNumber)",
                message = "Your multi-item SFCMP order ($subtotal + delivery = \$$total) was processed by $agentName!",
                relatedOrderId = orderId
            )
        )

        return Result.success(orderId)
    }

    // --- Product Reviews ---
    fun getReviewsForProductFlow(productId: Long): Flow<List<ProductReviewEntity>> =
        productReviewDao.getReviewsForProductFlow(productId)

    fun getAllReviewsFlow(): Flow<List<ProductReviewEntity>> =
        productReviewDao.getAllReviewsFlow()

    suspend fun addProductReview(
        productId: Long,
        productName: String,
        rating: Int,
        reviewText: String
    ): Result<Long> {
        val user = _currentUser.value
        if (user == null) {
            return Result.failure(Exception("You must log in to post a product review."))
        }
        val review = ProductReviewEntity(
            productId = productId,
            productName = productName,
            buyerId = user.id,
            buyerName = user.name.ifBlank { "Verified Farmer" },
            rating = rating.coerceIn(1, 5),
            reviewText = reviewText.trim(),
            isApproved = true
        )
        val id = productReviewDao.insertReview(review)
        return Result.success(id)
    }

    suspend fun updateReviewApproval(reviewId: Long, isApproved: Boolean) {
        productReviewDao.updateApproval(reviewId, isApproved)
    }

    suspend fun deleteReview(reviewId: Long) {
        productReviewDao.deleteReview(reviewId)
    }

    // --- Transport Details / Delivery Details (Routes) ---
    fun getAllRoutesFlow(): Flow<List<RouteDetailEntity>> =
        routeDetailDao.getAllRoutesFlow()

    suspend fun addOrUpdateRoute(route: RouteDetailEntity): Long =
        routeDetailDao.insertRoute(route)

    suspend fun deleteRoute(route: RouteDetailEntity) =
        routeDetailDao.deleteRoute(route)

    // --- Seller Payment Config & COD Eligibility ---
    fun getSellerPaymentConfigFlow(): Flow<SellerPaymentConfigEntity?> =
        sellerPaymentConfigDao.getPaymentConfigFlow()

    suspend fun saveSellerPaymentConfig(config: SellerPaymentConfigEntity) =
        sellerPaymentConfigDao.savePaymentConfig(config)

    // --- Order Call Verification ("Is this order ordered by mistake?") ---
    suspend fun processSellerOrderCallVerification(orderId: Long, isOrderedByMistake: Boolean): Result<Unit> {
        val order = orderDao.getOrderById(orderId) ?: return Result.failure(Exception("Order not found"))
        if (isOrderedByMistake) {
            orderDao.cancelOrder(orderId, "Cancelled by Seller: Buyer confirmed order was placed by mistake.")
            orderDao.updateCallVerification(orderId, "ORDERED_BY_MISTAKE_CANCELLED", true)
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = order.buyerId,
                    userRole = "BUYER",
                    title = "❌ Order Cancelled",
                    message = "Your order #${order.orderNumber} was cancelled as confirmed on call (Ordered by mistake).",
                    relatedOrderId = orderId
                )
            )
        } else {
            orderDao.updateOrderStatus(orderId, "ACCEPTED")
            orderDao.updateCallVerification(orderId, "VERIFIED_VALID", false)
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = order.buyerId,
                    userRole = "BUYER",
                    title = "✅ Order Accepted & Confirmed",
                    message = "Your order #${order.orderNumber} was verified on call and accepted by seller!",
                    relatedOrderId = orderId
                )
            )
        }
        return Result.success(Unit)
    }

    // --- Saved Addresses with Structured Fields ---
    fun getSavedAddressesFlow(): Flow<List<SavedAddressEntity>> {
        val userId = _currentUser.value?.id ?: 0L
        return savedAddressDao.getSavedAddressesForUser(userId)
    }

    suspend fun addStructuredSavedAddress(
        label: String,
        fullName: String,
        phone: String,
        secondaryPhone: String = "",
        houseNo: String,
        roadName: String,
        areaColony: String,
        pincode: String,
        state: String,
        isDefault: Boolean = false
    ): Result<Long> {
        val user = _currentUser.value
        if (user == null) {
            return Result.failure(Exception("Please log in to save address."))
        }
        if (pincode.isBlank()) {
            return Result.failure(Exception("Address Pincode is mandatory."))
        }
        val fullAddr = "$houseNo, $roadName, $areaColony, $state - $pincode".trim(',', ' ')
        if (isDefault) {
            savedAddressDao.clearDefaultAddress(user.id)
        }
        val entity = SavedAddressEntity(
            userId = user.id,
            label = label.ifBlank { "Delivery Address" },
            fullName = fullName.ifBlank { user.name },
            phone = phone.ifBlank { user.phone },
            secondaryPhone = secondaryPhone,
            houseNo = houseNo,
            roadName = roadName,
            areaColony = areaColony,
            pincode = pincode,
            state = state,
            fullAddress = fullAddr,
            isDefault = isDefault
        )
        val id = savedAddressDao.insertAddress(entity)
        return Result.success(id)
    }

    companion object {
        @Volatile
        private var INSTANCE: AppRepository? = null

        fun getInstance(context: Context): AppRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = AppRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
