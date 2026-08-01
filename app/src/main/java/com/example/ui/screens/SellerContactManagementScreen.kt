package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.DirectCallInquiryEntity
import com.example.data.DirectContactConfigEntity
import com.example.data.ProductEntity
import com.example.ui.components.VikasLogoBadge
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerContactManagementScreen(
    viewModel: MainViewModel,
    onNavigateToDashboard: () -> Unit
) {
    val contactConfig by viewModel.directContactConfig.collectAsState()
    val allInquiries by viewModel.allDirectCallInquiries.collectAsState()
    val pendingInquiries by viewModel.pendingDirectCallInquiries.collectAsState()
    val stockProducts by viewModel.allProducts.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Inquiries & Orders, 1 = Contact Settings

    var showCreateOrderDialog by remember { mutableStateOf(false) }
    var selectedInquiryForOrder by remember { mutableStateOf<DirectCallInquiryEntity?>(null) }

    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.testTag("sfcmp_threeline_logo_btn")
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "SFCMP Menu",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "SDCMP Desk",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Seller Direct Contact & order management",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.width(260.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("1. Direct Call Inquiries List", fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    menuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("2. Create Multi-Item Order", fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    menuExpanded = false
                                    selectedInquiryForOrder = null
                                    showCreateOrderDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("3. Customer & Agent Chat Answers", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.navigateTo(com.example.viewmodel.Screen.ChatSupport)
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("4. Return to Seller Dashboard", fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.Dashboard, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToDashboard()
                                }
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToDashboard) {
                        Icon(Icons.Default.Dashboard, contentDescription = "Dashboard")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedInquiryForOrder = null
                    showCreateOrderDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("sfcmp_create_order_fab")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.PhoneCallback, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Direct Call Order", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // SFCMP Tab Navigation Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.testTag("sfcmp_tab_inquiries")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (pendingInquiries.isNotEmpty()) {
                                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                                        Text(pendingInquiries.size.toString(), color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Direct Call Inquiries (${allInquiries.size})", fontWeight = FontWeight.Bold)
                    }
                }

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.testTag("sfcmp_tab_settings")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.ContactPhone, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Contact & Agent Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (selectedTab == 0) {
                // SFCMP Inquiries & Order Creation List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // SFCMP Banner
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Direct Contact Order Management",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "When buyers initiate a direct phone call, verify whether they placed an order on call. If YES, create the order from stock items directly into SFCMP.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (pendingInquiries.isNotEmpty()) {
                        item {
                            Text(
                                text = "⚠️ Action Required: Pending Call Confirmation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        items(pendingInquiries) { inquiry ->
                            SfcmpInquiryCard(
                                inquiry = inquiry,
                                onCreateOrderForInquiry = {
                                    selectedInquiryForOrder = inquiry
                                    showCreateOrderDialog = true
                                },
                                onMarkNoOrder = {
                                    viewModel.respondToDirectCallInquiry(inquiry.id, isOrderPlaced = false)
                                }
                            )
                        }
                    }

                    item {
                        Text(
                            text = "All Direct Call History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (allInquiries.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No direct phone call inquiries received yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        val processedInquiries = allInquiries.filter { it.status != "PENDING_SELLER_RESPONSE" }
                        items(processedInquiries) { inquiry ->
                            SfcmpInquiryCard(
                                inquiry = inquiry,
                                onCreateOrderForInquiry = {
                                    selectedInquiryForOrder = inquiry
                                    showCreateOrderDialog = true
                                },
                                onMarkNoOrder = {
                                    viewModel.respondToDirectCallInquiry(inquiry.id, isOrderPlaced = false)
                                }
                            )
                        }
                    }
                }
            } else {
                // Contact Settings Tab
                SfcmpContactSettingsForm(
                    currentConfig = contactConfig,
                    onSave = { updated ->
                        viewModel.saveDirectContactConfig(updated)
                    }
                )
            }
        }
    }

    if (showCreateOrderDialog) {
        CreateDirectCallOrderDialog(
            inquiry = selectedInquiryForOrder,
            stockProducts = stockProducts,
            onDismiss = { showCreateOrderDialog = false },
            onSubmitOrder = { buyerName, buyerPhone, address, notes, itemsWithQty, agentName ->
                viewModel.createMultiItemSfcmpOrder(
                    buyerName = buyerName,
                    buyerPhone = buyerPhone,
                    deliveryAddress = address,
                    deliveryNotes = notes,
                    selectedItemsWithQty = itemsWithQty,
                    agentName = agentName,
                    inquiryId = selectedInquiryForOrder?.id
                ) {
                    showCreateOrderDialog = false
                }
            }
        )
    }
}

