package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import com.example.util.AppLanguage
import com.example.util.LocalizationManager

sealed class Screen {
    object Home : Screen()
    object Catalog : Screen()
    data class ProductDetail(val productId: Long) : Screen()
    object DirectCallContact : Screen() // Direct Phone Call Contact Page for Buyers
    object Cart : Screen()
    object Checkout : Screen()
    data class OrderStatus(val orderId: Long) : Screen()
    object OrderHistory : Screen()
    object SellerDashboard : Screen()
    object SellerContactManagement : Screen() // Seller Direct Contact Management Page (SFCMP)
    object Auth : Screen()
    object Profile : Screen()
    object ContactSupport : Screen()
    object ChatSupport : Screen() // Live Chat Support Page
    object AdminDashboard : Screen() // Server-Driven Complete Admin Control Panel
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = AppRepository.getInstance(application)

    init {
        LocalizationManager.init(application)
    }

    // Current navigation state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _isScreenLoading = MutableStateFlow(false)
    val isScreenLoading: StateFlow<Boolean> = _isScreenLoading.asStateFlow()

    // Language State
    val currentLanguage: StateFlow<AppLanguage> = LocalizationManager.currentLanguage
    val hasSelectedLanguage: StateFlow<Boolean> = LocalizationManager.hasSelectedLanguage

    fun setLanguage(lang: AppLanguage) {
        LocalizationManager.setLanguage(lang, getApplication())
    }

    fun markLanguageSelected() {
        LocalizationManager.markLanguageSelected()
    }

    // Auth state
    val currentUser: StateFlow<UserEntity?> = repository.currentUser

