package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LargeActionButton
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen

@Composable
fun CheckoutScreen(
    viewModel: MainViewModel,
    onNavigate: (Screen) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()

    var name by remember(currentUser) { mutableStateOf(currentUser?.name ?: "") }
    var primaryPhone by remember(currentUser) { mutableStateOf(currentUser?.phone ?: "") }
    var secondaryPhone by remember(currentUser) { mutableStateOf(currentUser?.secondaryPhone ?: "") }
    var address by remember(currentUser) { mutableStateOf(currentUser?.address ?: "") }
    var deliveryNotes by remember { mutableStateOf("") }

    val paymentOptions = listOf("Cash on Delivery", "Mobile Wallet / UPI", "OTHER (Contact Seller)")
    var selectedPaymentMethod by remember { mutableStateOf(paymentOptions[0]) }

    val remoteConfig by viewModel.remoteConfig.collectAsState()
    val deliveryChargeOption = remoteConfig?.deliveryChargeOption ?: if (remoteConfig?.deliveryChargeEnabled == false) "DISABLED" else if (remoteConfig?.mandatoryPayToTransport == true) "OPTION_2" else "OPTION_1"
    val lotCalculationEnabledOption1Direct = remoteConfig?.lotCalculationEnabledOption1Direct ?: true
    val lotCalculationEnabledOption2 = remoteConfig?.lotCalculationEnabledOption2 ?: true
    val farePerLot = remoteConfig?.defaultFarePerLot ?: 150.0

    val isOption1 = deliveryChargeOption == "OPTION_1"
    val isOption2 = deliveryChargeOption == "OPTION_2"
    val isDisabled = deliveryChargeOption == "DISABLED"
    val deliveryChargeEnabled = !isDisabled

    var lotCalculationMode by remember { mutableStateOf("AUTO") } // "AUTO" or "BY_SELLER"
    var userSelectedDeliveryPaymentOption by remember { mutableStateOf("PAY_TO_TRANSPORT_DIRECTLY") } // "PAY_INSTANTLY" or "PAY_TO_TRANSPORT_DIRECTLY"

    // Effective Delivery Payment Option according to Payment Mode & Admin Config
    val effectiveDeliveryPaymentOption = when {
        isDisabled -> "DISABLED"
        isOption2 -> "PAY_TO_TRANSPORT_DIRECTLY" // Option 2 forces mandatory direct transport payment
        selectedPaymentMethod == "OTHER (Contact Seller)" -> "PAY_TO_TRANSPORT_DIRECTLY" // Late Payment / Credit auto selects Pay to Transport Direct
        selectedPaymentMethod == "Cash on Delivery" -> "PAY_TO_TRANSPORT_DIRECTLY" // COD pays delivery on arrival
        else -> userSelectedDeliveryPaymentOption
    }

    val isLotCalculationEnabled = when {
        isDisabled -> false
        isOption2 -> lotCalculationEnabledOption2
        isOption1 && effectiveDeliveryPaymentOption == "PAY_TO_TRANSPORT_DIRECTLY" -> lotCalculationEnabledOption1Direct
        else -> true
    }

    // Calculate total lots automatically based on cart items lot sizes
    val calculatedTotalLots = remember(cartItems) {
        val total = cartItems.sumOf { item ->
            val itemsPerLot = if (item.itemsPerLot > 0) item.itemsPerLot else 10
            kotlin.math.ceil(item.quantity.toDouble() / itemsPerLot).toInt()
        }
        maxOf(1, total)
    }

    val calculatedDeliveryFee = when {
        isDisabled -> 0.0
        !isLotCalculationEnabled -> 0.0
        lotCalculationMode == "BY_SELLER" -> 0.0
        else -> (calculatedTotalLots * farePerLot)
    }

    val totalAddedDeliveryFee = if (isOption1 && effectiveDeliveryPaymentOption == "PAY_INSTANTLY") calculatedDeliveryFee else 0.0
    val total = subtotal + totalAddedDeliveryFee

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { onNavigate(Screen.Cart) },
                modifier = Modifier.testTag("checkout_back_btn")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Checkout Order",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Delivery Details Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. Delivery Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Buyer Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_name_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = primaryPhone,
                    onValueChange = { primaryPhone = it },
                    label = { Text("Primary Contact No. (Required 1/2)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_primary_phone_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = secondaryPhone,
                    onValueChange = { secondaryPhone = it },
                    label = { Text("Secondary Contact No. (Required 2/2)") },
                    leadingIcon = { Icon(Icons.Default.ContactPhone, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_secondary_phone_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Full Delivery Address") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_address_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = deliveryNotes,
                    onValueChange = { deliveryNotes = it },
                    label = { Text("Delivery Notes (Optional, e.g., Leave at front porch)") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_notes_input")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Payment Method Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. Select Payment Option",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                paymentOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentMethod = option }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPaymentMethod == option,
                            onClick = { selectedPaymentMethod = option }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedPaymentMethod == option) FontWeight.Bold else FontWeight.Normal
                            )
                            if (option == "OTHER (Contact Seller)") {
                                Text(
                                    "Direct seller interaction for custom terms / Special Late Payment approval",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                if (selectedPaymentMethod == "OTHER (Contact Seller)") {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "After placing order, you can call or message seller. Seller can convert this order into Special / Late Payment.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Packaging Lot & Delivery Fee Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3. Packaging Lot & Delivery Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (!isLotCalculationEnabled) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "📦 Lot Calculation Notice",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Lot size depends on the number of items ordered. Information about lot is given by seller at the time of order shipping or dispatch.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                } else {
                    Text("📦 Lot Calculation Mode:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { lotCalculationMode = "AUTO" }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = lotCalculationMode == "AUTO",
                            onClick = { lotCalculationMode = "AUTO" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Automatic Combined Lot Calculation", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Calculates $calculatedTotalLots Lot(s) required based on items size • Fare: ₹${farePerLot.toInt()}/lot = ₹${String.format("%.2f", calculatedDeliveryFee)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { lotCalculationMode = "BY_SELLER" }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = lotCalculationMode == "BY_SELLER",
                            onClick = { lotCalculationMode = "BY_SELLER" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Calculate Lot by Seller", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Seller checks buyer item list & quantity in Seller Lot Management to assign lots before order confirmation.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Delivery Payment Mode Selection
                Text("🚚 Delivery Charge Payment Mode:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))

                if (isDisabled) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Delivery Charges are currently disabled by Admin (Free Delivery). No delivery charge added.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else if (isOption2) {
                    // Option 2 (Delivery Charge_) Mandatory Direct Transport Card
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("delivery_option2_mandatory_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delivery Charge_ (Option 2 - Pay Direct to Transport)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Instruction: Pay delivery charge direct to transport. Delivery charge is NOT added to your total payment today.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (!lotCalculationEnabledOption2) {
                                Text(
                                    "• Lot size depends on no. of items ordered. Information about lot is given by seller at the time of order shipping or dispatch.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                )
                            } else if (lotCalculationMode != "BY_SELLER") {
                                Text(
                                    "• Estimated Lot Fare: ₹${String.format("%.2f", calculatedDeliveryFee)} ($calculatedTotalLots Lot(s) @ ₹${farePerLot.toInt()}/lot) - Pay directly to transport vehicle at delivery time.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                } else {
                    // Option 1 Sub-parts
                    // Sub-part A: Pay delivery charge direct to transport
                    val canSelectDirectTransport = selectedPaymentMethod == "Mobile Wallet / UPI"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = canSelectDirectTransport) {
                                userSelectedDeliveryPaymentOption = "PAY_TO_TRANSPORT_DIRECTLY"
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = effectiveDeliveryPaymentOption == "PAY_TO_TRANSPORT_DIRECTLY",
                            onClick = { if (canSelectDirectTransport) userSelectedDeliveryPaymentOption = "PAY_TO_TRANSPORT_DIRECTLY" },
                            enabled = canSelectDirectTransport || effectiveDeliveryPaymentOption == "PAY_TO_TRANSPORT_DIRECTLY"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Pay delivery charge direct to transport", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Pay on Delivery", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Text(
                                if (!lotCalculationEnabledOption1Direct) {
                                    "Pay delivery charge directly to transport driver on arrival. Lot info provided by seller at dispatch time. NOT added to total payment today."
                                } else {
                                    "Pay ₹${String.format("%.2f", calculatedDeliveryFee)} ($calculatedTotalLots Lot(s)) directly to transport driver on arrival. NOT added to total payment today."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Sub-part B: Pay instantly with item price online
                    val canSelectInstantly = selectedPaymentMethod == "Mobile Wallet / UPI"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = canSelectInstantly) {
                                userSelectedDeliveryPaymentOption = "PAY_INSTANTLY"
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = effectiveDeliveryPaymentOption == "PAY_INSTANTLY",
                            onClick = { if (canSelectInstantly) userSelectedDeliveryPaymentOption = "PAY_INSTANTLY" },
                            enabled = canSelectInstantly
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Pay instantly (with item price online)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Delivery charge ₹${String.format("%.2f", calculatedDeliveryFee)} ($calculatedTotalLots Lot(s)) is added to your online payment total today.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Helper notes for COD and Late payment
                    if (selectedPaymentMethod == "OTHER (Contact Seller)") {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                        ) {
                            Text(
                                "⚡ Late Payment / Credit orders automatically select 'Pay delivery charge direct to transport' option upon delivery.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else if (selectedPaymentMethod == "Cash on Delivery") {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                        ) {
                            Text(
                                "💵 Cash on Delivery (COD): Delivery charge is paid at the time of delivery along with item price.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Final Order Breakdown
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "4. Order Summary (${cartItems.sumOf { it.quantity }} items)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                cartItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.quantity}x ${item.productName}", style = MaterialTheme.typography.bodyMedium)
                        Text("₹${String.format("%.2f", item.price * item.quantity)}", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${String.format("%.2f", subtotal)}")
                }
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Delivery Charge", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        when {
                            isDisabled -> "₹0.00 (Disabled by Admin)"
                            !isLotCalculationEnabled -> "Pay to Transport (Lot info given by seller at dispatch)"
                            isOption2 -> "₹${String.format("%.2f", calculatedDeliveryFee)} (Pay Direct to Transport)"
                            effectiveDeliveryPaymentOption == "PAY_TO_TRANSPORT_DIRECTLY" -> "₹${String.format("%.2f", calculatedDeliveryFee)} (Direct to Transport)"
                            lotCalculationMode == "BY_SELLER" -> "Calculated by Seller (In Lot Management)"
                            else -> "₹${String.format("%.2f", calculatedDeliveryFee)} ($calculatedTotalLots Lot${if (calculatedTotalLots > 1) "s" else ""})"
                        },
                        fontWeight = if (effectiveDeliveryPaymentOption == "PAY_TO_TRANSPORT_DIRECTLY" || isOption2 || lotCalculationMode == "BY_SELLER") FontWeight.Bold else FontWeight.Normal,
                        color = if (effectiveDeliveryPaymentOption == "PAY_TO_TRANSPORT_DIRECTLY" || isOption2 || lotCalculationMode == "BY_SELLER") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Amount Payable Today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if ((effectiveDeliveryPaymentOption == "PAY_TO_TRANSPORT_DIRECTLY" || isOption2) && deliveryChargeEnabled) {
                            Text("(Items price payable now • Delivery fee paid to transport)", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                        } else if (lotCalculationMode == "BY_SELLER" && selectedPaymentMethod == "Mobile Wallet / UPI") {
                            Text("(Item subtotal today • Seller calculates lots in Lot Management)", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text("₹${String.format("%.2f", total)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                // Mandatory Notice for Lot Calculation by Seller with Online Payment
                if (lotCalculationMode == "BY_SELLER" && selectedPaymentMethod == "Mobile Wallet / UPI") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("seller_lot_calc_online_notice_card")
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "📦 Seller Lot Calculation Required for Online Payment",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    "You selected 'Calculate Lot by Seller'. Placing this order sends your item list & quantity to the seller. The seller will check your items in the Seller Lot Management page to calculate and assign lots before final payment confirmation.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }

                // Mandatory Buyer Message for Direct Transport Payment
                if ((effectiveDeliveryPaymentOption == "PAY_TO_TRANSPORT_DIRECTLY" || isOption2) && deliveryChargeEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("direct_transport_buyer_message_card")
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "🚚 Buyer Notice: Pay Delivery Charge directly to Transport",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                val noticeMessage = if (!isLotCalculationEnabled) {
                                    "Lot size depends on the number of items ordered. Information about lot is given by seller at the time of order shipping or dispatch. Please pay delivery charge directly to transport at the time of delivery."
                                } else if (lotCalculationMode == "BY_SELLER") {
                                    "Lot information and delivery fee will be provided by seller at the time of dispatch. Please pay delivery charge directly to transport driver on arrival."
                                } else {
                                    "Please pay delivery charge of ₹${String.format("%.2f", calculatedDeliveryFee)} ($calculatedTotalLots Lot(s) @ ₹${farePerLot.toInt()}/lot) directly to transport driver/vehicle at the time of delivery."
                                }
                                Text(
                                    noticeMessage,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LargeActionButton(
            text = if (lotCalculationMode == "BY_SELLER") "Send Order Request for Seller Lot Calculation" else "Confirm & Place Order",
            icon = Icons.Default.CheckCircle,
            onClick = {
                if (name.isBlank() || primaryPhone.isBlank() || secondaryPhone.isBlank() || address.isBlank()) {
                    viewModel.showMessage("Please enter your name, address, and minimum 2 contact numbers (Primary & Secondary phone).")
                    return@LargeActionButton
                }

                val combinedPhone = "$primaryPhone / Alt: $secondaryPhone"

                viewModel.submitOrder(
                    buyerName = name,
                    buyerPhone = combinedPhone,
                    deliveryAddress = address,
                    deliveryNotes = deliveryNotes,
                    paymentMethod = selectedPaymentMethod,
                    calculatedDeliveryFee = calculatedDeliveryFee,
                    calculatedTotalLots = calculatedTotalLots,
                    lotCalculationMode = if (!isLotCalculationEnabled) "DISABLED" else lotCalculationMode,
                    deliveryPaymentOption = effectiveDeliveryPaymentOption,
                    onOrderSuccess = { orderId ->
                        // Navigation handled in VM
                    }
                )
            },
            modifier = Modifier.testTag("submit_final_order_btn")
        )
    }
}
