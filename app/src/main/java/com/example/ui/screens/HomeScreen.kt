package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProductEntity
import com.example.ui.components.CategoryChip
import com.example.ui.components.ProductCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberAccentLight
import com.example.util.LocalizationManager
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigate: (Screen) -> Unit
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isSeller by viewModel.isSeller.collectAsState()

    val categories = listOf("All", "Tools & Equipment", "Irrigation & Pumps", "Fertilizers & Soil", "Crop Protection", "Fencing & Hardware", "Machinery & Parts", "Seeds & Seedlings")

    val dailySpecials = remember(filteredProducts) {
        filteredProducts.filter { it.isDailySpecial }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_grid")
    ) {
        // 1. Seller Store Status Banner
        item(span = { GridItemSpan(2) }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("store_banner_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF16A34A), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "HARDWARE STORE OPEN TODAY",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Vikas Agriculture & Hardware Store",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Agricultural tools",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Search Field
        item(span = { GridItemSpan(2) }) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search (e.g. फावड़ा, Sprayer, Hoe, Pump)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_field")
                )

                // Quick Search Suggestions for Buyers (no # required)
                val quickSuggestions = listOf("फावड़ा", "Sprayer", "Pump", "खाद", "Irrigation", "Seeds", "कुदळ", "Hoe")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Text(
                        "Search Terms:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(quickSuggestions) { term ->
                            AssistChip(
                                onClick = { viewModel.searchQuery.value = term },
                                label = { Text(term, fontSize = 10.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Category Filter Chips
        item(span = { GridItemSpan(2) }) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    CategoryChip(
                        category = category,
                        isSelected = selectedCategory == category,
                        onSelect = { viewModel.selectedCategory.value = category }
                    )
                }
            }
        }

        // 4. Role Info Banner if Guest or Seller
        if (currentUser == null) {
            item(span = { GridItemSpan(2) }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AmberAccentLight),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = AmberAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Browsing as Guest. Log in to place your daily order.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        TextButton(
                            onClick = { onNavigate(Screen.Auth) },
                            modifier = Modifier.testTag("guest_login_button")
                        ) {
                            Text("Log In", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 5. Today's Specials Header & List
        if (dailySpecials.isNotEmpty() && searchQuery.isBlank() && selectedCategory == "All") {
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⭐ Today's Daily Specials",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { onNavigate(Screen.Catalog) }) {
                        Text("View All")
                    }
                }
            }

            item(span = { GridItemSpan(2) }) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(dailySpecials, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onProductClick = { onNavigate(Screen.ProductDetail(it)) },
                            onAddToCart = { viewModel.addToCart(it) },
                            modifier = Modifier.width(220.dp),
                            isSeller = isSeller
                        )
                    }
                }
            }
        }

        // 6. Main Items Header
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (selectedCategory == "All") "Available Today (${filteredProducts.size})" else "$selectedCategory (${filteredProducts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 7. Products List
        if (filteredProducts.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No items found today.",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Try searching for another keyword or check back later.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

        // Footer Support Callout
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Screen.ContactSupport) },
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Need help with your order?", fontWeight = FontWeight.Bold)
                        Text("Contact the seller directly or view operating hours", style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}
