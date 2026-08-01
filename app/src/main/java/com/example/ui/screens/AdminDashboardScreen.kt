package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProductEntity
import com.example.data.ProductReviewEntity
import com.example.data.RemoteConfigEntity
import com.example.data.RouteDetailEntity
import com.example.ui.theme.AmberAccent
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    onNavigate: (Screen) -> Unit
) {
    val isAdmin by viewModel.isAdmin.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val remoteConfig by viewModel.remoteConfig.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val sellerOrders by viewModel.sellerOrders.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Remote Config & System, 1: Product Offers & MRP, 2: User Roles, 3: Orders Overview

    var showCreateAdminDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var editingProductOffer by remember { mutableStateOf<ProductEntity?>(null) }

    if (!isAdmin) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Central Admin Control Panel",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Please log in as a Central App Admin to access system settings, seller controls, and user management.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.quickLoginAdmin() },
                modifier = Modifier.testTag("admin_quick_login_btn")
            ) {
                Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log In as Central Admin Demo")
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { onNavigate(Screen.Auth) },
                modifier = Modifier.testTag("admin_custom_login_btn")
            ) {
                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log In with Email & Password")
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = { onNavigate(Screen.Home) }
            ) {
                Text("Return to Buyer Storefront")
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar for Admin (Vikas Style with Menu & Logout)
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.testTag("admin_menu_trigger_btn")
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Admin Menu",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.width(260.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("1. Central Admin Home", fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = { menuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("2. Buyer Storefront View", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigate(Screen.Home)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("3. Seller Hub Dashboard", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.Dashboard, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigate(Screen.SellerDashboard)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("4. Direct Call Desk (SFCMP)", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.ContactPhone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigate(Screen.SellerContactManagement)
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("5. Log Out Admin", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuExpanded = false
                                    showLogoutConfirmDialog = true
                                },
                                modifier = Modifier.testTag("admin_menu_logout_item")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Central Admin Control",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "SUPER ADMIN",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Server-Driven App & System Control",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = { showCreateAdminDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("admin_add_new_admin_btn")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Admin", fontSize = 11.sp)
                    }

                    // Direct Top Header Logout Icon Button
                    IconButton(
                        onClick = { showLogoutConfirmDialog = true },
                        modifier = Modifier.testTag("admin_top_bar_logout_btn")
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Log Out Admin",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Admin User Profile & Quick Stats Card (Like Buyer & Seller Panel)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Profile Avatar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = currentUser?.name ?: "System Administrator",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = currentUser?.email ?: "admin@vikas.com",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            if (!currentUser?.phone.isNullOrBlank()) {
                                Text(
                                    text = "📞 ${currentUser?.phone}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Prominent Logout Button in User Header Card
                    OutlinedButton(
                        onClick = { showLogoutConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("admin_profile_card_logout_btn")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                // Admin Stats Summary Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Users", fontSize = 10.sp, color = Color.Gray)
                        Text("${allUsers.size}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Sellers", fontSize = 10.sp, color = Color.Gray)
                        Text("${allUsers.count { it.role.equals("SELLER", ignoreCase = true) }}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Orders", fontSize = 10.sp, color = Color.Gray)
                        Text("${sellerOrders.size}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberAccent)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("System Config", fontSize = 10.sp, color = Color.Gray)
                        Text("Active", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                    }
                }
            }
        }

        // Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            edgePadding = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                Text("Remote Config", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                Text("Transport & Delivery Details", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) {
                Text("Product Reviews", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = activeTab == 3, onClick = { activeTab = 3 }) {
                Text("MRP & Offers", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = activeTab == 4, onClick = { activeTab = 4 }) {
                Text("Sellers & Buyers", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = activeTab == 5, onClick = { activeTab = 5 }) {
                Text("All Orders (${sellerOrders.size})", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        when (activeTab) {
            0 -> RemoteConfigTab(remoteConfig = remoteConfig, onSaveConfig = { viewModel.saveRemoteConfig(it) })
            1 -> AdminTransportDetailsTab(viewModel = viewModel)
            2 -> AdminProductReviewsTab(viewModel = viewModel)
            3 -> AdminOfferMrpTab(products = allProducts, onEditOffer = { editingProductOffer = it })
            4 -> AdminUserAndSellerManagementTab(viewModel = viewModel, onAddAdminClick = { showCreateAdminDialog = false })
            5 -> AdminOrdersTab(orders = sellerOrders)
        }
    }

    if (showCreateAdminDialog) {
        CreateAdminDialog(
            onDismiss = { showCreateAdminDialog = false },
            onCreate = { email, name, phone, pass ->
                viewModel.registerUser(name, email, pass, phone, "HQ Address", "ADMIN") {
                    showCreateAdminDialog = false
                }
            }
        )
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Log Out from Admin Panel?") },
            text = { Text("Are you sure you want to log out of Central Admin Control Panel?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.logout()
                        onNavigate(Screen.Auth)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    editingProductOffer?.let { product ->
        AdminOfferEditDialog(
            product = product,
            onDismiss = { editingProductOffer = null },
            onSaveOffer = { newMrp, mrpEnabled, showOfferTag ->
                val updated = product.copy(
                    mrpPrice = newMrp,
                    isMrpEnabled = mrpEnabled,
                    isOfferBadgeEnabled = showOfferTag
                )
                viewModel.updateProduct(updated)
                editingProductOffer = null
            }
        )
    }
}

@Composable
fun RemoteConfigTab(
    remoteConfig: RemoteConfigEntity?,
    onSaveConfig: (RemoteConfigEntity) -> Unit
) {
    var bannerText by remember(remoteConfig) { mutableStateOf(remoteConfig?.flashSaleTitle ?: "🌾 Vikas Maha Sale - Up to 40% OFF!") }
    var isBannerEnabled by remember(remoteConfig) { mutableStateOf(remoteConfig?.enableFlashSaleBanner ?: true) }
    var isExpressDelivery by remember(remoteConfig) { mutableStateOf(remoteConfig?.enableExpressDelivery ?: true) }
    var minDiscountForOfferTag by remember(remoteConfig) { mutableStateOf((remoteConfig?.minOfferDiscountPercent ?: 5).toString()) }
    var topAnnouncement by remember(remoteConfig) { mutableStateOf(remoteConfig?.topAnnouncement ?: "🚚 Express local transport delivery available!") }

    var enableSellerTax by remember(remoteConfig) { mutableStateOf(remoteConfig?.enableSellerTax ?: true) }
    var enableDirectSellerPayment by remember(remoteConfig) { mutableStateOf(remoteConfig?.enableDirectSellerPayment ?: true) }
    var enablePerformanceReviews by remember(remoteConfig) { mutableStateOf(remoteConfig?.enablePerformanceReviews ?: true) }
    var enableCallVerificationBeforeAccept by remember(remoteConfig) { mutableStateOf(remoteConfig?.enableCallVerificationBeforeAccept ?: true) }

    var deliveryChargeOption by remember(remoteConfig) {
        mutableStateOf(
            remoteConfig?.deliveryChargeOption ?: if (remoteConfig?.deliveryChargeEnabled == false) "DISABLED" else if (remoteConfig?.mandatoryPayToTransport == true) "OPTION_2" else "OPTION_1"
        )
    }
    var lotCalculationEnabledOption1Direct by remember(remoteConfig) { mutableStateOf(remoteConfig?.lotCalculationEnabledOption1Direct ?: true) }
    var lotCalculationEnabledOption2 by remember(remoteConfig) { mutableStateOf(remoteConfig?.lotCalculationEnabledOption2 ?: true) }
    var defaultFarePerLotStr by remember(remoteConfig) { mutableStateOf((remoteConfig?.defaultFarePerLot ?: 150.0).toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Server-Driven Remote Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Changes made here instantly configure all buyer and seller panels without app store updates.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Global Announcement Banner", fontWeight = FontWeight.SemiBold)
                    Switch(checked = isBannerEnabled, onCheckedChange = { isBannerEnabled = it })
                }

                OutlinedTextField(
                    value = bannerText,
                    onValueChange = { bannerText = it },
                    label = { Text("Global Banner Title Text") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Express Delivery Fleet Option", fontWeight = FontWeight.SemiBold)
                    Switch(checked = isExpressDelivery, onCheckedChange = { isExpressDelivery = it })
                }

                HorizontalDivider()

                // Admin Controls for Tax, Direct Payment & Reviews
                Text("Admin Controls & Seller Privileges", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Tax Option for Seller", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Toggle tax calculation on seller invoices", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(checked = enableSellerTax, onCheckedChange = { enableSellerTax = it }, modifier = Modifier.testTag("admin_tax_switch"))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Direct Payment to Seller", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Allow direct bank payouts to seller accounts", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(checked = enableDirectSellerPayment, onCheckedChange = { enableDirectSellerPayment = it }, modifier = Modifier.testTag("admin_direct_payment_switch"))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Performance & Review Option", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Buyer ratings & reviews functionality", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(checked = enablePerformanceReviews, onCheckedChange = { enablePerformanceReviews = it }, modifier = Modifier.testTag("admin_reviews_switch"))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Seller Order Call Verification", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Require seller call confirmation ('Is this order ordered by mistake?') before accepting order", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = enableCallVerificationBeforeAccept,
                        onCheckedChange = { enableCallVerificationBeforeAccept = it },
                        modifier = Modifier.testTag("admin_call_verification_switch")
                    )
                }

                HorizontalDivider()

                // Delivery Charge & Transport Payment Admin Controls
                Text("🚚 Delivery Charge System Controls", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Select active delivery charge mode (only one enabled at a time):", fontSize = 11.sp, color = Color.Gray)

                // Option 1 Radio Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (deliveryChargeOption == "OPTION_1") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (deliveryChargeOption == "OPTION_1") MaterialTheme.colorScheme.primary else Color.LightGray),
                    modifier = Modifier.fillMaxWidth().clickable { deliveryChargeOption = "OPTION_1" }
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = deliveryChargeOption == "OPTION_1",
                            onClick = { deliveryChargeOption = "OPTION_1" },
                            modifier = Modifier.testTag("admin_delivery_option_1_radio")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("1. Delivery Charge (Option 1)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Sub-parts: 'Pay delivery charge direct to transport' & 'Pay instantly' (online). For COD, pay at delivery. For Late payment, auto-selects direct to transport.", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }

                // Option 2 Radio Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (deliveryChargeOption == "OPTION_2") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (deliveryChargeOption == "OPTION_2") MaterialTheme.colorScheme.primary else Color.LightGray),
                    modifier = Modifier.fillMaxWidth().clickable { deliveryChargeOption = "OPTION_2" }
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = deliveryChargeOption == "OPTION_2",
                            onClick = { deliveryChargeOption = "OPTION_2" },
                            modifier = Modifier.testTag("admin_delivery_option_2_radio")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("2. Delivery Charge_ (Option 2 - Mandatory Direct Transport)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Mandatory instruction: 'Pay delivery charge direct to transport'. Not added to upfront total payment. Buyer pays transport directly at delivery time.", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }

                // Disabled Radio Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (deliveryChargeOption == "DISABLED") MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (deliveryChargeOption == "DISABLED") MaterialTheme.colorScheme.error else Color.LightGray),
                    modifier = Modifier.fillMaxWidth().clickable { deliveryChargeOption = "DISABLED" }
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = deliveryChargeOption == "DISABLED",
                            onClick = { deliveryChargeOption = "DISABLED" },
                            modifier = Modifier.testTag("admin_delivery_disabled_radio")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("3. Disable Delivery Charge System", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Completely disables delivery charges for all buyer orders.", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }

                // Lot Calculation Switch for Option 1 Sub-option 'Pay directly to transport'
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Lot Calculation for Option 1 (Sub-option 'Pay directly to transport')", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("If OFF, lot calculation is bypassed when buyer selects 'Pay delivery charge direct to transport' in Option 1. Shows message that lot info is given by seller at dispatch time.", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = lotCalculationEnabledOption1Direct,
                        onCheckedChange = { lotCalculationEnabledOption1Direct = it },
                        modifier = Modifier.testTag("admin_lot_calculation_option1_direct_switch")
                    )
                }

                // Whole Lot Calculation Switch for Option 2
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Whole Lot Calculation System for Option 2", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("If OFF, lot calculation is bypassed for Option 2 and buyer is informed that lot info is given by seller at dispatch time.", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = lotCalculationEnabledOption2,
                        onCheckedChange = { lotCalculationEnabledOption2 = it },
                        modifier = Modifier.testTag("admin_lot_calculation_option2_switch")
                    )
                }

                OutlinedTextField(
                    value = defaultFarePerLotStr,
                    onValueChange = { defaultFarePerLotStr = it },
                    label = { Text("Default Route Fare Per Lot (₹) (Default: 150)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                HorizontalDivider()

                OutlinedTextField(
                    value = minDiscountForOfferTag,
                    onValueChange = { minDiscountForOfferTag = it },
                    label = { Text("Min Discount % for Offer Tag Display (Default 5%)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = topAnnouncement,
                    onValueChange = { topAnnouncement = it },
                    label = { Text("Top Header Announcement") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = {
                        val isOption2 = deliveryChargeOption == "OPTION_2"
                        val isDisabled = deliveryChargeOption == "DISABLED"
                        val cfg = (remoteConfig ?: RemoteConfigEntity()).copy(
                            flashSaleTitle = bannerText,
                            enableFlashSaleBanner = isBannerEnabled,
                            enableExpressDelivery = isExpressDelivery,
                            enableSellerTax = enableSellerTax,
                            enableDirectSellerPayment = enableDirectSellerPayment,
                            enablePerformanceReviews = enablePerformanceReviews,
                            enableCallVerificationBeforeAccept = enableCallVerificationBeforeAccept,
                            deliveryChargeOption = deliveryChargeOption,
                            deliveryChargeEnabled = !isDisabled,
                            payToTransportDirectlyEnabled = true,
                            mandatoryPayToTransport = isOption2,
                            lotCalculationEnabled = true,
                            lotCalculationEnabledOption1Direct = lotCalculationEnabledOption1Direct,
                            lotCalculationEnabledOption2 = lotCalculationEnabledOption2,
                            defaultFarePerLot = defaultFarePerLotStr.toDoubleOrNull() ?: 150.0,
                            minOfferDiscountPercent = minDiscountForOfferTag.toIntOrNull() ?: 5,
                            topAnnouncement = topAnnouncement
                        )
                        onSaveConfig(cfg)
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_remote_config_btn")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save & Push Remote Config")
                }
            }
        }
    }
}

@Composable
fun AdminOfferMrpTab(
    products: List<ProductEntity>,
    onEditOffer: (ProductEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Product MRP & Admin Offer Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Override MRP prices and approve offer discounts (> 5%) for buyer visibility.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(products, key = { it.id }) { product ->
            val hasMrp = product.isMrpEnabled && product.mrpPrice > product.price
            val discountPct = if (hasMrp) (((product.mrpPrice - product.price) / product.mrpPrice) * 100).toInt() else 0

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Selling Price: \$${String.format("%.2f", product.price)}", fontSize = 12.sp)

                        if (product.isMrpEnabled) {
                            Text("MRP: \$${String.format("%.2f", product.mrpPrice)} (Discount: $discountPct%)", fontSize = 12.sp, color = AmberAccent, fontWeight = FontWeight.SemiBold)
                        } else {
                            Text("MRP Option: Disabled by Seller", fontSize = 11.sp, color = Color.Gray)
                        }

                        if (discountPct >= 5) {
                            Surface(
                                color = Color(0xFFE53935),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text("High Offer > 5% Active", fontSize = 9.sp, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Button(
                        onClick = { onEditOffer(product) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("admin_edit_offer_${product.id}")
                    ) {
                        Text("Manage MRP / Offer", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserAndSellerManagementTab(
    viewModel: MainViewModel,
    onAddAdminClick: () -> Unit
) {
    val allUsers by viewModel.allUsers.collectAsState()
    var editingSeller by remember { mutableStateOf<com.example.data.UserEntity?>(null) }
    var selectedRoleFilter by remember { mutableStateOf("ALL") }

    val filteredUsers = remember(allUsers, selectedRoleFilter) {
        if (selectedRoleFilter == "ALL") allUsers
        else allUsers.filter { it.role.equals(selectedRoleFilter, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Central User & Seller Login Material Manager", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Admin can view buyer/seller details and exclusively update or suspend seller login credentials.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onAddAdminClick,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Additional Admin Account")
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL", "SELLER", "BUYER", "ADMIN").forEach { role ->
                    FilterChip(
                        selected = selectedRoleFilter == role,
                        onClick = { selectedRoleFilter = role },
                        label = { Text(role, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        items(filteredUsers) { user ->
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = when(user.role.uppercase()) {
                                    "SELLER" -> MaterialTheme.colorScheme.primary
                                    "ADMIN" -> Color(0xFFC62828)
                                    else -> MaterialTheme.colorScheme.secondary
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    user.role,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (!user.isLoginAllowed) {
                            Surface(
                                color = MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("LOGIN SUSPENDED", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Text("📧 Email / Login ID: ${user.email}", style = MaterialTheme.typography.bodySmall)
                    Text("📞 Primary Phone: ${user.phone}", style = MaterialTheme.typography.bodySmall)
                    if (user.secondaryPhone.isNotBlank()) {
                        Text("📱 Secondary Phone: ${user.secondaryPhone}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("📍 Address: ${user.address}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (user.role.equals("SELLER", ignoreCase = true)) {
                        Text("🔑 Password: ${user.password}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { editingSeller = user },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("admin_manage_seller_login_btn_${user.id}")
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Manage Seller Login Material", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    editingSeller?.let { seller ->
        EditSellerLoginMaterialDialog(
            seller = seller,
            onDismiss = { editingSeller = null },
            onSave = { updatedSeller ->
                viewModel.updateUser(updatedSeller)
                editingSeller = null
            }
        )
    }
}

@Composable
fun EditSellerLoginMaterialDialog(
    seller: com.example.data.UserEntity,
    onDismiss: () -> Unit,
    onSave: (com.example.data.UserEntity) -> Unit
) {
    var email by remember { mutableStateOf(seller.email) }
    var password by remember { mutableStateOf(seller.password) }
    var phone by remember { mutableStateOf(seller.phone) }
    var name by remember { mutableStateOf(seller.name) }
    var isLoginAllowed by remember { mutableStateOf(seller.isLoginAllowed) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Seller Login Material") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Admin can exclusively update credentials and login permissions for this seller.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Seller Business Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Seller Login Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_seller_email_input")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Seller Login Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_seller_password_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Seller Contact Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Allow Seller Login Access", fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isLoginAllowed,
                        onCheckedChange = { isLoginAllowed = it },
                        modifier = Modifier.testTag("seller_login_allowed_switch")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = seller.copy(
                        name = name.trim(),
                        email = email.trim(),
                        password = password.trim(),
                        phone = phone.trim(),
                        isLoginAllowed = isLoginAllowed
                    )
                    onSave(updated)
                },
                modifier = Modifier.testTag("save_seller_login_material_btn")
            ) {
                Text("Save Credentials")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminOrdersTab(orders: List<com.example.data.OrderEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("System-Wide Orders Monitoring", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${orders.size} total orders processed across all seller accounts.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(orders, key = { it.id }) { order ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Order #${order.orderNumber}", fontWeight = FontWeight.Bold)
                        com.example.ui.components.StatusBadge(order.status)
                    }
                    Text("Buyer: ${order.buyerName} (${order.buyerPhone})", fontSize = 12.sp)
                    Text("Total: \$${String.format("%.2f", order.totalPrice)} | Payment: ${order.paymentMethod}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CreateAdminDialog(
    onDismiss: () -> Unit,
    onCreate: (email: String, name: String, phone: String, pass: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Provision New Admin User", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Admin Full Name") }, singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Admin Email") }, singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (email.isNotBlank() && pass.isNotBlank()) {
                        onCreate(email, name.ifBlank { "Admin User" }, phone.ifBlank { "+1 800 ADMIN" }, pass)
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Create Admin")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminOfferEditDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onSaveOffer: (mrpPrice: Double, mrpEnabled: Boolean, showOfferTag: Boolean) -> Unit
) {
    var mrpStr by remember { mutableStateOf(if (product.mrpPrice > 0) product.mrpPrice.toString() else "") }
    var mrpEnabled by remember { mutableStateOf(product.isMrpEnabled) }
    var isApproved by remember { mutableStateOf(product.isOfferBadgeEnabled) }

    val sellingPrice = product.price
    val mrpVal = mrpStr.toDoubleOrNull() ?: 0.0
    val calculatedPct = if (mrpEnabled && mrpVal > sellingPrice) (((mrpVal - sellingPrice) / mrpVal) * 100).toInt() else 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Admin MRP & Offer Engine: ${product.name}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Selling Price: \$${String.format("%.2f", sellingPrice)}", fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable MRP Price Toggle")
                    Switch(checked = mrpEnabled, onCheckedChange = { mrpEnabled = it })
                }

                if (mrpEnabled) {
                    OutlinedTextField(
                        value = mrpStr,
                        onValueChange = { mrpStr = it },
                        label = { Text("MRP List Price ($)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Calculated Discount: $calculatedPct%",
                        fontWeight = FontWeight.Bold,
                        color = if (calculatedPct >= 5) Color(0xFFE53935) else MaterialTheme.colorScheme.primary
                    )

                    if (calculatedPct >= 5) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Approve Admin High Discount Offer (>5%)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Switch(checked = isApproved, onCheckedChange = { isApproved = it })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveOffer(mrpVal, mrpEnabled, isApproved)
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Admin Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminTransportDetailsTab(viewModel: MainViewModel) {
    val routes by viewModel.allRoutes.collectAsState()
    var showAddRouteDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Transport Details / Delivery Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Configure logistics routes, bus & truck transport names, fare per lot, and journey pincodes for local delivery charge calculation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Route details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { showAddRouteDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("admin_add_route_details_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Route Details")
                }
            }
        }

        if (routes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "No route details configured yet. Click 'Add Route Details' to create your first transport delivery corridor.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray
                    )
                }
            }
        } else {
            items(routes, key = { it.id }) { route ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📍 ${route.routeName}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            IconButton(onClick = { viewModel.deleteRoute(route) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Route", tint = Color.Red)
                            }
                        }

                        if (route.transportName.isNotBlank()) {
                            Text("🚛 Transport Name: ${route.transportName}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        if (route.busNamesCsv.isNotBlank()) {
                            Text("🚌 (I) Bus Name(s): ${route.busNamesCsv}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (route.truckNamesCsv.isNotBlank()) {
                            Text("🚚 (II) Truck Name(s): ${route.truckNamesCsv}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (route.routeApproach.isNotBlank()) {
                            Text("🗺️ Route Approach: ${route.routeApproach}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        Text(
                            text = "💰 Fare / Delivery Charge per Lot: ₹${String.format("%.2f", route.farePerLot)}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            fontSize = 13.sp
                        )

                        if (route.journeyPincodesCsv.isNotBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "📮 Route Journey Pincodes: ${route.journeyPincodesCsv}",
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddRouteDialog) {
        AddRouteDetailsDialog(
            onDismiss = { showAddRouteDialog = false },
            onSave = { newRoute ->
                viewModel.addOrUpdateRoute(newRoute)
                showAddRouteDialog = false
            }
        )
    }
}

@Composable
fun AddRouteDetailsDialog(
    onDismiss: () -> Unit,
    onSave: (RouteDetailEntity) -> Unit
) {
    var routeName by remember { mutableStateOf("") }
    var transportName by remember { mutableStateOf("") }
    var busNamesCsv by remember { mutableStateOf("") }
    var truckNamesCsv by remember { mutableStateOf("") }
    var routeApproach by remember { mutableStateOf("") }
    var farePerLotStr by remember { mutableStateOf("150") }
    var journeyPincodesCsv by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Route Details") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = routeName,
                        onValueChange = { routeName = it },
                        label = { Text("1. Route Name") },
                        placeholder = { Text("e.g. NH-48 Express Agri Corridor") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = transportName,
                        onValueChange = { transportName = it },
                        label = { Text("2. Transport Name") },
                        placeholder = { Text("e.g. Western State Logistics") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = busNamesCsv,
                        onValueChange = { busNamesCsv = it },
                        label = { Text("  (I) Bus Name(s)") },
                        placeholder = { Text("e.g. Vikas Bus, Express Sleeper") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = truckNamesCsv,
                        onValueChange = { truckNamesCsv = it },
                        label = { Text("  (II) Truck Name(s)") },
                        placeholder = { Text("e.g. Eicher 10.95, Tata 407 Cargo") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = routeApproach,
                        onValueChange = { routeApproach = it },
                        label = { Text("3. Route Approach (Destination)") },
                        placeholder = { Text("e.g. Agri Hub Terminal -> Central Hub") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = farePerLotStr,
                        onValueChange = { farePerLotStr = it },
                        label = { Text("4. Fare / Delivery Charge per Lot (in ₹)") },
                        placeholder = { Text("e.g. 150") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = journeyPincodesCsv,
                        onValueChange = { journeyPincodesCsv = it },
                        label = { Text("5. Add Route Journey Pincodes") },
                        placeholder = { Text("e.g. 380001, 380015, 360001 (Start, End & In-journey)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (routeName.isNotBlank()) {
                        val route = RouteDetailEntity(
                            routeName = routeName.trim(),
                            transportName = transportName.trim(),
                            busNamesCsv = busNamesCsv.trim(),
                            truckNamesCsv = truckNamesCsv.trim(),
                            routeApproach = routeApproach.trim(),
                            farePerLot = farePerLotStr.toDoubleOrNull() ?: 150.0,
                            journeyPincodesCsv = journeyPincodesCsv.trim()
                        )
                        onSave(route)
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Route Details")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminProductReviewsTab(viewModel: MainViewModel) {
    val reviews by viewModel.allReviews.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Product Reviews Moderation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Moderate all buyer product reviews. Approved reviews are displayed directly under product details.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        if (reviews.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text(
                        text = "No product reviews posted by buyers yet.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        } else {
            items(reviews, key = { it.id }) { review ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "👤 ${review.buyerName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Row {
                                repeat(5) { starIndex ->
                                    Icon(
                                        imageVector = if (starIndex < review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Product: ${review.productName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "\"${review.reviewText}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (review.isApproved) "✅ Status: Approved & Live" else "🙈 Status: Hidden",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (review.isApproved) Color(0xFF2E7D32) else Color.Red
                            )

                            Row {
                                TextButton(onClick = { viewModel.updateReviewApproval(review.id, !review.isApproved) }) {
                                    Text(if (review.isApproved) "Hide" else "Approve", fontSize = 12.sp)
                                }
                                TextButton(onClick = { viewModel.deleteReview(review.id) }) {
                                    Text("Delete", fontSize = 12.sp, color = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