@Composable
fun SfcmpInquiryCard(
    inquiry: DirectCallInquiryEntity,
    onCreateOrderForInquiry: () -> Unit,
    onMarkNoOrder: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()) }
    val dateStr = remember(inquiry.timestamp) { dateFormat.format(Date(inquiry.timestamp)) }
    val isPending = inquiry.status == "PENDING_SELLER_RESPONSE"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPending) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPending) 3.dp else 1.dp),
        border = if (isPending) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier.fillMaxWidth().testTag("sfcmp_inquiry_card_${inquiry.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isPending) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outlineVariant,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PhoneInTalk,
                            contentDescription = null,
                            tint = if (isPending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = inquiry.buyerName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Phone: ${inquiry.buyerPhone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Called Agent: ${inquiry.agentName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Status: ${inquiry.status}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (inquiry.status) {
                            "ORDER_CREATED" -> Color(0xFF15803D)
                            "NO_ORDER" -> Color.Gray
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            }

            if (isPending) {
                Spacer(modifier = Modifier.height(12.dp))

                // Mandatory Prompt Requirement:
                // "send a notification for seller, that direct CONTACT buyer is give order or not on call. if seller go for yes then buyer information is go to seller [SFCMP] and buyer make order in [SFCMP] of same quantity and product as it is taken on call from buyers"
                Text(
                    text = "Did direct CONTACT buyer give order or not on call?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onMarkNoOrder,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inquiry_no_order_btn_${inquiry.id}")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("No Order")
                    }

                    Button(
                        onClick = onCreateOrderForInquiry,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("inquiry_yes_create_order_btn_${inquiry.id}")
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Yes, Create Order", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SfcmpContactSettingsForm(
    currentConfig: DirectContactConfigEntity,
    onSave: (DirectContactConfigEntity) -> Unit
) {
    var phoneNumber by remember(currentConfig) { mutableStateOf(currentConfig.primaryPhone) }
    var agentName by remember(currentConfig) { mutableStateOf(currentConfig.agentName) }
    var availableHours by remember(currentConfig) { mutableStateOf(currentConfig.workingHours) }
    var notes by remember(currentConfig) { mutableStateOf(currentConfig.notes) }

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
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Manage Direct Phone Call Contact (SFCMP)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Buyers will call this agent and phone number when accessing the 'Direct Phone Call Contact' page in the buyer app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = agentName,
                    onValueChange = { agentName = it },
                    label = { Text("Seller Agent Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sfcmp_agent_name_input")
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Direct Phone Call Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sfcmp_phone_input")
                )

                OutlinedTextField(
                    value = availableHours,
                    onValueChange = { availableHours = it },
                    label = { Text("Call Availability Hours") },
                    leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sfcmp_hours_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Phone Call Instructions for Buyers") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sfcmp_notes_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onSave(
                            currentConfig.copy(
                                primaryPhone = phoneNumber.trim(),
                                agentName = agentName.trim(),
                                workingHours = availableHours.trim(),
                                notes = notes.trim()
                            )
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("sfcmp_save_settings_btn")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Contact Settings", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDirectCallOrderDialog(
    inquiry: DirectCallInquiryEntity?,
    stockProducts: List<ProductEntity>,
    onDismiss: () -> Unit,
    onSubmitOrder: (
        buyerName: String,
        buyerPhone: String,
        deliveryAddress: String,
        deliveryNotes: String,
        selectedItemsWithQty: List<Pair<ProductEntity, Int>>,
        agentName: String
    ) -> Unit
) {
    val availableProducts = remember(stockProducts) { stockProducts.filter { it.isAvailable && it.availableQuantity > 0 } }

    var buyerName by remember { mutableStateOf(inquiry?.buyerName ?: "Ramesh Kumar (Farmer)") }
    var buyerPhone by remember { mutableStateOf(inquiry?.buyerPhone ?: "+91 98765 43210") }
    var buyerAddress by remember { mutableStateOf("Farm House #12, Agricultural Zone, District Vikas") }
    var deliveryNotes by remember { mutableStateOf("Taken on phone call by seller agent") }
    var agentName by remember { mutableStateOf(inquiry?.agentName ?: "Vikas Support Agent") }

    // Map of Product ID to selected Quantity
    val selectedQuantities = remember { mutableStateMapOf<Long, Int>() }

    // Calculate total subtotal
    val subtotal = selectedQuantities.entries.sumOf { (prodId, qty) ->
        val prod = availableProducts.find { it.id == prodId }
        (prod?.price ?: 0.0) * qty
    }
    val grandTotal = if (subtotal > 0) subtotal + 2.50 else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SFCMP Multi-Stock Direct Order", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select one or multiple in-stock items with quantities. Order will be linked to buyer's account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Buyer Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("sfcmp_dialog_buyer_name")
                )

                OutlinedTextField(
                    value = buyerPhone,
                    onValueChange = { buyerPhone = it },
                    label = { Text("Buyer Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("sfcmp_dialog_buyer_phone")
                )

                OutlinedTextField(
                    value = agentName,
                    onValueChange = { agentName = it },
                    label = { Text("Handling Seller Agent Name") },
                    leadingIcon = { Icon(Icons.Default.SupportAgent, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("sfcmp_dialog_agent_name")
                )

                Text("Select In-Stock Items & Quantities:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                if (availableProducts.isEmpty()) {
                    Text("No products in stock available.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableProducts.forEach { product ->
                            val currentQty = selectedQuantities[product.id] ?: 0
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (currentQty > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(product.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Available Stock: ${product.availableQuantity} | Price: \$${String.format("%.2f", product.price)}", style = MaterialTheme.typography.bodySmall)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                if (currentQty > 0) {
                                                    val newQty = currentQty - 1
                                                    if (newQty == 0) selectedQuantities.remove(product.id)
                                                    else selectedQuantities[product.id] = newQty
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease")
                                        }

                                        Text(
                                            text = currentQty.toString(),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )

                                        IconButton(
                                            onClick = {
                                                if (currentQty < product.availableQuantity) {
                                                    selectedQuantities[product.id] = currentQty + 1
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (subtotal > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Items Subtotal:", style = MaterialTheme.typography.bodyMedium)
                                Text("\$${String.format("%.2f", subtotal)}", fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Delivery Fee:", style = MaterialTheme.typography.bodyMedium)
                                Text("\$2.50", fontWeight = FontWeight.SemiBold)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Grand Total:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("\$${String.format("%.2f", grandTotal)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = buyerAddress,
                    onValueChange = { buyerAddress = it },
                    label = { Text("Delivery Address") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("sfcmp_dialog_address")
                )

                OutlinedTextField(
                    value = deliveryNotes,
                    onValueChange = { deliveryNotes = it },
                    label = { Text("Direct Call Order Notes") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("sfcmp_dialog_notes")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val itemsWithQty = selectedQuantities.mapNotNull { (prodId, qty) ->
                        val prod = availableProducts.find { it.id == prodId }
                        if (prod != null && qty > 0) Pair(prod, qty) else null
                    }
                    if (itemsWithQty.isNotEmpty()) {
                        onSubmitOrder(
                            buyerName.ifBlank { "Phone Customer" },
                            buyerPhone.ifBlank { "+91 98765 00000" },
                            buyerAddress,
                            deliveryNotes,
                            itemsWithQty,
                            agentName
                        )
                    }
                },
                enabled = selectedQuantities.values.any { it > 0 },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("sfcmp_submit_order_btn")
            ) {
                Text("Confirm & Create Multi-Item Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
