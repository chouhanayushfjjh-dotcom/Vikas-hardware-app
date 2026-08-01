package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import coil.compose.AsyncImage
import com.example.data.NotificationEntity
import com.example.data.ProductEntity
import com.example.util.AppLanguage
import com.example.util.LocalizationManager
import com.example.ui.theme.*
import com.example.viewmodel.Screen

fun getDeliveryIcon(serviceName: String): ImageVector {
    val lower = serviceName.lowercase()
    return when {
        lower.contains("bus") -> Icons.Default.DirectionsBus
        lower.contains("mini truck") -> Icons.Default.RvHookup
        lower.contains("truck") -> Icons.Default.AirportShuttle
        lower.contains("pickup") || lower.contains("loading") -> Icons.Default.Moped
        else -> Icons.Default.LocalShipping
    }
}

@Composable
fun VikasLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    textSize: TextUnit = 22.sp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF15803D),
                        Color(0xFF22C55E),
                        Color(0xFF16A34A)
                    )
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF86EFAC),
                        Color(0xFF4ADE80)
                    )
                ),
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "V",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = textSize,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-1).sp
            )
            Box(
                modifier = Modifier
                    .offset(x = (-1).dp, y = (-6).dp)
                    .size(6.dp)
                    .background(Color(0xFFFACC15), CircleShape)
            )
        }
    }
}

