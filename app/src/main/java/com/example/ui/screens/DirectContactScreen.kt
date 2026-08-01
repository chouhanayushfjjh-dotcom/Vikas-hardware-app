package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DirectCallInquiryEntity
import com.example.ui.components.VikasLogoBadge
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectContactScreen(
    viewModel: MainViewModel,
    onNavigateToCatalog: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val context = LocalContext.current
    val contactConfig by viewModel.directContactConfig.collectAsState()
    val buyerInquiries by viewModel.buyerCallInquiries.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showCallSuccessDialog by remember { mutableStateOf(false) }
    var lastCalledAgent by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhoneInTalk,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Direct Phone Call Contact",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Call Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("direct_call_hero_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Call,
                                contentDescription = "Call",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Call Seller Agent & Order Directly",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Prefer talking on call? Connect directly with seller agent ${contactConfig.agentName} to place stock orders or ask product questions over phone call.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Big Call Button
                        Button(
                            onClick = {
                                lastCalledAgent = contactConfig.agentName
                                // Trigger Dial Intent
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contactConfig.phoneNumber}"))
                                    context.startActivity(dialIntent)
                                } catch (e: Exception) {
                                    // Fallback if no dialer in environment
                                }
                                // Notify seller in SFCMP
                                viewModel.recordDirectCallInquiry(
                                    agentName = contactConfig.agentName,
                                    calledNumber = contactConfig.phoneNumber
                                ) {
                                    showCallSuccessDialog = true
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("call_seller_agent_btn")
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Call Agent (${contactConfig.phoneNumber})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Contact Information Details Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Seller Direct Contact Info",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Contact Agent Name", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(contactConfig.agentName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Direct Phone Number", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(contactConfig.phoneNumber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Call Hours", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(contactConfig.availableHours, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Phone Ordering Instructions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(contactConfig.notes, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // How Phone Call Ordering Works
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "How Direct Phone Call Ordering Works",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        StepRow(number = "1", text = "Click 'Call Agent' above to speak directly with the Vikas seller representative.")
                        Spacer(modifier = Modifier.height(8.dp))
                        StepRow(number = "2", text = "Tell the seller agent which products and quantities you need from the stock catalog.")
                        Spacer(modifier = Modifier.height(8.dp))
                        StepRow(number = "3", text = "Seller creates the order in Seller Direct Contact Management Page (SFCMP) for you.")
                        Spacer(modifier = Modifier.height(8.dp))
                        StepRow(number = "4", text = "Order appears immediately in your account under 'Orders' with phone call details.")
                    }
                }
            }

            // Recent Direct Phone Call Inquiries History for Buyer
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Phone Call History & SFCMP Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToOrders) {
                        Text("View Orders", fontSize = 12.sp)
                    }
                }
            }

            if (buyerInquiries.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No phone calls initiated yet. Tap 'Call Agent' above to contact seller!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(buyerInquiries) { inquiry ->
                    DirectCallInquiryCard(
                        inquiry = inquiry,
                        onViewOrders = onNavigateToOrders
                    )
                }
            }
        }
    }

    if (showCallSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showCallSuccessDialog = false },
            icon = {
                Icon(
                    Icons.Default.PhoneCallback,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    "Direct Contact Notification Sent!",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Notification sent to seller in Seller Direct Contact Management Page (SFCMP).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "If you gave an order on call to agent $lastCalledAgent, the seller will create the order in SFCMP and it will show in your account under 'Orders'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCallSuccessDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun StepRow(number: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
fun DirectCallInquiryCard(
    inquiry: DirectCallInquiryEntity,
    onViewOrders: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()) }
    val dateStr = remember(inquiry.timestamp) { dateFormat.format(Date(inquiry.timestamp)) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Call to ${inquiry.agentName}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SFCMP Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Seller SFCMP Status:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                when (inquiry.status) {
                    "PENDING_SELLER_RESPONSE" -> {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Awaiting Seller SFCMP Confirmation",
                                color = Color(0xFF92400E),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    "ORDER_CREATED" -> {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Direct Phone Order Created ✓",
                                color = Color(0xFF166534),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    else -> {
                        Surface(
                            color = Color(0xFFF3F4F6),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Phone Call Only (No Order Placed)",
                                color = Color(0xFF4B5563),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            if (inquiry.status == "ORDER_CREATED") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onViewOrders,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Order Details", fontSize = 11.sp)
                }
            }
        }
    }
}
