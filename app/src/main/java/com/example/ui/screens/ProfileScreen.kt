package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class ProfileTab(val title: String, val icon: ImageVector) {
    EDIT_PROFILE("Edit Profile", Icons.Default.Person),
    DEVICES("Manage Devices", Icons.Default.PhoneAndroid),
    ADDRESSES("Save Address", Icons.Default.LocationOn),
    NOTIFICATIONS("Notification Centre", Icons.Default.Notifications),
    ACTIVITY("My Activity", Icons.Default.History)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val devices by viewModel.userDevices.collectAsState()
    val addresses by viewModel.savedAddresses.collectAsState()
    val notificationPrefs by viewModel.notificationPreferences.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val buyerOrders by viewModel.buyerOrders.collectAsState()
    val inquiries by viewModel.allDirectCallInquiries.collectAsState()

    var selectedTab by remember { mutableStateOf(ProfileTab.EDIT_PROFILE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Account & Profile Settings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        user?.let { u ->
                            Text(
                                text = "${u.name} • ${u.role}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // User Header Card
            if (user != null) {
                val u = user!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "User Avatar",
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = u.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = u.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "📞 ${u.phone}${if (u.secondaryPhone.isNotBlank()) " | Alt: ${u.secondaryPhone}" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonOutline,
                                contentDescription = "Guest Profile",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Guest Profile Setup Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Fill out your contact details & farm shipping address below without logging in.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Options Scrollable Navigation Rail / Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfileTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        modifier = Modifier.testTag("profile_tab_${tab.name.lowercase()}"),
                        text = { Text(tab.title) },
                        icon = { Icon(tab.icon, contentDescription = tab.title) }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    ProfileTab.EDIT_PROFILE -> EditProfileSection(user, viewModel)
                    ProfileTab.DEVICES -> ManageDevicesSection(devices, viewModel)
                    ProfileTab.ADDRESSES -> SaveAddressSection(user, addresses, viewModel)
                    ProfileTab.NOTIFICATIONS -> NotificationCentreSection(notificationPrefs, notifications, viewModel)
                    ProfileTab.ACTIVITY -> MyActivitySection(buyerOrders, inquiries)
                }
            }
        }
    }
}

@Composable
private fun EditProfileSection(
    user: UserEntity?,
    viewModel: MainViewModel
) {
    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var phone by remember(user) { mutableStateOf(user?.phone ?: "") }
    var secondaryPhone by remember(user) { mutableStateOf(user?.secondaryPhone ?: "") }
    var address by remember(user) { mutableStateOf(user?.address ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "A. Edit Profile & Contact Numbers",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Keep your farm details and min 2 contact numbers up to date for delivery & seller direct calls.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name / Farm Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_name_input"),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Primary Phone Number *") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_phone_input"),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = secondaryPhone,
                onValueChange = { secondaryPhone = it },
                label = { Text("Secondary / Alternate Phone Number (Required for 2 contacts)") },
                leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                supportingText = { Text("Provides an alternate number if primary line is unreachable during delivery.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_sec_phone_input"),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Primary Farm / Store Address") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_address_input"),
                minLines = 2
            )
        }

        errorMsg?.let { err ->
            item {
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        item {
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) {
                        errorMsg = "Name and primary phone are required."
                    } else {
                        errorMsg = null
                        viewModel.updateUserProfile(name, phone, secondaryPhone, address)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_profile_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Profile Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ManageDevicesSection(
    devices: List<UserDeviceEntity>,
    viewModel: MainViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "B. Manage Signed-In Devices",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Monitor active devices logged into your Vikas Agri account. Revoke session access if unrecognized.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(devices) { dev ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (dev.isCurrent) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (dev.deviceName.contains("Tablet")) Icons.Default.Tablet else Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = dev.deviceName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            if (dev.isCurrent) {
                                Spacer(modifier = Modifier.width(8.dp))
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text("This Device", fontSize = 10.sp) }
                                )
                            }
                        }
                        Text(
                            text = "${dev.model} • IP: ${dev.ipAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Last active: ${dev.lastActive}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (!dev.isCurrent) {
                        IconButton(
                            onClick = { viewModel.revokeDevice(dev.id) },
                            modifier = Modifier.testTag("revoke_device_${dev.id}")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Revoke Device",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SaveAddressSection(
    user: UserEntity?,
    addresses: List<SavedAddressEntity>,
    viewModel: MainViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "C. Saved Delivery Addresses",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Save farm, store, or warehouse locations with min 2 phone numbers for smooth checkout.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_new_address_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddLocation, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Delivery Address")
            }
        }

        if (addresses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.LocationOff, contentDescription = null, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No saved secondary addresses yet.", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Primary address: ${user?.address ?: "Not set"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(addresses) { addr ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (addr.isDefault) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = if (addr.isDefault) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = addr.label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                if (addr.isDefault) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Default") }
                                    )
                                }
                            }

                            Row {
                                if (!addr.isDefault) {
                                    TextButton(onClick = { viewModel.setDefaultAddress(addr.id) }) {
                                        Text("Set Default")
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteSavedAddress(addr.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Address", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Text(text = "Contact: ${addr.fullName}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "📞 Primary: ${addr.phone}${if (addr.secondaryPhone.isNotBlank()) " | Alt: ${addr.secondaryPhone}" else ""}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "📍 ${addr.fullAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAddressDialog(
            defaultName = user?.name ?: "",
            defaultPhone = user?.phone ?: "",
            defaultSecPhone = user?.secondaryPhone ?: "",
            onDismiss = { showAddDialog = false },
            onSave = { label, name, phone, secPhone, address, isDefault ->
                viewModel.saveAddress(label, name, phone, secPhone, address, isDefault)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddAddressDialog(
    defaultName: String,
    defaultPhone: String,
    defaultSecPhone: String,
    onDismiss: () -> Unit,
    onSave: (label: String, name: String, phone: String, secPhone: String, address: String, isDefault: Boolean) -> Unit
) {
    var label by remember { mutableStateOf("Farm Address") }
    var name by remember { mutableStateOf(defaultName) }
    var phone by remember { mutableStateOf(defaultPhone) }
    var secPhone by remember { mutableStateOf(defaultSecPhone) }
    var address by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Delivery Address (2 Contacts Required)") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (e.g. Main Farm, Store #2)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact Person Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Primary Phone *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = secPhone,
                    onValueChange = { secPhone = it },
                    label = { Text("Secondary / Alternate Phone *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Full Farm / Shipping Address *") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                    Text("Set as default delivery address")
                }

                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank() || secPhone.isBlank() || address.isBlank()) {
                        error = "Please fill all fields, including 2 phone numbers."
                    } else {
                        onSave(label, name, phone, secPhone, address, isDefault)
                    }
                }
            ) {
                Text("Save Address")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun NotificationCentreSection(
    pref: NotificationPreferenceEntity?,
    notifications: List<NotificationEntity>,
    viewModel: MainViewModel
) {
    var mahaSell by remember(pref) { mutableStateOf(pref?.mahaSellAlerts ?: true) }
    var tenPercentOff by remember(pref) { mutableStateOf(pref?.tenPercentOffAlerts ?: true) }
    var orderUpdates by remember(pref) { mutableStateOf(pref?.orderUpdates ?: true) }
    var callReminders by remember(pref) { mutableStateOf(pref?.callReminders ?: true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "D. Notification Centre",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Manage notifications like MahaSell alerts, 10% discount offers, order tracking updates, and direct call seller reminders.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Notification Alert Preferences", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    NotificationToggleRow(
                        title = "🔥 MahaSell Agri Specials",
                        subtitle = "Alerts for seasonal equipment discount sales & heavy machinery auctions",
                        checked = mahaSell,
                        onCheckedChange = {
                            mahaSell = it
                            viewModel.updateNotificationPreferences(mahaSell, tenPercentOff, orderUpdates, callReminders)
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    NotificationToggleRow(
                        title = "🏷️ 10% Off & Coupon Offers",
                        subtitle = "Exclusive monthly agricultural promo codes and stock clearing deals",
                        checked = tenPercentOff,
                        onCheckedChange = {
                            tenPercentOff = it
                            viewModel.updateNotificationPreferences(mahaSell, tenPercentOff, orderUpdates, callReminders)
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    NotificationToggleRow(
                        title = "📦 Order Tracking & Status",
                        subtitle = "Instant updates when orders are accepted, shipped, or updated with tracking numbers",
                        checked = orderUpdates,
                        onCheckedChange = {
                            orderUpdates = it
                            viewModel.updateNotificationPreferences(mahaSell, tenPercentOff, orderUpdates, callReminders)
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    NotificationToggleRow(
                        title = "📞 Seller Direct Call Order Follow-ups",
                        subtitle = "Reminders when seller posts your direct phone call order into SFCMP",
                        checked = callReminders,
                        onCheckedChange = {
                            callReminders = it
                            viewModel.updateNotificationPreferences(mahaSell, tenPercentOff, orderUpdates, callReminders)
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Recent Notifications Log", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (notifications.isEmpty()) {
            item {
                Text("No notification history yet.", style = MaterialTheme.typography.bodySmall)
            }
        } else {
            items(notifications) { n ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(n.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val dateStr = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(n.timestamp))
                            Text(dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(n.message, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun MyActivitySection(
    orders: List<OrderEntity>,
    inquiries: List<DirectCallInquiryEntity>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "E. My Activity",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Track your recent account interactions, direct call seller inquiries, and order submissions.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${orders.size}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Total Orders", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${inquiries.size}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Direct Calls", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val sfcmpCount = orders.count { it.isDirectCallOrder }
                        Text("$sfcmpCount", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                        Text("SFCMP Orders", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            Text("📞 Direct Call Inquiries Log", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (inquiries.isEmpty()) {
            item {
                Text("No phone call inquiry records found.", style = MaterialTheme.typography.bodySmall)
            }
        } else {
            items(inquiries) { inq ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Agent: ${inq.agentName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val dateStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(inq.timestamp))
                            Text("Called ${inq.calledNumber} on $dateStr", style = MaterialTheme.typography.bodySmall)
                            Text("Status: ${inq.status}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
