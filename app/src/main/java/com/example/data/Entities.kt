package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val password: String,
    val name: String,
    val phone: String,
    val secondaryPhone: String = "",
    val address: String,
    val avatarUrl: String = "",
    val role: String, // "SELLER", "BUYER", "ADMIN"
    val isLoginAllowed: Boolean = true
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val photoUrl: String = "",
    val price: Double,
    val mrpPrice: Double = price * 1.25, // MRP price field
    val isMrpEnabled: Boolean = true,    // Seller enable/disable MRP
    val isOfferBadgeEnabled: Boolean = true, // Offer badge enable/disable
    val availableQuantity: Int = 10,
    val description: String = "",
    val category: String = "Tools & Equipment",
    val searchKeywords: String = "",
    val isAvailable: Boolean = true,
    val isDailySpecial: Boolean = false,
    val availabilityMode: String = "LIVE", // "LIVE" or "COMING_SOON"
    val minOrderQuantity: Int = 1,
    val itemsPerLot: Int = 1, // How many items of this product make a lot (one box)
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val buyerId: Long,
    val productId: Long,
    val quantity: Int,
    val isSavedForLater: Boolean = false
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val buyerId: Long,
    val buyerName: String,
    val buyerPhone: String,
    val buyerSecondaryPhone: String = "",
    val deliveryAddress: String,
    val deliveryNotes: String = "",
    val subtotalPrice: Double,
    val deliveryFee: Double = 2.50,
    val totalPrice: Double,
    val status: String = "PENDING", // "PENDING", "ACCEPTED", "SHIPPED", "DELIVERED", "CANCELLED"
    val deliveryService: String = "", // e.g. "Vikas Own Vehicle Service", "Transport (Buses)", "Transport (Truck)", "Transport (Mini Truck)", "Transport (Pickup Loading)"
    val deliveryDetails: String = "", // e.g. Vehicle number, driver phone, transport LR number
    val trackingNumber: String = "",
    val courierName: String = "", // Transport Name
    val transportVehicleName: String = "", // Bus name / Truck name / Vehicle No.
    val trackingStatus: String = "",
    val cancelReason: String = "",
    val paymentMethod: String = "Cash on Delivery", // "Online Prepaid", "Cash on Delivery", "OTHER (Contact Seller)"
    val isLatePaymentAllowed: Boolean = false, // Converted to Special / Late payment by seller
    val latePaymentStatus: String = "NOT_APPLICABLE", // "NOT_APPLICABLE", "PENDING", "PAID"
    val isDirectCallOrder: Boolean = false,
    val directCallAgent: String = "",
    val directCallNotes: String = "",
    val callVerificationStatus: String = "PENDING_CALL", // "PENDING_CALL", "VERIFIED_VALID", "ORDERED_BY_MISTAKE_CANCELLED"
    val isOrderedByMistake: Boolean? = null,
    val totalLots: Int = 1,
    val lotCalculationMode: String = "AUTO", // "AUTO", "BY_SELLER"
    val lotStatus: String = "CALCULATED", // "CALCULATED", "PENDING_SELLER_LOT"
    val deliveryPaymentOption: String = "PAY_INSTANTLY", // "PAY_INSTANTLY", "PAY_TO_TRANSPORT_DIRECTLY", "DISABLED"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "category_page_config")
data class CategoryPageConfigEntity(
    @PrimaryKey val id: Long = 1,
    val heroBannerTitle: String = "🌾 Agriculture & Hardware Marketplace",
    val heroBannerSubtitle: String = "Up to 40% OFF on Top Seeds, Pumps, Sprayers & Tools",
    val heroBannerImageUrl: String = "https://images.unsplash.com/photo-1592982537447-7440770cbfc9?w=800&auto=format&fit=crop",
    val spotlightTitle: String = "🔥 Spotlight Collections",
    val featuredCategoriesCsv: String = "Seeds, Fertilizers, Pesticides, Farm Machinery, Irrigation, Tools, Power Tools, Electrical, Plumbing, Paints, Safety Equipment, Building Materials, Gardening, Livestock Supplies, Spare Parts",
    val promoTagline: String = "Certified Quality • Direct Seller Pricing • Fast Transport Dispatch",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_devices")
data class UserDeviceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val deviceName: String,
    val model: String,
    val ipAddress: String,
    val lastActive: String,
    val isCurrent: Boolean = false
)

@Entity(tableName = "saved_addresses")
data class SavedAddressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val label: String, // e.g., "Home Farm", "Secondary Warehouse"
    val fullName: String,
    val phone: String,
    val secondaryPhone: String = "",
    val houseNo: String = "", // 1. House no./building name
    val roadName: String = "", // 2. Road name
    val areaColony: String = "", // 3. Area/colony
    val pincode: String = "", // 4. Address Pincode (mandatory)
    val state: String = "", // 5. State
    val fullAddress: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "notification_preferences")
data class NotificationPreferenceEntity(
    @PrimaryKey val userId: Long,
    val mahaSellAlerts: Boolean = true,
    val tenPercentOffAlerts: Boolean = true,
    val orderUpdates: Boolean = true,
    val callReminders: Boolean = true
)

