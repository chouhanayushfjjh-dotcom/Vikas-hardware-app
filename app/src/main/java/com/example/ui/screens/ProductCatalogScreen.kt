package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.ProductCard
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.delay

@Composable
fun ShimmerSkeletonBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(Color.LightGray.copy(alpha = alpha))
    )
}

@Composable
fun HeroBannerSkeleton() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("hero_banner_skeleton")
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ShimmerSkeletonBox(modifier = Modifier.width(100.dp).height(16.dp), shape = RoundedCornerShape(4.dp))
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerSkeletonBox(modifier = Modifier.fillMaxWidth(0.85f).height(20.dp), shape = RoundedCornerShape(4.dp))
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerSkeletonBox(modifier = Modifier.fillMaxWidth(0.65f).height(14.dp), shape = RoundedCornerShape(4.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            ShimmerSkeletonBox(modifier = Modifier.size(70.dp), shape = RoundedCornerShape(12.dp))
        }
    }
}

@Composable
fun ProductCardSkeleton() {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("product_card_skeleton")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            ShimmerSkeletonBox(modifier = Modifier.fillMaxWidth().height(105.dp), shape = RoundedCornerShape(10.dp))
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerSkeletonBox(modifier = Modifier.width(60.dp).height(12.dp), shape = RoundedCornerShape(4.dp))
            Spacer(modifier = Modifier.height(6.dp))
            ShimmerSkeletonBox(modifier = Modifier.fillMaxWidth(0.9f).height(16.dp), shape = RoundedCornerShape(4.dp))
            Spacer(modifier = Modifier.height(4.dp))
            ShimmerSkeletonBox(modifier = Modifier.fillMaxWidth(0.5f).height(14.dp), shape = RoundedCornerShape(4.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerSkeletonBox(modifier = Modifier.width(55.dp).height(18.dp), shape = RoundedCornerShape(4.dp))
                ShimmerSkeletonBox(modifier = Modifier.size(32.dp), shape = CircleShape)
            }
        }
    }
}

data class CategoryMenuItem(
    val name: String,
    val icon: ImageVector,
    val badge: String? = null
)

val categoryMenuItems = listOf(
    CategoryMenuItem("All", Icons.Default.GridView),
    CategoryMenuItem("Farm Machinery", Icons.Default.PrecisionManufacturing),
    CategoryMenuItem("Irrigation", Icons.Default.WaterDrop),
    CategoryMenuItem("Tools", Icons.Default.Build),
    CategoryMenuItem("Power Tools", Icons.Default.ElectricBolt, "Hot"),
    CategoryMenuItem("Electrical", Icons.Default.Power),
    CategoryMenuItem("Plumbing", Icons.Default.Plumbing),
    CategoryMenuItem("Safety Equipment", Icons.Default.HealthAndSafety),
    CategoryMenuItem("Gardening", Icons.Default.Yard),
    CategoryMenuItem("Livestock Supplies", Icons.Default.Pets),
    CategoryMenuItem("Spare Parts", Icons.Default.SettingsSuggest)
)

@Composable
fun ProductCatalogScreen(
    viewModel: MainViewModel,
    onNavigate: (Screen) -> Unit
) {
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val availableOnly by viewModel.availableOnlyFilter.collectAsState()
    val isSeller by viewModel.isSeller.collectAsState()
    val categoryConfig by viewModel.categoryPageConfig.collectAsState()
    val cartCount by viewModel.cartItemCount.collectAsState()
    val unreadNotifications by viewModel.unreadNotificationCount.collectAsState()

    var showScannerDialog by remember { mutableStateOf(false) }
    var isCategoryLoading by remember { mutableStateOf(true) }

    LaunchedEffect(selectedCategory, searchQuery) {
        isCategoryLoading = true
        delay(350)
        isCategoryLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Header Bar
        Surface(
            color = Color.White,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("Search Machinery, Tools, Hardware...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF6F8FA),
                            focusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("category_search_field")
                    )

                    // Scanner Icon
                    IconButton(
                        onClick = { showScannerDialog = true },
                        modifier = Modifier.testTag("category_scanner_icon")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR/Barcode", tint = MaterialTheme.colorScheme.primary)
                    }

                    // Cart Icon
                    IconButton(
                        onClick = { onNavigate(Screen.Cart) },
                        modifier = Modifier.testTag("category_cart_icon")
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge { Text(cartCount.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Main Split Body: Left Vertical Menu + Right Dynamic Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Left Vertical Category Navigation Menu
            Surface(
                color = Color(0xFFF8F9FA),
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(categoryMenuItems, key = { it.name }) { item ->
                        val isSelected = selectedCategory == item.name || (selectedCategory == "All" && item.name == "All")
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color.White else Color.Transparent
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (isSelected) 3.dp else 0.dp
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectedCategory.value = if (item.name == "All") "All" else item.name
                                }
                                .testTag("left_cat_item_${item.name}")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .padding(vertical = 10.dp, horizontal = 4.dp)
                                    .fillMaxWidth()
                            ) {
                                Box {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.name,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    if (item.badge != null) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.error,
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.align(Alignment.TopEnd)
                                        ) {
                                            Text(
                                                item.badge,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }

            VerticalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            // Right Dynamic Content Area
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(Color.White)
            ) {
                // Large Hero Banner Card
                item(span = { GridItemSpan(2) }) {
                    if (isCategoryLoading) {
                        HeroBannerSkeleton()
                    } else {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = categoryConfig?.promoTagline ?: "OFFER OF THE MONTH",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = categoryConfig?.heroBannerTitle ?: "Agri & Hardware Deals",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = categoryConfig?.heroBannerSubtitle ?: "Up to 35% OFF on Genuine Seeds & Tools",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }

                                AsyncImage(
                                    model = categoryConfig?.heroBannerImageUrl ?: "https://images.unsplash.com/photo-1592417817098-8f3d6ef23a8d?w=300",
                                    contentDescription = "Hero Deals",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }
                }

                // In-Stock Filter Switch Header
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (selectedCategory == "All") "Spotlight Products" else "$selectedCategory Items",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("In-Stock Only", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = availableOnly,
                                onCheckedChange = { viewModel.availableOnlyFilter.value = it },
                                modifier = Modifier.scale(0.8f).testTag("category_instock_toggle")
                            )
                        }
                    }
                }

                if (isCategoryLoading) {
                    items(6) {
                        ProductCardSkeleton()
                    }
                } else if (filteredProducts.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Category,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No products found in this category.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Try selecting 'All' or clearing search filters.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onProductClick = { onNavigate(Screen.ProductDetail(it)) },
                            onAddToCart = { viewModel.addToCart(it) },
                            isSeller = isSeller
                        )
                    }
                }
            }
        }
    }

    if (showScannerDialog) {
        AlertDialog(
            onDismissRequest = { showScannerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Product Scanner")
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CenterFocusWeak, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Scan product QR code or barcode to quick search items in Vikas Marketplace.", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(onClick = {
                    showScannerDialog = false
                    viewModel.searchQuery.value = "Pesticides"
                }) {
                    Text("Simulate Scan (Pesticides)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScannerDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.padding(0.dp)
)
