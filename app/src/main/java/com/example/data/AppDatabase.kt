package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        NotificationEntity::class,
        DirectContactConfigEntity::class,
        DirectCallInquiryEntity::class,
        UserDeviceEntity::class,
        SavedAddressEntity::class,
        NotificationPreferenceEntity::class,
        ChatMessageEntity::class,
        RemoteConfigEntity::class,
        ProductReviewEntity::class,
        RouteDetailEntity::class,
        SellerPaymentConfigEntity::class,
        CategoryPageConfigEntity::class
    ],
    version = 13,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun notificationDao(): NotificationDao
    abstract fun directContactDao(): DirectContactDao
    abstract fun directCallInquiryDao(): DirectCallInquiryDao
    abstract fun userDeviceDao(): UserDeviceDao
    abstract fun savedAddressDao(): SavedAddressDao
    abstract fun notificationPreferenceDao(): NotificationPreferenceDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun remoteConfigDao(): RemoteConfigDao
    abstract fun productReviewDao(): ProductReviewDao
    abstract fun routeDetailDao(): RouteDetailDao
    abstract fun sellerPaymentConfigDao(): SellerPaymentConfigDao
    abstract fun categoryPageConfigDao(): CategoryPageConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daily_order_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(database)
                    }
                }
            }

            private suspend fun seedDatabase(db: AppDatabase) {
                // Seed Users
                val sellerId = db.userDao().insertUser(
                    UserEntity(
                        email = "seller@shop.com",
                        password = "seller123",
                        name = "Vikas Agriculture & Hardware",
                        phone = "+1 (800) 555-FARM",
                        address = "108 Industrial Farm Road, Zone 4, AgriHub",
                        role = "SELLER"
                    )
                )

                val buyerId = db.userDao().insertUser(
                    UserEntity(
                        email = "buyer@shop.com",
                        password = "buyer123",
                        name = "John Miller (Green Acres Farm)",
                        phone = "+1 (555) 432-8901",
                        address = "450 Rural Route 12, Farmstead County",
                        role = "BUYER"
                    )
                )

                val adminId = db.userDao().insertUser(
                    UserEntity(
                        email = "admin@vikas.com",
                        password = "admin123",
                        name = "Vikas Central Admin",
                        phone = "+1 (800) 555-ADMIN",
                        address = "Vikas Corporate HQ, Tech Hub",
                        role = "ADMIN"
                    )
                )

                // Seed Remote Config
                db.remoteConfigDao().saveRemoteConfig(
                    RemoteConfigEntity(
                        id = 1,
                        appName = "Vikas App",
                        enableCod = true,
                        enablePrepaid = true,
                        enableExpressDelivery = true,
                        enableChatSupport = true,
                        enableFlashSaleBanner = true,
                        flashSaleTitle = "🌾 Vikas Maha Sale - Up to 40% OFF!",
                        minOfferDiscountPercent = 5,
                        homepageTheme = "VIKAS_WHITE",
                        topAnnouncement = "🚚 Express local transport delivery available across all hardware tools!"
                    )
                )

                // Seed initial Chat Message
                db.chatMessageDao().insertMessage(
                    ChatMessageEntity(
                        senderId = sellerId,
                        senderName = "Vikas Agri Hardware Seller",
                        senderRole = "SELLER",
                        receiverId = buyerId,
                        message = "Hello! Welcome to Vikas Agri Hardware. How can we help you with your farming tools and hardware today?"
                    )
                )

                // Seed Products (Agriculture Hardware & Farm Supplies)
                val initialProducts = listOf(
                    ProductEntity(
                        name = "Heavy Duty Manual Knapsack Sprayer (16L)",
                        photoUrl = "https://images.unsplash.com/photo-1592417817098-8f3d6eb23659?w=500&auto=format&fit=crop",
                        price = 45.00,
                        availableQuantity = 15,
                        description = "Ergonomic 16-liter backpack sprayer with adjustable brass nozzle, pressure regulator, and stainless steel lance. Ideal for crop spraying, weed control, and pesticides.",
                        category = "Crop Protection",
                        searchKeywords = "छिड़काव पंप, फवारणी, પંપ, Knapsack Sprayer, Fumigador, Pesticide pump, Spray tank",
                        isAvailable = true,
                        isDailySpecial = true
                    ),
                    ProductEntity(
                        name = "Solar Fence Charger Energizer (10 Mile Range)",
                        photoUrl = "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=500&auto=format&fit=crop",
                        price = 129.99,
                        availableQuantity = 8,
                        description = "Weatherproof solar-powered electric fence energizer designed to contain livestock and deter predators. Includes built-in rechargeable battery and mounting bracket.",
                        category = "Fencing & Hardware",
                        searchKeywords = "झटका मशीन, तार फेंसिंग, ઝટકા મશીન, Electric Fence, Cercas, Solar shock machine",
                        isAvailable = true,
                        isDailySpecial = true
                    ),
                    ProductEntity(
                        name = "Drip Irrigation Starter Kit (500 sq ft)",
                        photoUrl = "https://images.unsplash.com/photo-1563514227147-6d2ff665a6a0?w=500&auto=format&fit=crop",
                        price = 89.50,
                        availableQuantity = 12,
                        description = "Complete precision irrigation kit with 1/2\" main hose, micro emitters, pressure regulator, and automatic water timer for farm rows and greenhouse beds.",
                        category = "Irrigation & Pumps",
                        searchKeywords = "ड्रिप सिंचाई, ठिबक सिंचन, ટપક સિંચાઇ, Drip System, Riego por goteo, Water hose",
                        isAvailable = true,
                        isDailySpecial = true
                    ),
                    ProductEntity(
                        name = "Submersible Deep Well Water Pump (1 HP)",
                        photoUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=500&auto=format&fit=crop",
                        price = 210.00,
                        availableQuantity = 6,
                        description = "Heavy-duty stainless steel submersible pump engineered for deep agricultural wells. Delivers high flow rate up to 33 GPM for field irrigation.",
                        category = "Irrigation & Pumps",
                        searchKeywords = "पानी का मोटर पंप, पाण्याच्या मोटर, પંપ, Water Pump, Bomba de agua, Submersible pump",
                        isAvailable = true,
                        isDailySpecial = false
                    ),
                    ProductEntity(
                        name = "Forged Steel Garden Hoe & Cultivator",
                        photoUrl = "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=500&auto=format&fit=crop",
                        price = 24.99,
                        availableQuantity = 25,
                        description = "Dual-head agricultural hand tool crafted from heat-treated carbon steel with ash wood handle. Perfect for soil loosening, weeding, and seedbed preparation.",
                        category = "Tools & Equipment",
                        searchKeywords = "कुदाली, फावड़ा, खुरपा, कुदळ, પાવડો, Kudal, Khurpa, Hoe, Azada, Spade",
                        isAvailable = true,
                        isDailySpecial = false
                    ),
                    ProductEntity(
                        name = "Organic Farming NPK Fertilizer (25kg)",
                        photoUrl = "https://images.unsplash.com/photo-1628352081506-83c43123ed6d?w=500&auto=format&fit=crop",
                        price = 34.50,
                        availableQuantity = 30,
                        description = "Balanced 10-10-10 slow-release organic fertilizer granules enriched with essential trace minerals and micronutrients for crop yield enhancement.",
                        category = "Fertilizers & Soil",
                        searchKeywords = "जैविक खाद, सेंद्रिय खत, ખાતર, Organic Fertilizer, Abono organico, NPK khad",
                        isAvailable = true,
                        isDailySpecial = false
                    ),
                    ProductEntity(
                        name = "Galvanized Barbed Wire Roll (14 Gauge, 1000 ft)",
                        photoUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&auto=format&fit=crop",
                        price = 78.00,
                        availableQuantity = 10,
                        description = "Rust-resistant hot-dipped galvanized 2-point barbed wire roll. High tensile strength suitable for field perimeter fencing and farm security.",
                        category = "Fencing & Hardware",
                        searchKeywords = "कांटेदार तार, काटेरी तार, કાંટાળો તાર, Barbed wire, Alambre de púas, Fence roll",
                        isAvailable = true,
                        isDailySpecial = false
                    ),
                    ProductEntity(
                        name = "Universal Tractor Rotary Tiller Blades (Set of 4)",
                        photoUrl = "https://images.unsplash.com/photo-1530267981375-f0de937f5f13?w=500&auto=format&fit=crop",
                        price = 65.00,
                        availableQuantity = 12,
                        description = "Boron steel hardened tiller replacement blades designed for farm tractors and power tillers. Ensures deep soil penetration and longevity.",
                        category = "Machinery & Parts",
                        searchKeywords = "रोटावेटर ब्लेड, रोटाव्हेटर ब्लेड, Tiller blade, Tractor blade, Rotavator part",
                        isAvailable = true,
                        isDailySpecial = false
                    ),
                    ProductEntity(
                        name = "4-in-1 Soil pH & Moisture Meter Probe",
                        photoUrl = "https://images.unsplash.com/photo-1584467735871-8e85353a8413?w=500&auto=format&fit=crop",
                        price = 19.99,
                        availableQuantity = 20,
                        description = "Digital soil analyzer measures soil pH balance, moisture levels, temperature, and sunlight intensity without batteries.",
                        category = "Tools & Equipment",
                        searchKeywords = "मिट्टी परीक्षण, जमीन चाचणी, Soil tester, Moisture meter, Medidor de suelo",
                        isAvailable = true,
                        isDailySpecial = true
                    ),
                    ProductEntity(
                        name = "High-Yield Hybrid Grain Seed Bag (10kg)",
                        photoUrl = "https://images.unsplash.com/photo-1574943320219-553eb213f72d?w=500&auto=format&fit=crop",
                        price = 42.00,
                        availableQuantity = 18,
                        description = "Certified drought-resistant hybrid crop seeds treated for high germination rate and pest protection.",
                        category = "Seeds & Seedlings",
                        searchKeywords = "हाइब्रिड बीज, बियाणे, બીજ, Crop seeds, Semillas, Hybrid grain",
                        isAvailable = true,
                        isDailySpecial = false
                    )
                )

                for (p in initialProducts) {
                    db.productDao().insertProduct(p)
                }

                // Seed Direct Contact Config
                db.directContactDao().insertOrUpdateConfig(
                    DirectContactConfigEntity(
                        id = 1,
                        agentName = "Vikas Agri Direct Expert - Rajesh Kumar",
                        primaryPhone = "+1 (800) 555-FARM",
                        alternatePhone = "+1 (800) 555-AGRI",
                        workingHours = "8:00 AM - 8:00 PM (Mon-Sat)",
                        specialOffer = "📞 Direct Call Special: Get 5% extra discount & direct stock reservation when ordering on call!",
                        storeAddress = "108 Industrial Farm Road, Zone 4, AgriHub",
                        isAvailableForCalls = true
                    )
                )

                // Initial Seed Notification for seller
                db.notificationDao().insertNotification(
                    NotificationEntity(
                        userId = sellerId,
                        userRole = "SELLER",
                        title = "Welcome to AgriHardware Store!",
                        message = "Your online agriculture hardware store is live. Manage stock, equipment inventory, and orders in your Seller Dashboard."
                    )
                )

                // Initial Seed Notification for buyer
                db.notificationDao().insertNotification(
                    NotificationEntity(
                        userId = buyerId,
                        userRole = "BUYER",
                        title = "Agriculture Hardware Specials Today!",
                        message = "AgriTech Hardware has fresh inventory of Sprayers, Solar Fence Chargers, and Irrigation Kits ready for farm delivery today!"
                    )
                )

                // Seed Route Details
                db.routeDetailDao().insertRoute(
                    RouteDetailEntity(
                        routeName = "NH-48 Western Express Agri Corridor",
                        busNamesCsv = "Vikas Agri Bus, Shree Ram Transport, Express Sleeper",
                        truckNamesCsv = "Eicher 10.95 Heavy, Tata 407 Cargo, Ashok Leyland Container",
                        transportName = "Western State Express Logistics",
                        routeApproach = "Agri Hub Terminal -> District Warehouse -> Farm Delivery Center",
                        farePerLot = 150.0,
                        journeyPincodesCsv = "380001, 380015, 360001, 362001, 382008"
                    )
                )

                // Seed Seller Payment Config
                db.sellerPaymentConfigDao().savePaymentConfig(
                    SellerPaymentConfigEntity(
                        id = 1,
                        eligibleCodPincodesCsv = "380001, 380015, 360001, 362001, 382008",
                        allowOthersContactSellerForCod = true
                    )
                )

                // Seed Initial Reviews
                db.productReviewDao().insertReview(
                    ProductReviewEntity(
                        productId = 1,
                        productName = "Heavy Duty Manual Knapsack Sprayer (16L)",
                        buyerId = buyerId,
                        buyerName = "John Miller (Green Acres)",
                        rating = 5,
                        reviewText = "Excellent build quality! Pressure stays steady for hours, perfect for spraying pesticide on our 5-acre orchard.",
                        isApproved = true
                    )
                )
            }
        }
    }
}
