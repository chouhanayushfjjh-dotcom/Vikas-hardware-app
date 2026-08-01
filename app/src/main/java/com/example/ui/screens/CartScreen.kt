package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.LargeActionButton
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen

@Composable
fun CartScreen(
    viewModel: MainViewModel,
    onNavigate: (Screen) -> Unit
) {
    val activeCartItems by viewModel.activeCartItems.collectAsState()
    val savedForLaterItems by viewModel.savedForLaterItems.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val deliveryFee = if (activeCartItems.isNotEmpty()) 2.50 else 0.0
    val total = subtotal + deliveryFee

    if (currentUser == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Your Cart",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Please log in or sign up to view your cart and place orders.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onNavigate(Screen.Auth) },
                modifier = Modifier.testTag("cart_login_prompt_btn")
            ) {
                Text("Log In or Sign Up")
            }
        }
        return
    }

    if (activeCartItems.isEmpty() && savedForLaterItems.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.RemoveShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Your Cart is Empty",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Explore available agriculture hardware & tools from Vikas sellers!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onNavigate(Screen.Catalog) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("browse_catalog_btn")
            ) {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Browse Items")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Your Order Cart",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${activeCartItems.sumOf { it.quantity }} active items • ${savedForLaterItems.size} saved for later",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Cart Items List & Saved For Later
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (activeCartItems.isNotEmpty()) {
                item {
                    Text("Active Cart Items", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }

                items(activeCartItems, key = { "active_${it.cartItemId}" }) { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cart_item_${item.cartItemId}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = item.photoUrl,
                                    contentDescription = item.productName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.productName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "₹${String.format("%.2f", item.price)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "MRP ₹${String.format("%.2f", item.price * 1.25)}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                            ),
                                            color = Color.Gray
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Quantity controls
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedIconButton(
                                            onClick = { viewModel.updateCartQuantity(item, item.quantity - 1) },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .testTag("cart_qty_minus_${item.cartItemId}")
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                                        }

                                        Text(
                                            text = item.quantity.toString(),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp)
                                        )

                                        OutlinedIconButton(
                                            onClick = { viewModel.updateCartQuantity(item, item.quantity + 1) },
                                            enabled = item.quantity < item.maxQuantity,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .testTag("cart_qty_plus_${item.cartItemId}")
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    val itemTotal = item.price * item.quantity
                                    Text(
                                        text = "₹${String.format("%.2f", itemTotal)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))

                            // 3 Explicit Action Buttons: Remove Options, Save for Later, Buy Now
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.removeCartItem(item.cartItemId) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("remove_option_${item.cartItemId}"),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remove Option", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.toggleSaveForLater(item.cartItemId, true) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("save_for_later_${item.cartItemId}"),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save for later", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { onNavigate(Screen.Checkout) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("buy_now_${item.cartItemId}"),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Buy Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Saved For Later Section
            if (savedForLaterItems.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("📌 Saved for Later (${savedForLaterItems.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
                }

                items(savedForLaterItems, key = { "saved_${it.cartItemId}" }) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("saved_item_${item.cartItemId}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = item.photoUrl,
                                contentDescription = item.productName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.productName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("₹${String.format("%.2f", item.price)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }

                            Row {
                                TextButton(
                                    onClick = { viewModel.toggleSaveForLater(item.cartItemId, false) },
                                    modifier = Modifier.testTag("move_to_cart_${item.cartItemId}")
                                ) {
                                    Text("Move to Cart", fontSize = 11.sp)
                                }
                                IconButton(
                                    onClick = { viewModel.removeCartItem(item.cartItemId) }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (activeCartItems.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            // Order Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Order Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Items Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${String.format("%.2f", subtotal)}", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Express Local Delivery Fee", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${String.format("%.2f", deliveryFee)}", fontWeight = FontWeight.SemiBold)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Order Amount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("₹${String.format("%.2f", total)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LargeActionButton(
                text = "Proceed to Checkout • ₹${String.format("%.2f", total)}",
                icon = Icons.Default.ArrowForward,
                onClick = { onNavigate(Screen.Checkout) },
                modifier = Modifier.testTag("proceed_to_checkout_btn")
            )
        }
    }
}