@Composable
fun AppTopBar(
    currentScreen: Screen,
    userRole: String?,
    userName: String?,
    unreadNotificationsCount: Int,
    cartItemCount: Int,
    onNavigate: (Screen) -> Unit,
    onOpenNotifications: () -> Unit,
    onLogout: () -> Unit,
    onQuickLoginSeller: (() -> Unit)? = null,
    onLoginSeller: ((String, String) -> Unit)? = null,
    onOpenLanguageSelector: (() -> Unit)? = null
) {
    var logoMenuExpanded by remember { mutableStateOf(false) }
    var showSellerLoginDialog by remember { mutableStateOf(false) }
    val currentLang by LocalizationManager.currentLanguage.collectAsState()

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand / Title with Logo Dropdown Menu
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { logoMenuExpanded = true }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("vikas_logo_menu_trigger")
                    ) {
                        VikasLogoBadge(size = 36.dp, textSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Vikas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = when (userRole) {
                                    "ADMIN" -> "Central Admin"
                                    "SELLER" -> "Store Manager"
                                    else -> "Agriculture & Hardware"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Logo Click Options Dropdown
                    DropdownMenu(
                        expanded = logoMenuExpanded,
                        onDismissRequest = { logoMenuExpanded = false },
                        modifier = Modifier
                            .width(250.dp)
                            .testTag("vikas_logo_dropdown_menu")
                    ) {
                        if (userRole == "ADMIN") {
                            DropdownMenuItem(
                                text = { Text("1. Central Admin Control", fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    logoMenuExpanded = false
                                    onNavigate(Screen.AdminDashboard)
                                },
                                modifier = Modifier.testTag("logo_menu_admin_home_item")
                            )
                            DropdownMenuItem(
                                text = { Text("2. Storefront (Buyer View)", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    logoMenuExpanded = false
                                    onNavigate(Screen.Catalog)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("3. Seller Hub Dashboard", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.Dashboard, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    logoMenuExpanded = false
                                    onNavigate(Screen.SellerDashboard)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("4. Direct Call Desk (SFCMP)", fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.ContactPhone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    logoMenuExpanded = false
                                    onNavigate(Screen.SellerContactManagement)
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("5. Log Out Admin", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Outlined.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    logoMenuExpanded = false
                                    onLogout()
                                },
                                modifier = Modifier.testTag("logo_menu_admin_logout_item")
                            )
                        } else if (userRole == "SELLER") {
                            DropdownMenuItem(
                                text = { Text("1. Seller Dashboard", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.Dashboard, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    logoMenuExpanded = false
                                    onNavigate(Screen.SellerDashboard)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("2. Product Catalog", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    logoMenuExpanded = false
                                    onNavigate(Screen.Catalog)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("3. SFCMP Phone Desk", fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.ContactPhone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    logoMenuExpanded = false
                                    onNavigate(Screen.SellerContactManagement)
                                },
                                modifier = Modifier.testTag("logo_menu_sfcmp_item")
                            )
                            DropdownMenuItem(
                                text = { Text("4. Buyer Chat Answers", fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    logoMenuExpanded = false
                                    onNavigate(Screen.ChatSupport)
                                },
                                modifier = Modifier.testTag("logo_menu_seller_chat_item")
                            )
                            DropdownMenuItem(
                                text = { Text("5. Admin Control Panel (Login)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary) },
                                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
                                onClick = {
                                    logoMenuExpanded = false
                                    onNavigate(Screen.AdminDashboard)
                                },
                                modifier = Modifier.testTag("logo_menu_admin_item")
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("6. Logout / Switch Account", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Outlined.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    logoMenuExpanded = false
                                    onLogout()
                                },
                                modifier = Modifier.testTag("logo_menu_seller_logout_item")
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("1. Profile Details", fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    logoMenuExpanded = false
                                    onNavigate(Screen.Profile)
                                },
                                modifier = Modifier.testTag("logo_menu_profile_item")
                            )
                            DropdownMenuItem(
                                text = { Text("2. Chat with Seller / Support", fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    logoMenuExpanded = false
                                    onNavigate(Screen.ChatSupport)
                                },
                                modifier = Modifier.testTag("logo_menu_buyer_chat_item")
                            )
                            DropdownMenuItem(
                                text = { Text("3. Become a Seller / Seller Login", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    logoMenuExpanded = false
                                    showSellerLoginDialog = true
                                },
                                modifier = Modifier.testTag("logo_menu_become_seller_item")
                            )
                            DropdownMenuItem(
                                text = { Text("4. Customer Support", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.SupportAgent, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    logoMenuExpanded = false
                                    onNavigate(Screen.ContactSupport)
                                },
                                modifier = Modifier.testTag("logo_menu_support_item")
                            )
                            DropdownMenuItem(
                                text = { Text("5. Direct Phone Call Contact", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    logoMenuExpanded = false
                                    onNavigate(Screen.DirectCallContact)
                                },
                                modifier = Modifier.testTag("logo_menu_direct_call_item")
                            )
                            DropdownMenuItem(
                                text = { Text("6. My Cart", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    logoMenuExpanded = false
                                    onNavigate(Screen.Cart)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("7. My Orders", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    logoMenuExpanded = false
                                    onNavigate(Screen.OrderHistory)
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("8. Logout / Exit Account", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Outlined.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    logoMenuExpanded = false
                                    onLogout()
                                },
                                modifier = Modifier.testTag("logo_menu_buyer_logout_item")
                            )
                        }
                    }
                }

                // Actions: Role Badge, Notifications, Profile, Log In text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (userRole == "ADMIN") {
                        AssistChip(
                            onClick = { onNavigate(Screen.AdminDashboard) },
                            label = { Text("Admin Panel", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("admin_dashboard_chip")
                        )
                    } else if (userRole == "SELLER") {
                        AssistChip(
                            onClick = { onNavigate(Screen.SellerDashboard) },
                            label = { Text("Seller Panel", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("seller_dashboard_chip")
                        )
                    }

                    // Notifications Icon with badge
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier.testTag("notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge(containerColor = AmberAccent) {
                                        Text(unreadNotificationsCount.toString(), color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                        }
                    }

                    // Profile Icon for Profile Page
                    if (userRole != "SELLER") {
                        IconButton(
                            onClick = { onNavigate(Screen.Profile) },
                            modifier = Modifier.testTag("topbar_buyer_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Buyer Profile Page",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Log In Text Button in Header Line
                    if (userName == null) {
                        Button(
                            onClick = { onNavigate(Screen.Auth) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("login_nav_button")
                        ) {
                            Text("Log In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            onClick = { onNavigate(Screen.Profile) },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Text(
                                text = userName.take(8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSellerLoginDialog) {
        SellerLoginDialog(
            onDismiss = { showSellerLoginDialog = false },
            onLoginSeller = onLoginSeller,
            onQuickLoginSeller = onQuickLoginSeller
        )
    }
}

@Composable
fun AppBottomNavigation(
    currentScreen: Screen,
    userRole: String?,
    cartCount: Int,
    onNavigate: (Screen) -> Unit
) {
    val currentLang by LocalizationManager.currentLanguage.collectAsState()

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        if (userRole == "SELLER") {
            NavigationBarItem(
                selected = currentScreen is Screen.SellerDashboard,
                onClick = { onNavigate(Screen.SellerDashboard) },
                icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                label = { Text(LocalizationManager.getString("seller_portal", currentLang)) },
                modifier = Modifier.testTag("nav_seller_dashboard")
            )
            NavigationBarItem(
                selected = currentScreen is Screen.Catalog,
                onClick = { onNavigate(Screen.Catalog) },
                icon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                label = { Text(LocalizationManager.getString("search", currentLang)) },
                modifier = Modifier.testTag("nav_seller_items")
            )
            NavigationBarItem(
                selected = currentScreen is Screen.SellerContactManagement,
                onClick = { onNavigate(Screen.SellerContactManagement) },
                icon = { Icon(Icons.Default.ContactPhone, contentDescription = null) },
                label = { Text("SDCMP", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("nav_seller_sdcmp")
            )
        } else {
            NavigationBarItem(
                selected = currentScreen is Screen.Home,
                onClick = { onNavigate(Screen.Home) },
                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                label = { Text(LocalizationManager.getString("home", currentLang)) },
                modifier = Modifier.testTag("nav_home")
            )
            NavigationBarItem(
                selected = currentScreen is Screen.Catalog,
                onClick = { onNavigate(Screen.Catalog) },
                icon = { Icon(Icons.Default.Category, contentDescription = null) },
                label = { Text("Categories", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("nav_catalog")
            )
            NavigationBarItem(
                selected = currentScreen is Screen.DirectCallContact,
                onClick = { onNavigate(Screen.DirectCallContact) },
                icon = { Icon(Icons.Default.PhoneInTalk, contentDescription = null) },
                label = { Text("Direct Call", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("nav_direct_call")
            )
            NavigationBarItem(
                selected = currentScreen is Screen.Cart,
                onClick = { onNavigate(Screen.Cart) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge { Text(cartCount.toString()) }
                            }
                        }
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    }
                },
                label = { Text(LocalizationManager.getString("cart", currentLang)) },
                modifier = Modifier.testTag("nav_cart")
            )
            NavigationBarItem(
                selected = currentScreen is Screen.OrderHistory,
                onClick = { onNavigate(Screen.OrderHistory) },
                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                label = { Text(LocalizationManager.getString("orders", currentLang)) },
                modifier = Modifier.testTag("nav_orders")
            )
        }
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    onProductClick: (Long) -> Unit,
    onAddToCart: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isSeller: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onProductClick(product.id) }
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f))
            ) {
                AsyncImage(
                    model = product.photoUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Daily Special Badge
                if (product.isDailySpecial) {
                    Surface(
                        color = AmberAccent,
                        shape = RoundedCornerShape(bottomEnd = 12.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Today's Special",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Availability tag
                val isAvailable = product.isAvailable && product.availableQuantity > 0
                Surface(
                    color = if (isAvailable) StatusDeliveredBg else StatusCancelledBg,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (isAvailable) "${product.availableQuantity} left" else "Out of stock",
                        color = if (isAvailable) StatusDeliveredFg else StatusCancelledFg,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        val effectiveMrp = if (product.mrpPrice > 0) product.mrpPrice else (product.price * 1.25)
                        val discountPct = if (effectiveMrp > product.price) (((effectiveMrp - product.price) / effectiveMrp) * 100).toInt() else 0

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "\$${String.format("%.2f", product.price)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MRP \$${String.format("%.2f", effectiveMrp)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                ),
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (discountPct > 0) {
                            Surface(
                                color = Color(0xFFE53935),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "$discountPct% OFF OFFER 🔥",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "MRP Verified",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    if (!isSeller) {
                        Button(
                            onClick = { onAddToCart(product.id) },
                            enabled = product.isAvailable && product.availableQuantity > 0,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("add_to_cart_btn_${product.id}")
                        ) {
                            Icon(
                                Icons.Default.AddShoppingCart,
                                contentDescription = "Add to Cart",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onProductClick(product.id) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("view_details_btn_${product.id}")
                        ) {
                            Text("Details", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, fgColor, text) = when (status.uppercase()) {
        "PENDING" -> Triple(StatusPendingBg, StatusPendingFg, "Pending Approval")
        "ACCEPTED" -> Triple(StatusAcceptedBg, StatusAcceptedFg, "Accepted / Preparing")
        "DELIVERED" -> Triple(StatusDeliveredBg, StatusDeliveredFg, "Delivered")
        "CANCELLED" -> Triple(StatusCancelledBg, StatusCancelledFg, "Cancelled")
        else -> Triple(Color.LightGray, Color.DarkGray, status)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(fgColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = fgColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelect,
        label = { Text(category, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White
        ),
        modifier = Modifier.testTag("category_chip_$category")
    )
}

@Composable
fun LargeActionButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NotificationDialog(
    notifications: List<NotificationEntity>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onSelectOrder: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notifications", style = MaterialTheme.typography.titleLarge)
                if (notifications.any { !it.isRead }) {
                    TextButton(onClick = onMarkAllRead) {
                        Text("Mark all read", fontSize = 12.sp)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
            ) {
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No notifications yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    notifications.forEach { notif ->
                        Surface(
                            color = if (notif.isRead) Color.Transparent else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    notif.relatedOrderId?.let { orderId ->
                                        onSelectOrder(orderId)
                                        onDismiss()
                                    }
                                }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = notif.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun SellerLoginDialog(
    onDismiss: () -> Unit,
    onLoginSeller: ((String, String) -> Unit)?,
    onQuickLoginSeller: (() -> Unit)?
) {
    var email by remember { mutableStateOf("seller@shop.com") }
    var password by remember { mutableStateOf("seller123") }
    var passwordVisible by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VikasLogoBadge(size = 32.dp, textSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Vikas Seller Portal Login", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Manage store inventory, list agricultural equipment, and process customer orders.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Seller credentials display banner showing password clearly
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Seller Credentials & Password",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Email: seller@shop.com", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Password: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                    Text("seller123", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Seller Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("seller_email_input")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password"
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("seller_password_input")
                )

                OutlinedButton(
                    onClick = {
                        onQuickLoginSeller?.invoke()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("seller_quick_login_btn")
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("⚡ Quick Seller Demo Login", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onLoginSeller?.invoke(email, password)
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Log In as Seller")
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
fun InitialAuthDialog(
    viewModel: com.example.viewmodel.MainViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row with Cancel/Cut Close 'X' Button on corner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VikasLogoBadge(size = 36.dp, textSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSignUp) "Create Account" else "Vikas Store",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Cancel / Cut Close Button on top corner
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("auth_dialog_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isSignUp) "Sign up to order agricultural tools, seeds & farm supplies" else "Log in to browse farm equipment and place orders",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Tabs
                TabRow(
                    selectedTabIndex = if (isSignUp) 1 else 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = !isSignUp,
                        onClick = { isSignUp = false },
                        modifier = Modifier.testTag("dialog_login_tab")
                    ) {
                        Text("Log In", modifier = Modifier.padding(vertical = 10.dp), fontWeight = FontWeight.Bold)
                    }
                    Tab(
                        selected = isSignUp,
                        onClick = { isSignUp = true },
                        modifier = Modifier.testTag("dialog_signup_tab")
                    ) {
                        Text("Sign Up", modifier = Modifier.padding(vertical = 10.dp), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Demo Logins
                if (!isSignUp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.quickLoginBuyer()
                                onSuccess()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialog_quick_customer_login_btn")
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Customer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.quickLoginSeller()
                                onSuccess()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialog_quick_seller_login_btn")
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Seller Login", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Input Fields
                if (isSignUp) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_signup_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_email_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password"
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_password_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (isSignUp) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_phone_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Delivery Address") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_address_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                LargeActionButton(
                    text = if (isSignUp) "Create Account" else "Log In",
                    icon = if (isSignUp) Icons.Default.PersonAdd else Icons.Default.Login,
                    onClick = {
                        if (isSignUp) {
                            if (name.isBlank() || email.isBlank() || password.isBlank() || phone.isBlank() || address.isBlank()) {
                                viewModel.showMessage("Please fill in all fields.")
                                return@LargeActionButton
                            }
                            viewModel.registerBuyer(name, email, password, phone, address) {
                                onSuccess()
                            }
                        } else {
                            if (email.isBlank() || password.isBlank()) {
                                viewModel.showMessage("Please enter email and password.")
                                return@LargeActionButton
                            }
                            viewModel.login(email, password) {
                                onSuccess()
                            }
                        }
                    },
                    modifier = Modifier.testTag("dialog_auth_submit_btn")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isSignUp) "Already have an account?" else "Don't have an account?",
                        style = MaterialTheme.typography.bodySmall
                    )
                    TextButton(
                        onClick = { isSignUp = !isSignUp },
                        modifier = Modifier.testTag("dialog_toggle_auth_btn")
                    ) {
                        Text(
                            text = if (isSignUp) "Log In" else "Sign Up",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onContinue: () -> Unit
) {
    var selectedLang by remember { mutableStateOf(currentLanguage) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = {
            // Must select language and click continue
        }
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon & Badge
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Language",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = LocalizationManager.getString("select_language_title", selectedLang),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = LocalizationManager.getString("select_language_subtitle", selectedLang),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // List of Languages
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = selectedLang == lang
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLang = lang
                                    onLanguageSelected(lang)
                                }
                                .testTag("lang_option_${lang.code}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(lang.flag, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = lang.nativeName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = lang.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else {
                                    RadioButton(
                                        selected = false,
                                        onClick = {
                                            selectedLang = lang
                                            onLanguageSelected(lang)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Continue Button
                Button(
                    onClick = {
                        onLanguageSelected(selectedLang)
                        onContinue()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("language_continue_btn")
                ) {
                    Text(
                        text = LocalizationManager.getString("continue", selectedLang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Continue",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SmartCenteredLoadingAnimation(
    modifier: Modifier = Modifier.fillMaxSize(),
    title: String = "Loading...",
    subtitle: String? = "Please wait a moment...",
    showLogo: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SmartLoadingTransition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationAngle"
    )

    val dotCount by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 3.99f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DotCount"
    )

    val animatedDots = ".".repeat(dotCount.toInt())

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .testTag("smart_centered_loading_anim"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
            tonalElevation = 2.dp,
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 320.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(80.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(76.dp)
                            .rotate(rotationAngle),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.5.dp,
                        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )

                    if (showLogo) {
                        VikasLogoBadge(
                            size = 46.dp,
                            textSize = 24.sp,
                            modifier = Modifier.scale(pulseScale)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Loading",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(32.dp)
                                .scale(pulseScale)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "$title$animatedDots",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

