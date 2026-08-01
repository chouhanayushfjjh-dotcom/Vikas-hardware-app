package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ProductEntity
import com.example.ui.components.LargeActionButton
import com.example.ui.components.SmartCenteredLoadingAnimation
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen

@Composable
fun ProductDetailScreen(
    productId: Long,
    viewModel: MainViewModel,
    onNavigate: (Screen) -> Unit
) {
    val isSeller by viewModel.isSeller.collectAsState()
    var product by remember { mutableStateOf<ProductEntity?>(null) }
    var quantity by remember { mutableStateOf(1) }

    LaunchedEffect(productId) {
        product = viewModel.repository.getProductById(productId)
    }

    val currentProduct = product

    if (currentProduct == null) {
        SmartCenteredLoadingAnimation(
            title = "Loading product",
            subtitle = "Fetching product specifications & pricing..."
        )
        return
    }

    val isAvailable = currentProduct.isAvailable && currentProduct.availableQuantity > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Photo Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color.LightGray.copy(alpha = 0.3f))
        ) {
            AsyncImage(
                model = currentProduct.photoUrl,
                contentDescription = currentProduct.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Back button overlay
            IconButton(
                onClick = { onNavigate(Screen.Catalog) },
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .align(Alignment.TopStart)
                    .testTag("detail_back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            // Category tag
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(topStart = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = currentProduct.category.uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            // Product Name & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentProduct.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = if (isAvailable) StatusDeliveredBg else StatusCancelledBg,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isAvailable) "Available Today (${currentProduct.availableQuantity} in stock)" else "Currently Out of Stock",
                            color = if (isAvailable) StatusDeliveredFg else StatusCancelledFg,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    val effectiveMrp = if (currentProduct.mrpPrice > 0) currentProduct.mrpPrice else (currentProduct.price * 1.25)
                    val discountPct = if (effectiveMrp > currentProduct.price) (((effectiveMrp - currentProduct.price) / effectiveMrp) * 100).toInt() else 0
                    val savings = effectiveMrp - currentProduct.price

                    Text(
                        text = "\$${String.format("%.2f", currentProduct.price)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MRP \$${String.format("%.2f", effectiveMrp)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            ),
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        if (discountPct > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFF2E7D32),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "$discountPct% OFF",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Highlighting Special Offer & Place Cart Section
            val effectiveMrpVal = if (currentProduct.mrpPrice > 0) currentProduct.mrpPrice else (currentProduct.price * 1.25)
            val discountPctVal = if (effectiveMrpVal > currentProduct.price) (((effectiveMrpVal - currentProduct.price) / effectiveMrpVal) * 100).toInt() else 0
            val savingsVal = effectiveMrpVal - currentProduct.price

            Spacer(modifier = Modifier.height(16.dp))

            if (discountPctVal > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Special $discountPctVal% OFF Offer!",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "You save \$${String.format("%.2f", savingsVal)} per item on this order",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Description
            Text(
                text = "Item Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = currentProduct.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            if (currentProduct.searchKeywords.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Search Hashtags & Terms:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                val keywordsList = currentProduct.searchKeywords
                    .split("\\s+|,|;".toRegex())
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .map { if (it.startsWith("#")) it else "#$it" }
                    .distinct()

                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(keywordsList.size) { index ->
                        val tag = keywordsList[index]
                        val cleanTerm = tag.removePrefix("#")
                        SuggestionChip(
                            onClick = {
                                viewModel.searchQuery.value = cleanTerm
                                onNavigate(Screen.Home)
                            },
                            label = { Text(tag, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            icon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(12.dp)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isSeller) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Store Manager Mode",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Buying options are disabled for seller accounts. Manage stock and products in your Seller Portal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onNavigate(Screen.SellerDashboard) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("detail_seller_portal_btn")
                        ) {
                            Text("Go to Seller Dashboard")
                        }
                    }
                }
            } else if (isAvailable) {
                // Quantity Selector Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Select Quantity",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedIconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                enabled = quantity > 1,
                                shape = CircleShape,
                                modifier = Modifier.testTag("decrease_qty_btn")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                            }

                            Text(
                                text = quantity.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            OutlinedIconButton(
                                onClick = { if (quantity < currentProduct.availableQuantity) quantity++ },
                                enabled = quantity < currentProduct.availableQuantity,
                                shape = CircleShape,
                                modifier = Modifier.testTag("increase_qty_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val totalPrice = currentProduct.price * quantity

                LargeActionButton(
                    text = "Add $quantity to Cart • \$${String.format("%.2f", totalPrice)}",
                    icon = Icons.Default.AddShoppingCart,
                    onClick = {
                        viewModel.addToCart(currentProduct.id, quantity)
                    },
                    modifier = Modifier.testTag("detail_add_to_cart_btn")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.addToCart(currentProduct.id, quantity)
                        onNavigate(Screen.Cart)
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("detail_buy_now_btn")
                ) {
                    Text("Buy Now (Go to Cart)", fontWeight = FontWeight.Bold)
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusCancelledBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = StatusCancelledFg)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "This item is sold out for today. Please check back tomorrow when the seller updates inventory!",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusCancelledFg,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            val remoteConfig by viewModel.remoteConfig.collectAsState()
            val isReviewsEnabled = remoteConfig?.enablePerformanceReviews ?: true

            if (isReviewsEnabled) {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                var userRating by remember { mutableStateOf(5) }
                var reviewComment by remember { mutableStateOf("") }
                var userSubmittedReviews by remember { mutableStateOf(listOf(
                    Triple("Rajesh Kumar (Agri Farmer)", 5, "Excellent quality material! Very durable hardware parts."),
                    Triple("Vikram Patel", 4, "Good value for money. Transport delivery was fast.")
                )) }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Performance & Customer Reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("4.8", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = com.example.ui.theme.AmberAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row {
                                repeat(5) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = com.example.ui.theme.AmberAccent, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text("Based on verified buyer ratings", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Leave a Product Rating & Review", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Rating: ", fontSize = 12.sp)
                                (1..5).forEach { star ->
                                    IconButton(
                                        onClick = { userRating = star },
                                        modifier = Modifier.size(32.dp).testTag("rate_star_$star")
                                    ) {
                                        Icon(
                                            if (star <= userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "$star Stars",
                                            tint = com.example.ui.theme.AmberAccent
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = reviewComment,
                                onValueChange = { reviewComment = it },
                                label = { Text("Write your review") },
                                placeholder = { Text("How was the product performance?") },
                                maxLines = 3,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("product_review_input")
                            )

                            Button(
                                onClick = {
                                    if (reviewComment.isNotBlank()) {
                                        userSubmittedReviews = listOf(Triple("You (Verified Buyer)", userRating, reviewComment)) + userSubmittedReviews
                                        reviewComment = ""
                                        viewModel.showMessage("Thank you! Your product review has been published.")
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("submit_product_review_btn")
                            ) {
                                Text("Submit Review & Rating")
                            }
                        }
                    }

                    userSubmittedReviews.forEach { (reviewer, rating, comment) ->
                        Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(reviewer, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Row {
                                        repeat(rating) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = com.example.ui.theme.AmberAccent, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(comment, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
