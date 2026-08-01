package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.OrderEntity
import com.example.data.OrderWithItems
import com.example.data.ProductEntity
import com.example.data.RouteDetailEntity
import com.example.ui.components.LargeActionButton
import com.example.ui.components.SmartCenteredLoadingAnimation
import com.example.ui.components.StatusBadge
import com.example.ui.components.getDeliveryIcon
import com.example.ui.theme.AmberAccent
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeliveryOption(
    val title: String,
    val icon: ImageVector,
    val description: String
)

val deliveryOptionsList = listOf(
    DeliveryOption("Vikas Own Vehicle Service", Icons.Default.LocalShipping, "Direct delivery via Vikas personal vehicle fleet"),
    DeliveryOption("Transport (Buses)", Icons.Default.DirectionsBus, "Dispatched via regional/local bus transport"),
    DeliveryOption("Transport (Truck)", Icons.Default.AirportShuttle, "Heavy transport via cargo truck"),
    DeliveryOption("Transport (Mini Truck)", Icons.Default.RvHookup, "Medium cargo via mini truck loading"),
    DeliveryOption("Transport (Pickup Loading)", Icons.Default.Moped, "Local pickup / auto loading vehicle")
)

@Composable
fun SellerDashboardScreen(
    viewModel: MainViewModel,
    onNavigate: (Screen) -> Unit
) {
    val isSeller by viewModel.isSeller.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val orders by viewModel.sellerOrders.collectAsState()
    val remoteConfig by viewModel.remoteConfig.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Daily Items, 1: Buyer Orders, 2: Sales Summary
    var orderStatusFilter by remember { mutableStateOf("ALL") }

    var showAddProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var selectedOrderForDetail by remember { mutableStateOf<Long?>(null) }
    var deliveryDialogOrderAndStatus by remember { mutableStateOf<Pair<OrderEntity, String>?>(null) }
    var orderForUpdateTrack by remember { mutableStateOf<OrderEntity?>(null) }
    var orderForCallVerification by remember { mutableStateOf<OrderEntity?>(null) }

    if (!isSeller) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.AdminPanelSettings,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Seller Admin Access Only",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Please log in with your Seller account to access the dashboard.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.quickLoginSeller() },
                modifier = Modifier.testTag("login_seller_access_btn")
            ) {
                Text("Log In as Seller Demo")
            }
        }
        return
    }

    var storeOpenToday by remember { mutableStateOf(true) }
    var menuExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Vikas Style Seller Top App Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 2.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.testTag("seller_threeline_logo_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Seller Drawer Menu",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.width(280.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("1. Direct Contact & Call Orders Desk (SDCMP)", fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.ContactPhone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigate(Screen.SellerContactManagement)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("2. Customer Chat & Answers Desk", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigate(Screen.ChatSupport)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("3. Central Admin Control Panel", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigate(Screen.AdminDashboard)
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("4. Sign Out / Switch User", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.logout()
                                    onNavigate(Screen.Auth)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Vikas Seller Hub",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (storeOpenToday) Color(0xFF2E7D32) else Color.Red,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (storeOpenToday) "OPEN" else "CLOSED",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Daily Operations & Store Management",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Store Open Today Switch Toggle
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (storeOpenToday) "Open Today" else "Closed",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (storeOpenToday) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = storeOpenToday,
                            onCheckedChange = {
                                storeOpenToday = it
                                viewModel.showMessage(if (it) "Store marked OPEN for today!" else "Store marked CLOSED for today")
                            },
                            modifier = Modifier.testTag("store_open_today_switch")
                        )
                    }
                }
            }
        }

        // Dashboard Tab Selector
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            edgePadding = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                modifier = Modifier.testTag("seller_tab_items")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Catalog (${allProducts.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                modifier = Modifier.testTag("seller_tab_orders")
            ) {
                val pendingCount = orders.count { it.status == "PENDING" }
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BadgedBox(
                        badge = {
                            if (pendingCount > 0) {
                                Badge(containerColor = AmberAccent) {
                                    Text(pendingCount.toString(), color = Color.White)
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Orders (${orders.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                modifier = Modifier.testTag("seller_tab_offers")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Offers & Coupons", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Tab(
                selected = activeTab == 3,
                onClick = { activeTab = 3 },
                modifier = Modifier.testTag("seller_tab_settlements")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Payments & Tax", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Tab(
                selected = activeTab == 4,
                onClick = { activeTab = 4 },
                modifier = Modifier.testTag("seller_tab_analytics")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.QueryStats, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Performance & Reviews", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Tab(
                selected = activeTab == 5,
                onClick = { activeTab = 5 },
                modifier = Modifier.testTag("seller_tab_support")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Chat & Support", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Tab(
                selected = activeTab == 6,
                onClick = { activeTab = 6 },
                modifier = Modifier.testTag("seller_tab_payment_methods")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Manage Buyer Payment Method", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Tab(
                selected = activeTab == 7,
                onClick = { activeTab = 7 },
                modifier = Modifier.testTag("seller_tab_lot_management")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Widgets, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lot Management", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Tab(
                selected = activeTab == 8,
                onClick = { activeTab = 8 },
                modifier = Modifier.testTag("seller_tab_vyapar_summary")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Summary / Vyapar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        when (activeTab) {
            0 -> {
                // TAB 0: Daily Items Management
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Today's Available Stock", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Tap switch to make items available or out-of-stock instantly", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        FloatingActionButton(
                            onClick = { showAddProductDialog = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            modifier = Modifier.testTag("add_product_fab")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Product")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(allProducts, key = { it.id }) { product ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("seller_product_item_${product.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = product.photoUrl,
                                        contentDescription = product.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(product.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("\$${String.format("%.2f", product.price)} • ${product.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "Stock: ${product.availableQuantity}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            IconButton(
                                                onClick = { editingProduct = product },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Item", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Switch(
                                            checked = product.isAvailable && product.availableQuantity > 0,
                                            onCheckedChange = { isChecked ->
                                                viewModel.toggleProductAvailability(product.id, isChecked)
                                            },
                                            modifier = Modifier.testTag("toggle_availability_${product.id}")
                                        )
                                        Text(
                                            if (product.isAvailable && product.availableQuantity > 0) "Available" else "Disabled",
                                            fontSize = 10.sp,
                                            color = if (product.isAvailable && product.availableQuantity > 0) MaterialTheme.colorScheme.primary else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // TAB 1: Master Order Manager
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Status Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filters = listOf("ALL", "PENDING", "ACCEPTED", "DELIVERED", "CANCELLED")
                        filters.forEach { status ->
                            FilterChip(
                                selected = orderStatusFilter == status,
                                onClick = { orderStatusFilter = status },
                                label = { Text(status, fontSize = 11.sp) },
                                modifier = Modifier.testTag("filter_chip_$status")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredOrders = remember(orders, orderStatusFilter) {
                        if (orderStatusFilter == "ALL") orders
                        else orders.filter { it.status == orderStatusFilter }
                    }

                    if (filteredOrders.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No orders match status filter.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredOrders, key = { it.id }) { order ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("seller_order_card_${order.id}")
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(order.orderNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            StatusBadge(order.status)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Buyer: ${order.buyerName} • ${order.buyerPhone}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Text("Deliver to: ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                         if (order.deliveryNotes.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Note: ${order.deliveryNotes}", style = MaterialTheme.typography.bodySmall, color = AmberAccent, fontWeight = FontWeight.Medium)
                                        }

                                        // Delivery Service Badge on Card
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = getDeliveryIcon(order.deliveryService),
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        if (order.deliveryService.isNotBlank()) "Service: ${order.deliveryService}" else "Delivery: Not assigned (Tap Accept to assign)",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    if (order.deliveryDetails.isNotBlank()) {
                                                        Text(
                                                            order.deliveryDetails,
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                IconButton(
                                                    onClick = { deliveryDialogOrderAndStatus = Pair(order, order.status) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Change Delivery Mode", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val dateStr = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(order.createdAt))
                                            Text(dateStr, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            Text("\$${String.format("%.2f", order.totalPrice)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }

                                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                                        // Status action buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (order.status == "PENDING") {
                                                Button(
                                                    onClick = {
                                                        if (remoteConfig.enableCallVerificationBeforeAccept) {
                                                            orderForCallVerification = order
                                                        } else {
                                                            deliveryDialogOrderAndStatus = Pair(order, "ACCEPTED")
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("accept_order_btn_${order.id}")
                                                ) {
                                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Accept")
                                                }
                                            }

                                            // Option "Update Track" placed between Accept and Delivered
                                            if (order.status == "PENDING" || order.status == "ACCEPTED") {
                                                OutlinedButton(
                                                    onClick = { orderForUpdateTrack = order },
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .weight(1.2f)
                                                        .testTag("update_track_btn_${order.id}")
                                                ) {
                                                    Icon(Icons.Default.EditLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Update Track", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            if (order.status == "ACCEPTED") {
                                                Button(
                                                    onClick = { deliveryDialogOrderAndStatus = Pair(order, "DELIVERED") },
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("deliver_order_btn_${order.id}")
                                                ) {
                                                    Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Delivered")
                                                }
                                            }

                                            if (order.status != "DELIVERED" && order.status != "CANCELLED") {
                                                OutlinedButton(
                                                    onClick = { viewModel.updateOrderStatus(order.id, "CANCELLED") },
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("cancel_order_btn_${order.id}")
                                                ) {
                                                    Text("Cancel")
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = { selectedOrderForDetail = order.id },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.testTag("view_order_details_btn_${order.id}")
                                            ) {
                                                Icon(Icons.Default.Visibility, contentDescription = "View Details", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // TAB 2: Offers & Coupon Management
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Seller Coupon & Offer Manager", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Boost sales by offering store discount coupons. Offers created with > 5% discount from MRP qualify for Vikas Spotlight Highlights!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Coupon List
                    Text("Active Store Coupons", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("VIKAS2026", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                                Text("20% OFF on Orders above \$50", style = MaterialTheme.typography.bodySmall)
                                Text("Admin Status: APPROVED & ENABLED", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                            Switch(checked = true, onCheckedChange = { viewModel.showMessage("Coupon toggled") })
                        }
                    }

                    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("HARVEST10", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                                Text("Flat \$10 OFF on Irrigation Equipment", style = MaterialTheme.typography.bodySmall)
                                Text("Admin Status: APPROVED & ENABLED", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                            Switch(checked = true, onCheckedChange = { viewModel.showMessage("Coupon toggled") })
                        }
                    }

                    Button(
                        onClick = { viewModel.showMessage("New coupon template created!") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Custom Seller Coupon")
                    }
                }
            }

            3 -> {
                // TAB 3: Payments, Settlement & Tax Details
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Current Payment Settlement", style = MaterialTheme.typography.labelMedium)
                            Text("\$1,845.00", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Scheduled Payout Date: Friday, Aug 01", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Bank Account:", style = MaterialTheme.typography.bodySmall)
                                Text("HDFC Bank **** 4892", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Platform Commission:", style = MaterialTheme.typography.bodySmall)
                                Text("2.0% (Subsidized)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                            }
                        }
                    }

                    Text("Recent Settlement Invoices", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    val settlements = listOf(
                        Triple("SET-2026-0722", "\$1,240.00", "Paid on Jul 22"),
                        Triple("SET-2026-0715", "\$980.50", "Paid on Jul 15"),
                        Triple("SET-2026-0708", "\$1,560.00", "Paid on Jul 08")
                    )

                    settlements.forEach { (inv, amt, status) ->
                        Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(inv, fontWeight = FontWeight.Bold)
                                    Text(status, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Text(amt, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            4 -> {
                // TAB 4: Performance & Customer Reviews
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Seller Health Badge", style = MaterialTheme.typography.labelMedium)
                                    Text("SUPER SELLER (4.9 ★)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AmberAccent)
                                }
                                Icon(Icons.Default.Verified, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(36.dp))
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("On-time Dispatch", fontSize = 11.sp, color = Color.Gray)
                                    Text("98.4%", fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Order Cancellation", fontSize = 11.sp, color = Color.Gray)
                                    Text("0.8%", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                                Column {
                                    Text("Buyer Rating", fontSize = 11.sp, color = Color.Gray)
                                    Text("4.9 / 5.0", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Text("Recent Customer Reviews", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    val reviews = listOf(
                        Pair("John Miller", "High quality shovel and fast delivery via bus transport! Highly recommended seller."),
                        Pair("Ramesh Patel", "Genuine agricultural machinery spare parts. Packed very carefully."),
                        Pair("Sunita Sharma", "Received irrigation pump on time. Great service!")
                    )

                    reviews.forEach { (reviewer, review) ->
                        Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(reviewer, fontWeight = FontWeight.Bold)
                                    Row {
                                        repeat(5) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(review, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            5 -> {
                // TAB 5: Chat Answers Desk & Support
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Customer Chat & Answers Desk", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Answer buyer product questions in real time and guide customers through order placement.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onNavigate(Screen.ChatSupport) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Live Chat Desk")
                            }
                        }
                    }

                    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Seller Support Ticket System", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Need help with cataloging, shipping issues or payouts?", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { viewModel.showMessage("Support ticket submitted to Vikas Admin Team!") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Support, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Raise Admin Support Ticket")
                            }
                        }
                    }
                }
            }

            6 -> {
                // TAB 6: Manage Buyer Payment Method
                SellerBuyerPaymentMethodsTab(viewModel = viewModel)
            }

            7 -> {
                // TAB 7: Lot Management
                SellerLotManagementTab(viewModel = viewModel)
            }

            8 -> {
                // TAB 8: Summary / Vyapar Page
                SellerVyaparSummaryTab(viewModel = viewModel)
            }
        }
    }

    // Add / Edit Product Sheet Dialog
    if (showAddProductDialog || editingProduct != null) {
        ProductEditDialog(
            product = editingProduct,
            onDismiss = {
                showAddProductDialog = false
                editingProduct = null
            },
            onSaveProduct = { updatedOrNew ->
                if (editingProduct != null) {
                    viewModel.updateProduct(updatedOrNew)
                } else {
                    viewModel.addProductEntity(updatedOrNew)
                }
                showAddProductDialog = false
                editingProduct = null
            },
            onDelete = { prod ->
                viewModel.deleteProduct(prod)
                showAddProductDialog = false
                editingProduct = null
            }
        )
    }

    // View Order Details Sheet Dialog
    selectedOrderForDetail?.let { orderId ->
        SellerOrderDetailDialog(
            orderId = orderId,
            viewModel = viewModel,
            onDismiss = { selectedOrderForDetail = null },
            onChangeDelivery = { order ->
                selectedOrderForDetail = null
                deliveryDialogOrderAndStatus = Pair(order, order.status)
            }
        )
    }

    // Delivery Service Selection Dialog
    deliveryDialogOrderAndStatus?.let { (order, targetStatus) ->
        DeliverySelectionDialog(
            order = order,
            targetStatus = targetStatus,
            onDismiss = { deliveryDialogOrderAndStatus = null },
            onConfirm = { service, details ->
                viewModel.updateOrderStatus(
                    orderId = order.id,
                    newStatus = targetStatus,
                    deliveryService = service,
                    deliveryDetails = details
                )
                deliveryDialogOrderAndStatus = null
            }
        )
    }

    // Update Track Dialog
    orderForUpdateTrack?.let { order ->
        UpdateTrackDialog(
            order = order,
            onDismiss = { orderForUpdateTrack = null },
            onSaveTracking = { trackingNo, courier, status ->
                viewModel.updateOrderTracking(
                    orderId = order.id,
                    trackingNumber = trackingNo,
                    courierName = courier,
                    trackingStatus = status
                )
                orderForUpdateTrack = null
            }
        )
    }

    // Call Verification Dialog Before Accept
    orderForCallVerification?.let { order ->
        OrderCallVerificationDialog(
            order = order,
            onDismiss = { orderForCallVerification = null },
            onVerificationResult = { isOrderedByMistake ->
                viewModel.processSellerOrderCallVerification(order.id, isOrderedByMistake)
                orderForCallVerification = null
                if (!isOrderedByMistake) {
                    deliveryDialogOrderAndStatus = Pair(order, "ACCEPTED")
                }
            }
        )
    }
}

@Composable
fun ProductEditDialog(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onSaveProduct: (ProductEntity) -> Unit,
    onDelete: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var photoUrl by remember { mutableStateOf(product?.photoUrl ?: "") }
    var priceStr by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var mrpPriceStr by remember { mutableStateOf(if ((product?.mrpPrice ?: 0.0) > 0.0) product?.mrpPrice?.toString() ?: "" else "") }
    var mrpEnabled by remember { mutableStateOf(product?.isMrpEnabled ?: false) }
    var qtyStr by remember { mutableStateOf(product?.availableQuantity?.toString() ?: "10") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Tools & Equipment") }
    var searchKeywords by remember { mutableStateOf(product?.searchKeywords ?: "") }
    var isDailySpecial by remember { mutableStateOf(product?.isDailySpecial ?: false) }

    var availabilityMode by remember { mutableStateOf(product?.availabilityMode ?: "LIVE_PRODUCT") }
    var minOrderQtyStr by remember { mutableStateOf(product?.minOrderQuantity?.toString() ?: "1") }
    var itemsPerLotStr by remember { mutableStateOf(product?.itemsPerLot?.toString() ?: "10") }

    var newTagInput by remember { mutableStateOf("") }

    val categories = listOf("Tools & Equipment", "Irrigation & Pumps", "Fertilizers & Soil", "Crop Protection", "Fencing & Hardware", "Machinery & Parts", "Seeds & Seedlings")
    val quickHashtags = listOf("#फावड़ा", "#कुदळ", "#खाद", "#खाતર", "#सिंचाई", "#ટપક", "#पंप", "#Sprayer", "#बीज", "#Azada", "#Hoe", "#Hardware")

    val currentHashtags = remember(searchKeywords) {
        if (searchKeywords.isBlank()) emptyList()
        else searchKeywords.split("\\s+|,|;".toRegex())
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { if (it.startsWith("#")) it else "#$it" }
            .distinct()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add Daily Item" else "Edit Item") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input")
                )

                // Product Status Mode: Live Product vs Coming Soon
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Product Availability Mode:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = availabilityMode == "LIVE_PRODUCT",
                                onClick = { availabilityMode = "LIVE_PRODUCT" },
                                label = { Text("🟢 Live Product", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = availabilityMode == "COMING_SOON",
                                onClick = { availabilityMode = "COMING_SOON" },
                                label = { Text("🚀 Coming Soon", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Min Order Quantity and Lot Configuration
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minOrderQtyStr,
                        onValueChange = { minOrderQtyStr = it },
                        label = { Text("Min. Order Qty") },
                        placeholder = { Text("1") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = itemsPerLotStr,
                        onValueChange = { itemsPerLotStr = it },
                        label = { Text("Items per Lot (Box)") },
                        placeholder = { Text("10") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Seller Hashtag Keyword Management Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = "Hashtags",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Hashtag Keyword System for Search",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Input field to add individual hashtag
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newTagInput,
                                onValueChange = { newTagInput = it },
                                label = { Text("Add Search Keyword") },
                                placeholder = { Text("e.g. फावड़ा or Sprayer") },
                                leadingIcon = {
                                    Text("#", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("product_new_tag_input")
                            )

                            Button(
                                onClick = {
                                    val clean = newTagInput.trim().removePrefix("#")
                                    if (clean.isNotBlank()) {
                                        val tag = "#$clean"
                                        if (!currentHashtags.contains(tag)) {
                                            val updated = (currentHashtags + tag).joinToString(" ")
                                            searchKeywords = updated
                                        }
                                        newTagInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("add_hashtag_btn")
                            ) {
                                Text("+ Tag")
                            }
                        }

                        // Active hashtag chips with delete option
                        if (currentHashtags.isNotEmpty()) {
                            Text("Active Hashtags (${currentHashtags.size}):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(currentHashtags.size) { idx ->
                                    val tag = currentHashtags[idx]
                                    InputChip(
                                        selected = true,
                                        onClick = {
                                            val updated = currentHashtags.filter { it != tag }.joinToString(" ")
                                            searchKeywords = updated
                                        },
                                        label = { Text(tag, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove tag",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        // Quick Add Regional Hashtags
                        Text("Quick Add Regional #Hashtags:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quickHashtags.size) { idx ->
                                val tag = quickHashtags[idx]
                                val isAdded = currentHashtags.contains(tag)
                                FilterChip(
                                    selected = isAdded,
                                    onClick = {
                                        if (isAdded) {
                                            val updated = currentHashtags.filter { it != tag }.joinToString(" ")
                                            searchKeywords = updated
                                        } else {
                                            val updated = (currentHashtags + tag).joinToString(" ")
                                            searchKeywords = updated
                                        }
                                    },
                                    label = { Text(tag, fontSize = 10.sp) }
                                )
                            }
                        }

                        Text(
                            "Note: Buyers can search for these terms with or without typing '#'!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Selling Price ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("product_price_input")
                    )

                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it },
                        label = { Text("Stock Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("product_qty_input")
                    )
                }

                // MRP Price & Toggle Option
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Enable MRP Price & Discount", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = mrpEnabled,
                                onCheckedChange = { mrpEnabled = it },
                                modifier = Modifier.testTag("mrp_enable_switch")
                            )
                        }

                        if (mrpEnabled) {
                            OutlinedTextField(
                                value = mrpPriceStr,
                                onValueChange = { mrpPriceStr = it },
                                label = { Text("MRP List Price ($)") },
                                placeholder = { Text("e.g. 45.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("product_mrp_input")
                            )
                            Text(
                                text = "🔒 Note: MRP is hidden from buyers unless discount > 5%, in which case admin offer badge is displayed.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Stock Status Quick Controls
                Text("Stock Quick Status:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AssistChip(
                        onClick = { if ((qtyStr.toIntOrNull() ?: 0) < 5) qtyStr = "15" },
                        label = { Text("In Stock (15)", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                    AssistChip(
                        onClick = { qtyStr = "3" },
                        label = { Text("Low Stock (3)", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                    AssistChip(
                        onClick = { qtyStr = "0" },
                        label = { Text("Stock End (0)", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }

                OutlinedTextField(
                    value = photoUrl,
                    onValueChange = { photoUrl = it },
                    label = { Text("Photo URL (Optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("product_photo_input")
                )

                Text("Category:", style = MaterialTheme.typography.labelMedium)
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories.size) { index ->
                        val cat = categories[index]
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("product_desc_input")
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Highlight as Daily Special")
                    Switch(checked = isDailySpecial, onCheckedChange = { isDailySpecial = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    val mrpVal = mrpPriceStr.toDoubleOrNull() ?: 0.0
                    val qty = qtyStr.toIntOrNull() ?: 0
                    val minQty = minOrderQtyStr.toIntOrNull() ?: 1
                    val lotSize = itemsPerLotStr.toIntOrNull() ?: 10

                    if (name.isNotBlank()) {
                        val newOrUpdated = (product ?: ProductEntity(
                            name = name.trim(),
                            price = price
                        )).copy(
                            name = name.trim(),
                            photoUrl = photoUrl.trim(),
                            price = price,
                            mrpPrice = mrpVal,
                            isMrpEnabled = mrpEnabled,
                            availableQuantity = qty,
                            description = description.trim(),
                            category = category,
                            searchKeywords = searchKeywords.trim(),
                            isDailySpecial = isDailySpecial,
                            availabilityMode = availabilityMode,
                            minOrderQuantity = minQty,
                            itemsPerLot = lotSize
                        )
                        onSaveProduct(newOrUpdated)
                    }
                },
                modifier = Modifier.testTag("save_product_btn")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (product != null) {
                    TextButton(
                        onClick = { onDelete(product) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun SellerOrderDetailDialog(
    orderId: Long,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onChangeDelivery: (OrderEntity) -> Unit
) {
    val orderWithItemsFlow = remember(orderId) { viewModel.repository.getOrderWithItemsFlow(orderId) }
    val orderWithItems by orderWithItemsFlow.collectAsState(initial = null)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(orderWithItems?.order?.orderNumber ?: "Order Details") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                orderWithItems?.let { data ->
                    Text("Buyer: ${data.order.buyerName}", fontWeight = FontWeight.Bold)
                    Text("Phone: ${data.order.buyerPhone}")
                    Text("Address: ${data.order.deliveryAddress}")
                    if (data.order.deliveryNotes.isNotBlank()) {
                        Text("Notes: ${data.order.deliveryNotes}", color = AmberAccent)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = getDeliveryIcon(data.order.deliveryService),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Assigned Delivery Service:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (data.order.deliveryService.isNotBlank()) data.order.deliveryService else "Vikas Own Vehicle Service (Default)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (data.order.deliveryDetails.isNotBlank()) {
                                Text(
                                    "Vehicle/Note: ${data.order.deliveryDetails}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = { onChangeDelivery(data.order) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Change Delivery Service / Vehicle", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Order Items:", fontWeight = FontWeight.Bold)
                    data.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.quantity}x ${item.productName}")
                            Text("₹${String.format("%.2f", item.productPrice * item.quantity)}")
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount:", fontWeight = FontWeight.Bold)
                        Text("₹${String.format("%.2f", data.order.totalPrice)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                } ?: Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SmartCenteredLoadingAnimation(
                        title = "Loading details",
                        subtitle = null
                    )
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
fun DeliverySelectionDialog(
    order: OrderEntity,
    targetStatus: String,
    onDismiss: () -> Unit,
    onConfirm: (deliveryService: String, deliveryDetails: String) -> Unit
) {
    var selectedService by remember {
        mutableStateOf(
            if (order.deliveryService.isNotBlank()) order.deliveryService
            else "Vikas Own Vehicle Service"
        )
    }
    var customDetails by remember { mutableStateOf(order.deliveryDetails) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Select Delivery Service", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Order #${order.orderNumber} • ${order.buyerName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Choose service/vehicle to deliver this order:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup()
                ) {
                    deliveryOptionsList.forEach { option ->
                        val isSelected = selectedService == option.title
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedService = option.title }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedService = option.title }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = option.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = option.description,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                OutlinedTextField(
                    value = customDetails,
                    onValueChange = { customDetails = it },
                    label = { Text("Vehicle No. / Bus No. / Driver Phone (Optional)") },
                    placeholder = { Text("e.g. Bus No 402 / MP09-AB-1234, Driver: 9876543210") },
                    maxLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delivery_details_input")
                )

                Text(
                    "Buyer will see this delivery method in their Order Status screen!",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedService, customDetails)
                },
                modifier = Modifier.testTag("confirm_delivery_btn")
            ) {
                Text(
                    if (targetStatus == order.status) "Save Delivery Mode"
                    else "Confirm & $targetStatus"
                )
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
fun UpdateTrackDialog(
    order: OrderEntity,
    onDismiss: () -> Unit,
    onSaveTracking: (String, String, String) -> Unit
) {
    var transportName by remember { mutableStateOf(order.courierName.ifBlank { "Vikas Local Transport Service" }) }
    var transportVehicleName by remember { mutableStateOf(order.trackingNumber.ifBlank { "MP-04-TR-8821 / Bus Driver 9826012345" }) }
    var trackingStatus by remember { mutableStateOf(order.trackingStatus.ifBlank { "Dispatched from Nagpur Depot - In Transit" }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Update Transport Tracking", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Order #${order.orderNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = transportName,
                    onValueChange = { transportName = it },
                    label = { Text("Transport Name") },
                    placeholder = { Text("e.g. Vikas MP Transport / Apex Bus Cargo") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("transport_name_input")
                )

                OutlinedTextField(
                    value = transportVehicleName,
                    onValueChange = { transportVehicleName = it },
                    label = { Text("Transport Vehicle Name / Number") },
                    placeholder = { Text("e.g. Bus No. MP-04-1234 / Driver Ramesh 9826012345") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("transport_vehicle_input")
                )

                OutlinedTextField(
                    value = trackingStatus,
                    onValueChange = { trackingStatus = it },
                    label = { Text("Tracking Status Note") },
                    placeholder = { Text("e.g. Package loaded in bus trunk, expected arrival 5 PM") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("tracking_status_input")
                )

                Text(
                    "Buyer will see Transport Name & Vehicle Name on their live Order Status screen!",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveTracking(transportVehicleName, transportName, trackingStatus) },
                modifier = Modifier.testTag("save_tracking_btn")
            ) {
                Text("Save Transport Tracking")
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
fun OrderCallVerificationDialog(
    order: OrderEntity,
    onDismiss: () -> Unit,
    onVerificationResult: (Boolean) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Confirm Order via Phone Call", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Order #${order.orderNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Buyer Name: ${order.buyerName}", fontWeight = FontWeight.Bold)
                        Text("Buyer Phone: ${order.buyerPhone}", style = MaterialTheme.typography.bodyMedium)
                        Text("Delivery Address: ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Button(
                    onClick = {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${order.buyerPhone}"))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("call_buyer_btn")
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("📞 Call Buyer First")
                }

                HorizontalDivider()

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Is this order ordered by mistake?",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "Ask buyer on call if they ordered by mistake. If YES, order will be automatically cancelled. If NO, order will be accepted.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { onVerificationResult(true) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f).testTag("call_verify_yes_btn")
                            ) {
                                Text("YES (Mistake)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { onVerificationResult(false) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier.weight(1f).testTag("call_verify_no_btn")
                            ) {
                                Text("NO (Valid Order)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SellerBuyerPaymentMethodsTab(viewModel: MainViewModel) {
    val routes by viewModel.allRoutes.collectAsState()

    val routePincodes = remember(routes) {
        routes.flatMap { it.journeyPincodesCsv.split(",").map { p -> p.trim() } }
            .filter { it.isNotBlank() }
            .distinct()
    }

    var selectedCodPincodes by remember { mutableStateOf(routePincodes.toSet()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manage Buyer Payment Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Configure Cash on Delivery (COD) eligibility for buyer pincodes.", style = MaterialTheme.typography.bodySmall)
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PinDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("1. Who is Eligible for Cash on Delivery (COD)", fontWeight = FontWeight.Bold)
                }
                Text("Select active transport route pincodes where buyers are allowed to choose COD payment:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (routePincodes.isEmpty()) {
                    Text("No route pincodes configured. Admin can add routes in Admin Transport Settings.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        routePincodes.forEach { pin ->
                            val isSelected = selectedCodPincodes.contains(pin)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCodPincodes = if (isSelected) selectedCodPincodes - pin else selectedCodPincodes + pin
                                    viewModel.showMessage("COD settings updated for Pincode $pin")
                                },
                                label = { Text("Pincode $pin", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                leadingIcon = if (isSelected) {
                                    @Composable { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("2. Others (Unserviceable or Special COD Requests)", fontWeight = FontWeight.Bold)
                }
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "For this contact seller to manage COD options",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Buyers outside designated transport routes must directly contact Vikas Agriculture Store seller desk to arrange special cash-on-delivery or custom transport shipment.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SellerLotManagementTab(viewModel: MainViewModel) {
    val products by viewModel.allProducts.collectAsState()
    val routes by viewModel.allRoutes.collectAsState()
    val orders by viewModel.sellerOrders.collectAsState()

    var editingProductLot by remember { mutableStateOf<ProductEntity?>(null) }

    val pendingLotOrders = remember(orders) {
        orders.filter { it.lotCalculationMode == "BY_SELLER" || it.lotStatus == "PENDING_SELLER_LOT" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Widgets, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lot & Wholesale Packaging Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Seller sells products in form of lots (boxes). Delivery charges for buyers are calculated according to lot size and route transport fare per lot.", style = MaterialTheme.typography.bodySmall)
            }
        }

        // Section: Buyer Orders Pending Seller Lot Calculation
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("seller_lot_calc_orders_section")
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "📦 Buyer Orders Awaiting Seller Lot Calculation (${pendingLotOrders.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Text(
                    "Review buyer item list & quantity below to calculate packaging lots before final order payment.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (pendingLotOrders.isEmpty()) {
                    Text(
                        "No buyer orders currently waiting for seller lot calculation.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontStyle = FontStyle.Italic
                    )
                } else {
                    pendingLotOrders.forEach { ord ->
                        SellerOrderLotCalculationCard(order = ord, viewModel = viewModel, routes = routes)
                    }
                }
            }
        }

        Text("Product Lot Packaging Catalog", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

        if (products.isEmpty()) {
            Text("No products in catalog.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        } else {
            products.forEach { prod ->
                val totalAvailableLots = if (prod.itemsPerLot > 0) prod.availableQuantity / prod.itemsPerLot else 0
                val sampleRoute = routes.firstOrNull()
                val estDeliveryChargePerLot = sampleRoute?.farePerLot ?: 25.0

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            AssistChip(
                                onClick = {},
                                label = { Text("Mode: ${prod.availabilityMode}", fontSize = 10.sp) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Items per Lot (1 Box):", fontSize = 11.sp, color = Color.Gray)
                                Text("${prod.itemsPerLot} units", fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Min. Order Qty:", fontSize = 11.sp, color = Color.Gray)
                                Text("${prod.minOrderQuantity} unit(s)", fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Available Lots:", fontSize = 11.sp, color = Color.Gray)
                                Text("$totalAvailableLots Box(es)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Delivery Charge / Lot: \$${String.format("%.2f", estDeliveryChargePerLot)} (Route Fare)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            OutlinedButton(
                                onClick = { editingProductLot = prod },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("edit_lot_btn_${prod.id}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Lot", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    editingProductLot?.let { prod ->
        var itemsPerLotStr by remember { mutableStateOf(prod.itemsPerLot.toString()) }
        var minOrderQtyStr by remember { mutableStateOf(prod.minOrderQuantity.toString()) }

        AlertDialog(
            onDismissRequest = { editingProductLot = null },
            title = { Text("Edit Lot Config for ${prod.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = itemsPerLotStr,
                        onValueChange = { itemsPerLotStr = it },
                        label = { Text("Items per Lot (Box size)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = minOrderQtyStr,
                        onValueChange = { minOrderQtyStr = it },
                        label = { Text("Minimum Order Quantity (Units)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lotSize = itemsPerLotStr.toIntOrNull() ?: prod.itemsPerLot
                        val minQty = minOrderQtyStr.toIntOrNull() ?: prod.minOrderQuantity
                        viewModel.updateProduct(prod.copy(itemsPerLot = lotSize, minOrderQuantity = minQty))
                        editingProductLot = null
                    }
                ) {
                    Text("Save Lot Config")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingProductLot = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SellerVyaparSummaryTab(viewModel: MainViewModel) {
    val orders by viewModel.sellerOrders.collectAsState()

    val totalRevenue = remember(orders) {
        orders.filter { it.status != "CANCELLED" }.sumOf { it.totalPrice }
    }
    val totalOrdersCount = orders.size
    val pendingOrdersCount = orders.count { it.status == "PENDING" }
    val deliveredOrdersCount = orders.count { it.status == "DELIVERED" }

    val latePaymentOrders = remember(orders) {
        orders.filter { it.isLatePaymentAllowed || it.paymentMethod.contains("OTHER", ignoreCase = true) }
    }

    val totalLotsProcessed = remember(orders) {
        orders.sumOf { it.totalLots }
    }
    val totalDeliveryFeeRevenue = remember(orders) {
        orders.sumOf { it.deliveryFee }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Summary / Vyapar Business Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Track overall store revenue, order volume, late payments & lot transport dispatches.", style = MaterialTheme.typography.bodySmall)
            }
        }

        // Key Business Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Total Revenue", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("₹${String.format("%.2f", totalRevenue)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Total Orders", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$totalOrdersCount", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Pending Orders", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$pendingOrdersCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AmberAccent)
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Delivered Orders", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$deliveredOrdersCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2E7D32))
                }
            }
        }

        // Late Payment & Special Payment Tracker Section
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Late Payment & Special Payment Requests", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }

                if (latePaymentOrders.isEmpty()) {
                    Text("No late payment or custom payment requests currently active.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    latePaymentOrders.forEach { ord ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Order #${ord.orderNumber}", fontWeight = FontWeight.Bold)
                                    Text("₹${String.format("%.2f", ord.totalPrice)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Text("Buyer: ${ord.buyerName} (${ord.buyerPhone})", style = MaterialTheme.typography.bodySmall)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isConverted = ord.isLatePaymentAllowed
                                    Text(
                                        if (isConverted) "Special / Late Payment APPROVED" else "Requested via OTHER payment method",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isConverted) Color(0xFF2E7D32) else AmberAccent
                                    )

                                    OutlinedButton(
                                        onClick = { viewModel.setOrderLatePaymentAllowed(ord.id, !isConverted) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (isConverted) "Revoke Late Pay" else "Approve Late Pay", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Lots & Delivery Charge Summary
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Packaging Lots & Transport Revenue", fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Total Lots Packed", fontSize = 11.sp, color = Color.Gray)
                        Text("$totalLotsProcessed Box(es)", fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Delivery Fees Collected", fontSize = 11.sp, color = Color.Gray)
                        Text("₹${String.format("%.2f", totalDeliveryFeeRevenue)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun SellerOrderLotCalculationCard(
    order: OrderEntity,
    viewModel: MainViewModel,
    routes: List<RouteDetailEntity>
) {
    val orderWithItemsFlow = remember(order.id) { viewModel.repository.getOrderWithItemsFlow(order.id) }
    val orderWithItems by orderWithItemsFlow.collectAsState(initial = null)

    val defaultFare = remember(routes) { routes.firstOrNull()?.farePerLot ?: 150.0 }
    
    val suggestedLots = remember(orderWithItems) {
        orderWithItems?.items?.sumOf { item ->
            kotlin.math.ceil(item.quantity.toDouble() / 10.0).toInt()
        } ?: 1
    }

    var lotsInput by remember(order.id, suggestedLots) { 
        mutableStateOf(if (order.totalLots > 0 && order.lotStatus == "CALCULATED") order.totalLots.toString() else suggestedLots.coerceAtLeast(1).toString()) 
    }
    var fareInput by remember(order.id, defaultFare) { mutableStateOf(defaultFare.toString()) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (order.lotStatus == "PENDING_SELLER_LOT" || order.lotStatus == "PENDING") MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("seller_order_lot_calc_card_${order.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Order #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Buyer: ${order.buyerName} • ${order.buyerPhone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    color = if (order.lotStatus == "CALCULATED") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        if (order.lotStatus == "CALCULATED") "Lots Calculated: ${order.totalLots}" else "Awaiting Seller Lot Calculation",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.lotStatus == "CALCULATED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text("Payment Method: ${order.paymentMethod} • Status: ${order.status}", fontSize = 11.sp, fontWeight = FontWeight.Medium)

            HorizontalDivider()

            Text("Buyer Ordered Item List & Quantity:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

            orderWithItems?.items?.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("• ${item.quantity}x ${item.productName}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("₹${String.format("%.2f", item.productPrice * item.quantity)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            } ?: Text("Loading item list...", fontSize = 11.sp, color = Color.Gray)

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = lotsInput,
                    onValueChange = { lotsInput = it },
                    label = { Text("Lots Count") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("input_lots_count_${order.id}")
                )

                OutlinedTextField(
                    value = fareInput,
                    onValueChange = { fareInput = it },
                    label = { Text("Fare / Lot (₹)") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("input_fare_per_lot_${order.id}")
                )
            }

            val lotsVal = lotsInput.toIntOrNull() ?: 1
            val fareVal = fareInput.toDoubleOrNull() ?: 150.0
            val calculatedFee = lotsVal * fareVal

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Calculated Delivery Fee:", fontSize = 11.sp, color = Color.Gray)
                    Text("₹${String.format("%.2f", calculatedFee)} ($lotsVal Lot(s) @ ₹${fareVal.toInt()})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Button(
                    onClick = {
                        viewModel.sellerUpdateOrderLotCount(order.id, lotsVal, fareVal)
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("save_order_lots_btn_${order.id}")
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Calculate & Assign Lots", fontSize = 11.sp)
                }
            }
        }
    }
}
