package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OrderWithItems
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
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun OrderStatusScreen(
    orderId: Long,
    viewModel: MainViewModel,
    onNavigate: (Screen) -> Unit
) {
    val orderWithItemsFlow = remember(orderId) { viewModel.repository.getOrderWithItemsFlow(orderId) }
    val orderData by orderWithItemsFlow.collectAsState(initial = null)

    val currentOrderData = orderData

    if (currentOrderData == null) {
        SmartCenteredLoadingAnimation(
            title = "Loading order status",
            subtitle = "Retrieving dispatch & tracking details..."
        )
        return
    }

    val order = currentOrderData.order
    val items = currentOrderData.items

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onNavigate(Screen.OrderHistory) },
                modifier = Modifier.testTag("status_back_btn")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Order Status", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            StatusBadge(order.status)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Order Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Order #${order.orderNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                val dateStr = SimpleDateFormat("EEEE, MMM dd • hh:mm a", Locale.getDefault()).format(Date(order.createdAt))
                Text(dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Timeline Stepper Visual
                OrderTimelineStepper(status = order.status)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seller Dispatch & Delivery Service Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("buyer_delivery_service_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getDeliveryIcon(order.deliveryService),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "Delivery Service (Selected by Seller)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (order.deliveryService.isNotBlank()) order.deliveryService else "Vikas Own Vehicle Service (Default)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (order.deliveryDetails.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Vehicle / Dispatch Note: ${order.deliveryDetails}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Delivery Address Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (order.isDirectCallOrder) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.PhoneInTalk,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "📞 Direct Phone Call Contact Order",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    "Order created in SFCMP via phone call with agent: ${order.directCallAgent.ifBlank { "Vikas Agent" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delivery Address", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(order.buyerName, fontWeight = FontWeight.SemiBold)
                Text(order.buyerPhone, style = MaterialTheme.typography.bodyMedium)
                Text(order.deliveryAddress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (order.deliveryNotes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Note: ${order.deliveryNotes}", style = MaterialTheme.typography.bodySmall, color = AmberAccent)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ordered Items List
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Items Ordered (${items.sumOf { it.quantity }})", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${item.quantity}x ${item.productName}", style = MaterialTheme.typography.bodyMedium)
                        Text("₹${String.format("%.2f", item.productPrice * item.quantity)}", fontWeight = FontWeight.SemiBold)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal")
                    Text("₹${String.format("%.2f", order.subtotalPrice)}")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Delivery Fee")
                    Text(
                        if (order.deliveryPaymentOption == "PAY_TO_TRANSPORT_DIRECTLY") {
                            if (order.deliveryFee > 0.0) "₹${String.format("%.2f", order.deliveryFee)} (Pay to Transport Direct)"
                            else "Pay to Transport Direct (Lot info given by seller at dispatch)"
                        } else "₹${String.format("%.2f", order.deliveryFee)}"
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total (${order.paymentMethod})", fontWeight = FontWeight.Bold)
                    Text("₹${String.format("%.2f", order.totalPrice)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                if (order.deliveryPaymentOption == "PAY_TO_TRANSPORT_DIRECTLY") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (order.deliveryFee > 0.0)
                                    "🚚 Delivery Charge Note: Pay ₹${String.format("%.2f", order.deliveryFee)} directly to transport driver at delivery time."
                                else
                                    "🚚 Delivery Charge Note: Pay delivery charge directly to transport according to lot size (Information given by seller at time of order shipping or dispatch).",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tracking Information Card (if updated by seller)
        if (order.trackingNumber.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("buyer_tracking_info_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Live Shipment Tracking Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Courier: ${order.courierName.ifBlank { "Vikas Express Transport" }}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Tracking #: ${order.trackingNumber}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    if (order.trackingStatus.isNotBlank()) {
                        Text("Status: ${order.trackingStatus}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Cancel Order Option (Allowed only before shipping: PENDING or ACCEPTED status)
        if (order.status == "PENDING" || order.status == "ACCEPTED") {
            OutlinedButton(
                onClick = { viewModel.cancelOrder(order.id) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("cancel_order_status_btn")
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel Order (Before Shipping)", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Direct Contact Seller Button
        LargeActionButton(
            text = "Contact Seller for Order Support",
            icon = Icons.Default.SupportAgent,
            onClick = { onNavigate(Screen.ContactSupport) },
            modifier = Modifier.testTag("contact_seller_btn")
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { onNavigate(Screen.Home) },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("back_to_home_btn")
        ) {
            Text("Return to Home")
        }
    }
}

@Composable
fun OrderTimelineStepper(status: String) {
    val steps = listOf(
        Pair("PENDING", "Order Placed"),
        Pair("ACCEPTED", "Accepted"),
        Pair("PACKED", "Packed"),
        Pair("SHIPPED", "In Transit"),
        Pair("DELIVERED", "Delivered")
    )

    val currentStepIndex = when (status.uppercase()) {
        "PENDING" -> 0
        "ACCEPTED" -> 1
        "PACKED" -> 2
        "SHIPPED" -> 3
        "DELIVERED" -> 4
        else -> -1 // Cancelled
    }

    if (status == "CANCELLED") {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("This order was cancelled.", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, pair ->
            val isCompleted = index <= currentStepIndex
            val isCurrent = index == currentStepIndex

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = if (isCompleted) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.Circle,
                        contentDescription = null,
                        tint = if (isCompleted) Color.White else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pair.second,
                    fontSize = 9.sp,
                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (isCompleted) MaterialTheme.colorScheme.primary else Color.Gray,
                    maxLines = 1
                )
            }
        }
    }
}