    val isSeller: StateFlow<Boolean> = currentUser.map { it?.role == "SELLER" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isBuyer: StateFlow<Boolean> = currentUser.map { it?.role == "BUYER" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isAdmin: StateFlow<Boolean> = currentUser.map { it?.role == "ADMIN" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Remote Configuration State
    val remoteConfig: StateFlow<RemoteConfigEntity> = repository.remoteConfigFlow
        .map { it ?: RemoteConfigEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RemoteConfigEntity())

    // Chat Messages State
    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.allChatMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Catalog search and filter state
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val availableOnlyFilter = MutableStateFlow(true)

    // All users from DB for Admin management
    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All products from DB
    val allProducts: StateFlow<List<ProductEntity>> = repository.getAllProductsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered products for buyer view
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        repository.getAllProductsFlow(),
        searchQuery,
        selectedCategory,
        availableOnlyFilter
    ) { products, query, category, availableOnly ->
        val cleanQuery = query.trim().lowercase().removePrefix("#")
        products.filter { p ->
            val cleanKeywords = p.searchKeywords.lowercase().replace("#", " ")
            val matchesQuery = query.isBlank() || 
                p.name.contains(query, ignoreCase = true) || 
                p.description.contains(query, ignoreCase = true) ||
                p.category.contains(query, ignoreCase = true) ||
                p.searchKeywords.contains(query, ignoreCase = true) ||
                (cleanQuery.isNotBlank() && cleanKeywords.contains(cleanQuery))
            val matchesCategory = category == "All" || p.category.equals(category, ignoreCase = true)
            val matchesAvailability = !availableOnly || (p.isAvailable && p.availableQuantity > 0)
            matchesQuery && matchesCategory && matchesAvailability
        }
    }.flowOn(Dispatchers.Default)
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart State
    val cartItems: StateFlow<List<CartItemWithProduct>> = currentUser.flatMapLatest { user ->
        if (user != null && user.role == "BUYER") {
            repository.getCartWithDetailsFlow(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCartItems: StateFlow<List<CartItemWithProduct>> = cartItems.map { list -> list.filter { !it.isSavedForLater } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedForLaterItems: StateFlow<List<CartItemWithProduct>> = cartItems.map { list -> list.filter { it.isSavedForLater } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItemCount: StateFlow<Int> = activeCartItems.map { items -> items.sumOf { it.quantity } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartSubtotal: StateFlow<Double> = activeCartItems.map { items -> items.sumOf { it.price * it.quantity } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // User Devices State
    val userDevices: StateFlow<List<UserDeviceEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) {
            viewModelScope.launch(Dispatchers.IO) { repository.addDefaultUserDevicesIfEmpty(user.id) }
            repository.getDevicesForUser(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Saved Addresses State
    val savedAddresses: StateFlow<List<SavedAddressEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getSavedAddressesForUser(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notification Preferences State
    val notificationPreferences: StateFlow<NotificationPreferenceEntity?> = currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getNotificationPreferencesFlow(user.id)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Orders State
    val sellerOrders: StateFlow<List<OrderEntity>> = repository.getAllOrdersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val buyerOrders: StateFlow<List<OrderEntity>> = currentUser.flatMapLatest { user ->
        if (user != null && user.role == "BUYER") {
            repository.getOrdersForBuyerFlow(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications State
    val notifications: StateFlow<List<NotificationEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getNotificationsFlow(user.id, user.role)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationCount: StateFlow<Int> = notifications.map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Direct Contact Config & Inquiries State
    val directContactConfig: StateFlow<DirectContactConfigEntity> = repository.directContactConfigFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DirectContactConfigEntity())

    val allDirectCallInquiries: StateFlow<List<DirectCallInquiryEntity>> = repository.allDirectCallInquiriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingDirectCallInquiries: StateFlow<List<DirectCallInquiryEntity>> = repository.pendingDirectCallInquiriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val buyerCallInquiries: StateFlow<List<DirectCallInquiryEntity>> = currentUser.flatMapLatest { user ->
        if (user != null && user.role == "BUYER") {
            repository.getInquiriesForBuyerFlow(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Feedback Message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        // Auto-login buyer session by default for instant out-of-the-box browsing
        viewModelScope.launch(Dispatchers.IO) {
            repository.autoLoginDefaultBuyer()
        }
    }

    fun navigateTo(screen: Screen, showLoading: Boolean = true) {
        if (_currentScreen.value == screen) return
        if (showLoading) {
            viewModelScope.launch {
                _isScreenLoading.value = true
                _currentScreen.value = screen
                delay(220) // Smart brief delay for smooth page loading animation
                _isScreenLoading.value = false
            }
        } else {
            _currentScreen.value = screen
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    // --- Authentication Actions ---
    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = repository.login(email, pass)
            res.onSuccess { user ->
                showMessage("Welcome back, ${user.name}!")
                when (user.role.uppercase()) {
                    "SELLER" -> _currentScreen.value = Screen.SellerDashboard
                    "ADMIN" -> _currentScreen.value = Screen.AdminDashboard
                    else -> _currentScreen.value = Screen.Home
                }
                onSuccess()
            }.onFailure { err ->
                showMessage(err.message ?: "Login failed")
            }
        }
    }

    fun quickLoginSeller() {
        viewModelScope.launch {
            val seller = repository.autoLoginDefaultSeller()
            if (seller != null) {
                showMessage("Logged in as Seller: ${seller.name}")
                _currentScreen.value = Screen.SellerDashboard
            }
        }
    }

    fun quickLoginBuyer() {
        viewModelScope.launch {
            val buyer = repository.autoLoginDefaultBuyer()
            if (buyer != null) {
                showMessage("Logged in as Buyer: ${buyer.name}")
                _currentScreen.value = Screen.Home
            }
        }
    }

    fun registerBuyer(
        name: String,
        email: String,
        pass: String,
        phone: String,
        address: String,
        onSuccess: () -> Unit
    ) {
        registerUser(name, email, pass, phone, address, "BUYER", onSuccess)
    }

    fun registerUser(
        name: String,
        email: String,
        pass: String,
        phone: String,
        address: String,
        role: String = "BUYER",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.registerUser(name, email, pass, phone, address, role)
            res.onSuccess { user ->
                showMessage("Account created! Welcome, ${user.name}.")
                when (user.role.uppercase()) {
                    "SELLER" -> _currentScreen.value = Screen.SellerDashboard
                    "ADMIN" -> _currentScreen.value = Screen.AdminDashboard
                    else -> _currentScreen.value = Screen.Home
                }
                onSuccess()
            }.onFailure { err ->
                showMessage(err.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        repository.logout()
        showMessage("Logged out successfully.")
        _currentScreen.value = Screen.Home
    }

    fun updateUserProfile(name: String, phone: String, secondaryPhone: String = "", address: String) {
        viewModelScope.launch {
            repository.updateUserProfile(name, phone, secondaryPhone, address)
            showMessage("Profile & Contact details updated successfully.")
        }
    }

    // --- Device Management ---
    fun revokeDevice(deviceId: Long) {
        viewModelScope.launch {
            repository.revokeDevice(deviceId)
            showMessage("Device session revoked successfully.")
        }
    }

    // --- Saved Address Actions ---
    fun saveAddress(label: String, fullName: String, phone: String, secondaryPhone: String, fullAddress: String, isDefault: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val address = SavedAddressEntity(
                userId = user.id,
                label = label.ifBlank { "Delivery Address" },
                fullName = fullName,
                phone = phone,
                secondaryPhone = secondaryPhone,
                fullAddress = fullAddress,
                isDefault = isDefault
            )
            repository.saveAddress(address)
            showMessage("Address saved successfully!")
        }
    }

    fun deleteSavedAddress(addressId: Long) {
        viewModelScope.launch {
            repository.deleteSavedAddress(addressId)
            showMessage("Saved address deleted.")
        }
    }

    fun setDefaultAddress(addressId: Long) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.setDefaultAddress(addressId, user.id)
            showMessage("Default delivery address set.")
        }
    }

    // --- Notification Preference Actions ---
    fun updateNotificationPreferences(
        mahaSell: Boolean,
        tenPercentOff: Boolean,
        orderUpdates: Boolean,
        callReminders: Boolean
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val pref = NotificationPreferenceEntity(
                userId = user.id,
                mahaSellAlerts = mahaSell,
                tenPercentOffAlerts = tenPercentOff,
                orderUpdates = orderUpdates,
                callReminders = callReminders
            )
            repository.updateNotificationPreferences(pref)
            showMessage("Notification preferences saved.")
        }
    }

    // --- Cart Actions ---
    fun addToCart(productId: Long, quantity: Int = 1) {
        val user = currentUser.value
        if (user == null || user.role != "BUYER") {
            _currentScreen.value = Screen.Auth
            showMessage("Please log in or sign up as a buyer to place orders.")
            return
        }

        viewModelScope.launch {
            val res = repository.addToCart(user.id, productId, quantity)
            res.onSuccess {
                showMessage("Item added to cart!")
            }.onFailure { err ->
                showMessage(err.message ?: "Could not add item to cart")
            }
        }
    }

    fun updateCartQuantity(cartItem: CartItemWithProduct, newQty: Int) {
        viewModelScope.launch {
            repository.updateCartItemQuantity(cartItem, newQty)
        }
    }

    fun removeCartItem(cartItemId: Long) {
        viewModelScope.launch {
            repository.removeCartItem(cartItemId)
            showMessage("Item removed from cart.")
        }
    }

    fun toggleSaveForLater(cartItemId: Long, isSaved: Boolean) {
        viewModelScope.launch {
            repository.toggleSaveForLater(cartItemId, isSaved)
            if (isSaved) showMessage("Item saved for later!") else showMessage("Item moved back to cart.")
        }
    }

    // --- Checkout & Orders ---
    val categoryPageConfig: StateFlow<CategoryPageConfigEntity> = repository.getCategoryPageConfigFlow()
        .map { it ?: CategoryPageConfigEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoryPageConfigEntity())

    fun saveCategoryPageConfig(config: CategoryPageConfigEntity) {
        viewModelScope.launch {
            repository.saveCategoryPageConfig(config)
            showMessage("Category Page settings updated successfully!")
        }
    }

    fun submitOrder(
        buyerName: String,
        buyerPhone: String,
        buyerSecondaryPhone: String = "",
        deliveryAddress: String,
        deliveryNotes: String,
        paymentMethod: String,
        calculatedDeliveryFee: Double = 0.0,
        calculatedTotalLots: Int = 1,
        lotCalculationMode: String = "AUTO",
        deliveryPaymentOption: String = "PAY_INSTANTLY",
        onOrderSuccess: (Long) -> Unit
    ) {
        val user = currentUser.value
        if (user == null || user.role != "BUYER") {
            showMessage("Please log in to place an order.")
            return
        }

        val items = activeCartItems.value
        if (items.isEmpty()) {
            showMessage("Your cart is empty.")
            return
        }

        val notesWithSecPhone = if (buyerSecondaryPhone.isNotBlank()) {
            "$deliveryNotes (Alt Phone: $buyerSecondaryPhone)".trim()
        } else deliveryNotes

        viewModelScope.launch {
            val result = repository.placeOrder(
                buyerId = user.id,
                buyerName = buyerName,
                buyerPhone = buyerPhone,
                deliveryAddress = deliveryAddress,
                deliveryNotes = notesWithSecPhone,
                paymentMethod = paymentMethod,
                cartItems = items,
                calculatedDeliveryFee = calculatedDeliveryFee,
                calculatedTotalLots = calculatedTotalLots,
                lotCalculationMode = lotCalculationMode,
                deliveryPaymentOption = deliveryPaymentOption
            )

            result.onSuccess { orderId ->
                showMessage("Order placed successfully!")
                _currentScreen.value = Screen.OrderStatus(orderId)
                onOrderSuccess(orderId)
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to place order.")
            }
        }
    }

    fun updateOrderTracking(
        orderId: Long,
        trackingNumber: String = "",
        courierName: String = "",
        transportVehicleName: String = "",
        trackingStatus: String = ""
    ) {
        viewModelScope.launch {
            val res = repository.updateOrderTracking(orderId, trackingNumber, courierName, transportVehicleName, trackingStatus)
            res.onSuccess {
                showMessage("Tracking information updated!")
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to update tracking")
            }
        }
    }

    fun setOrderLatePaymentAllowed(orderId: Long, isAllowed: Boolean) {
        viewModelScope.launch {
            val res = repository.setOrderLatePaymentAllowed(orderId, isAllowed)
            res.onSuccess {
                if (isAllowed) showMessage("Order converted to Special/Late Payment!") else showMessage("Special/Late Payment cancelled.")
            }
        }
    }

    fun sellerUpdateOrderLotCount(orderId: Long, lots: Int, farePerLot: Double = 150.0) {
        viewModelScope.launch {
            val res = repository.sellerUpdateOrderLotCount(orderId, lots, farePerLot)
            res.onSuccess {
                showMessage("Lots updated to $lots and delivery fee recalculated.")
            }
        }
    }

    fun cancelOrder(orderId: Long, reason: String = "Cancelled by buyer prior to shipping") {
        viewModelScope.launch {
            val res = repository.cancelOrder(orderId, reason)
            res.onSuccess {
                showMessage("Order cancelled successfully prior to shipping.")
            }.onFailure { err ->
                showMessage(err.message ?: "Order cancellation failed.")
            }
        }
    }

    // --- Seller Dashboard Actions ---
    fun addProductEntity(product: ProductEntity) {
        viewModelScope.launch {
            val defaultPhoto = product.photoUrl.ifBlank { "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500&auto=format&fit=crop" }
            val entityToInsert = product.copy(
                photoUrl = defaultPhoto,
                isAvailable = product.availableQuantity > 0
            )
            repository.addProduct(entityToInsert)
            showMessage("Product '${product.name}' saved to catalog!")
        }
    }

    fun addProduct(
        name: String,
        photoUrl: String,
        price: Double,
        quantity: Int,
        description: String,
        category: String,
        searchKeywords: String = "",
        isDailySpecial: Boolean
    ) {
        viewModelScope.launch {
            val defaultPhoto = photoUrl.ifBlank { "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500&auto=format&fit=crop" }
            val newProduct = ProductEntity(
                name = name.trim(),
                photoUrl = defaultPhoto,
                price = price,
                availableQuantity = quantity,
                description = description.trim(),
                category = category,
                searchKeywords = searchKeywords.trim(),
                isAvailable = quantity > 0,
                isDailySpecial = isDailySpecial
            )
            repository.addProduct(newProduct)
            showMessage("New product added with search keywords!")
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
            showMessage("Product '${product.name}' updated!")
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            showMessage("Product deleted.")
        }
    }

    fun toggleProductAvailability(id: Long, isAvailable: Boolean) {
        viewModelScope.launch {
            repository.toggleProductAvailability(id, isAvailable)
            val statusStr = if (isAvailable) "Available Today" else "Unavailable"
            showMessage("Item availability changed to $statusStr")
        }
    }

    fun updateProductStock(id: Long, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateProductQuantity(id, newQuantity)
            showMessage("Stock quantity updated to $newQuantity")
        }
    }

    fun updateOrderStatus(
        orderId: Long,
        newStatus: String,
        deliveryService: String = "",
        deliveryDetails: String = ""
    ) {
        viewModelScope.launch {
            val res = repository.updateOrderStatus(orderId, newStatus, deliveryService, deliveryDetails)
            res.onSuccess {
                showMessage("Order status updated to $newStatus")
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to update status")
            }
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(user.id, user.role)
        }
    }

    fun sendSupportInquiry(message: String, onSent: () -> Unit) {
        val user = currentUser.value
        viewModelScope.launch {
            val name = user?.name ?: "Guest Buyer"
            repository.sendSupportInquiry(user?.id ?: 0, name, message)
            showMessage("Inquiry sent to seller! We will contact you soon.")
            onSent()
        }
    }

    // --- Direct Contact & SFCMP Actions ---
    fun saveDirectContactConfig(config: DirectContactConfigEntity) {
        viewModelScope.launch {
            repository.saveDirectContactConfig(config)
            showMessage("Seller direct phone contact settings saved!")
        }
    }

    fun recordDirectCallInquiry(agentName: String, calledNumber: String, onRecorded: () -> Unit = {}) {
        val user = currentUser.value
        val buyerId = user?.id ?: 0
        val buyerName = user?.name ?: "Guest Call Buyer"
        val buyerPhone = user?.phone ?: "Direct Call"

        viewModelScope.launch {
            repository.recordDirectCallInquiry(
                buyerId = buyerId,
                buyerName = buyerName,
                buyerPhone = buyerPhone,
                agentName = agentName,
                calledNumber = calledNumber
            )
            showMessage("Call notification sent to seller! Seller will confirm your order in SFCMP.")
            onRecorded()
        }
    }

    fun respondToDirectCallInquiry(inquiryId: Long, isOrderPlaced: Boolean) {
        viewModelScope.launch {
            repository.respondToDirectCallInquiry(inquiryId, isOrderPlaced)
            if (!isOrderPlaced) {
                showMessage("Inquiry updated: Marked as Call Only (No Order).")
            }
        }
    }

    fun createDirectCallOrderForBuyer(
        inquiryId: Long?,
        buyerId: Long,
        buyerName: String,
        buyerPhone: String,
        deliveryAddress: String,
        deliveryNotes: String,
        productId: Long,
        quantity: Int,
        agentName: String,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.createDirectCallOrderForBuyer(
                inquiryId = inquiryId,
                buyerId = buyerId,
                buyerName = buyerName,
                buyerPhone = buyerPhone,
                deliveryAddress = deliveryAddress,
                deliveryNotes = deliveryNotes,
                productId = productId,
                quantity = quantity,
                agentName = agentName
            )

            result.onSuccess { orderId ->
                showMessage("Direct Phone Order successfully created for $buyerName!")
                onSuccess(orderId)
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to create direct call order.")
            }
        }
    }

    // --- Chat Support Actions ---
    fun sendChatMessage(text: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.sendChatMessage(
                senderId = user.id,
                senderName = user.name,
                senderRole = user.role,
                message = text
            )
        }
    }

    // --- Remote Config Admin Actions ---
    fun saveRemoteConfig(config: RemoteConfigEntity) {
        viewModelScope.launch {
            repository.updateRemoteConfig(config)
            showMessage("⚡ App Remote Config updated! Changes applied instantly across all devices.")
        }
    }

    fun updateUser(user: UserEntity) {
        viewModelScope.launch {
            repository.updateUser(user)
            showMessage("User details & login material updated!")
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            repository.deleteUser(userId)
            showMessage("User account deleted.")
        }
    }

    fun quickLoginAdmin() {
        login("admin@vikas.com", "admin123") {
            _currentScreen.value = Screen.AdminDashboard
        }
    }

    // --- SFCMP Multi-Item Order Creation ---
    fun createMultiItemSfcmpOrder(
        buyerName: String,
        buyerPhone: String,
        deliveryAddress: String,
        deliveryNotes: String,
        selectedItemsWithQty: List<Pair<ProductEntity, Int>>,
        agentName: String,
        inquiryId: Long?,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.createMultiItemSfcmpOrder(
                buyerName = buyerName,
                buyerPhone = buyerPhone,
                deliveryAddress = deliveryAddress,
                deliveryNotes = deliveryNotes,
                selectedItemsWithQty = selectedItemsWithQty,
                agentName = agentName,
                inquiryId = inquiryId
            )

            res.onSuccess { orderId ->
                showMessage("SFCMP Multi-Item Order created successfully!")
                onSuccess(orderId)
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to create SFCMP order.")
            }
        }
    }

    // --- Transport Routes / Delivery Details ---
    val allRoutes: StateFlow<List<RouteDetailEntity>> = repository.getAllRoutesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addOrUpdateRoute(route: RouteDetailEntity) {
        viewModelScope.launch {
            repository.addOrUpdateRoute(route)
            showMessage("Route '${route.routeName}' saved in Transport Details!")
        }
    }

    fun deleteRoute(route: RouteDetailEntity) {
        viewModelScope.launch {
            repository.deleteRoute(route)
            showMessage("Route deleted.")
        }
    }

    // --- Product Reviews ---
    val allReviews: StateFlow<List<ProductReviewEntity>> = repository.getAllReviewsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getReviewsForProductFlow(productId: Long): Flow<List<ProductReviewEntity>> =
        repository.getReviewsForProductFlow(productId)

    fun addProductReview(productId: Long, productName: String, rating: Int, reviewText: String) {
        viewModelScope.launch {
            val res = repository.addProductReview(productId, productName, rating, reviewText)
            res.onSuccess {
                showMessage("Thank you! Your product review has been submitted.")
            }.onFailure { err ->
                showMessage(err.message ?: "Could not post review.")
            }
        }
    }

    fun updateReviewApproval(reviewId: Long, isApproved: Boolean) {
        viewModelScope.launch {
            repository.updateReviewApproval(reviewId, isApproved)
            showMessage("Review approval status updated.")
        }
    }

    fun deleteReview(reviewId: Long) {
        viewModelScope.launch {
            repository.deleteReview(reviewId)
            showMessage("Review deleted.")
        }
    }

    // --- Seller Payment Method & COD Management ---
    val sellerPaymentConfig: StateFlow<SellerPaymentConfigEntity> = repository.getSellerPaymentConfigFlow()
        .map { it ?: SellerPaymentConfigEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SellerPaymentConfigEntity())

    fun saveSellerPaymentConfig(config: SellerPaymentConfigEntity) {
        viewModelScope.launch {
            repository.saveSellerPaymentConfig(config)
            showMessage("Seller Payment & COD settings saved!")
        }
    }

    // --- Seller Order Call Verification ("Is this order ordered by mistake?") ---
    fun processSellerOrderCallVerification(orderId: Long, isOrderedByMistake: Boolean) {
        viewModelScope.launch {
            val res = repository.processSellerOrderCallVerification(orderId, isOrderedByMistake)
            res.onSuccess {
                if (isOrderedByMistake) {
                    showMessage("Order marked as 'Ordered by Mistake' & automatically cancelled.")
                } else {
                    showMessage("Order verified on call and ACCEPTED!")
                }
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to process call verification.")
            }
        }
    }

    // --- Saved Addresses ---
    fun addStructuredAddress(
        label: String,
        fullName: String,
        phone: String,
        secondaryPhone: String,
        houseNo: String,
        roadName: String,
        areaColony: String,
        pincode: String,
        state: String,
        isDefault: Boolean
    ) {
        viewModelScope.launch {
            val res = repository.addStructuredSavedAddress(
                label = label,
                fullName = fullName,
                phone = phone,
                secondaryPhone = secondaryPhone,
                houseNo = houseNo,
                roadName = roadName,
                areaColony = areaColony,
                pincode = pincode,
                state = state,
                isDefault = isDefault
            )
            res.onSuccess {
                showMessage("New delivery address saved!")
            }.onFailure { err ->
                showMessage(err.message ?: "Could not save address.")
            }
        }
    }
}