@Entity(tableName = "direct_contact_config")
data class DirectContactConfigEntity(
    @PrimaryKey val id: Long = 1,
    val agentName: String = "Vikas Agri Direct Expert - Rajesh Kumar",
    val primaryPhone: String = "+1 (800) 555-FARM",
    val alternatePhone: String = "+1 (800) 555-AGRI",
    val workingHours: String = "8:00 AM - 8:00 PM (Mon-Sat)",
    val specialOffer: String = "📞 Direct Call Special: Get 5% extra discount & stock reservation when ordering on call!",
    val storeAddress: String = "108 Industrial Farm Road, Zone 4, AgriHub",
    val notes: String = "Tell seller agent your requested products and quantity on call. Order will be created in SFCMP.",
    val isAvailableForCalls: Boolean = true
) {
    val phoneNumber: String get() = primaryPhone
    val availableHours: String get() = workingHours
}

@Entity(tableName = "direct_call_inquiries")
data class DirectCallInquiryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val buyerId: Long,
    val buyerName: String,
    val buyerPhone: String,
    val agentName: String = "Vikas Direct Expert",
    val calledNumber: String = "+1 (800) 555-FARM",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING_SELLER_RESPONSE", // "PENDING_SELLER_RESPONSE", "ORDER_CREATED", "NO_ORDER"
    val relatedOrderId: Long? = null,
    val notes: String = ""
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val productPrice: Double,
    val quantity: Int,
    val photoUrl: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val userRole: String, // "SELLER" or "BUYER"
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val relatedOrderId: Long? = null
)

// Data class with populated cart item details for UI
data class CartItemWithProduct(
    val cartItemId: Long,
    val productId: Long,
    val productName: String,
    val photoUrl: String,
    val price: Double,
    val maxQuantity: Int,
    val quantity: Int,
    val isAvailable: Boolean,
    val isSavedForLater: Boolean = false,
    val itemsPerLot: Int = 1
)

// Data class for an Order with its items
data class OrderWithItems(
    val order: OrderEntity,
    val items: List<OrderItemEntity>
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderId: Long,
    val senderName: String,
    val senderRole: String, // "BUYER", "SELLER", "ADMIN"
    val receiverId: Long = 0, // 0 for broadcast / seller desk
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "remote_config")
data class RemoteConfigEntity(
    @PrimaryKey val id: Long = 1,
    val appName: String = "Vikas Agri Hardware",
    val enableCod: Boolean = true,
    val enablePrepaid: Boolean = true,
    val enableExpressDelivery: Boolean = true,
    val enableChatSupport: Boolean = true,
    val enableFlashSaleBanner: Boolean = true,
    val flashSaleTitle: String = "🌾 Vikas Maha Sale - Up to 40% OFF!",
    val minOfferDiscountPercent: Int = 5,
    val homepageTheme: String = "VIKAS_WHITE", // "VIKAS_WHITE"
    val maintenanceMode: Boolean = false,
    val forceUpdateMinVersion: String = "1.0.0",
    val topAnnouncement: String = "🚚 Express local transport delivery available across all hardware tools!",
    val sellerCommissionRatePercent: Double = 2.5,
    val enableSellerTax: Boolean = true,
    val enableDirectSellerPayment: Boolean = true,
    val enablePerformanceReviews: Boolean = true,
    val enableCallVerificationBeforeAccept: Boolean = true, // Admin enable/disable call verification ("Is order ordered by mistake?")
    val deliveryChargeEnabled: Boolean = true, // Admin enable/disable whole delivery charge system
    val payToTransportDirectlyEnabled: Boolean = true, // Admin enable/disable "Pay to transport directly" option
    val defaultFarePerLot: Double = 150.0, // Default fare per lot for lot calculations
    val mandatoryPayToTransport: Boolean = false, // If true, customer mandatory selects pay directly to transport
    val lotCalculationEnabled: Boolean = true, // Admin enable/disable whole lot calculation system
    val deliveryChargeOption: String = "OPTION_1", // "OPTION_1" (Delivery Charge), "OPTION_2" (Delivery Charge_), "DISABLED"
    val lotCalculationEnabledOption1Direct: Boolean = true, // Admin enable/disable lot calculation specifically for Option 1 sub-option 'Pay directly to transport'
    val lotCalculationEnabledOption2: Boolean = true, // Admin enable/disable lot calculation specifically for Option 2 (Delivery Charge_)
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "product_reviews")
data class ProductReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String = "",
    val buyerId: Long,
    val buyerName: String,
    val rating: Int, // 1 to 5
    val reviewText: String,
    val isApproved: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "route_details")
data class RouteDetailEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routeName: String,
    val busNamesCsv: String = "", // Sub-option (I) Bus name
    val truckNamesCsv: String = "", // Sub-option (II) Truck name
    val transportName: String = "",
    val routeApproach: String = "", // Route approach destination
    val farePerLot: Double = 150.0, // Delivery charge per lot of route in ₹
    val journeyPincodesCsv: String = "" // Starting, ending, and in-journey pincodes
)

@Entity(tableName = "seller_payment_config")
data class SellerPaymentConfigEntity(
    @PrimaryKey val id: Long = 1,
    val eligibleCodPincodesCsv: String = "380001, 360001, 362001, 382008",
    val allowOthersContactSellerForCod: Boolean = true
)
