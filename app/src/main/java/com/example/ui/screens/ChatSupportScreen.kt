package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.ChatMessageEntity
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSupportScreen(
    viewModel: MainViewModel,
    onNavigate: (Screen) -> Unit
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isSeller by viewModel.isSeller.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val remoteConfig by viewModel.remoteConfig.collectAsState()

    val isReviewsEnabled = remoteConfig?.enablePerformanceReviews ?: true
    var messageInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    if (!isReviewsEnabled && currentUser == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Account Login Required",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Live chat support is restricted to verified logged-in buyer accounts. Please log in or register to start a chat session.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onNavigate(Screen.Auth) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("chat_login_required_btn")
            ) {
                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log In / Register Account", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat Header Top Bar
        Surface(
            tonalElevation = 4.dp,
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onNavigate(Screen.Home) },
                        modifier = Modifier.testTag("chat_back_btn")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Box {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.SupportAgent,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isSeller) "Seller-Buyer Live Chat Portal" else "Live Support & Seller Chat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Online • Instant Response",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = if (isSeller) "SELLER" else "BUYER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Chat Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages, key = { it.id }) { msg ->
                val isSelf = msg.senderId == (currentUser?.id ?: 0L) ||
                        (isSeller && msg.senderRole == "SELLER") ||
                        (!isSeller && msg.senderRole == "BUYER")

                val bubbleBg = if (isSelf) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }

                val textColor = if (isSelf) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
                    ) {
                        if (!isSelf) {
                            Icon(
                                if (msg.senderRole == "SELLER") Icons.Default.Storefront else Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "${msg.senderName} (${msg.senderRole})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isSelf) 16.dp else 4.dp,
                            bottomEnd = if (isSelf) 4.dp else 16.dp
                        ),
                        color = bubbleBg,
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(msg.timestamp))
                            Text(
                                text = timeStr,
                                fontSize = 9.sp,
                                color = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }

        // Quick Preset Suggestions Row
        val quickPresets = if (isSeller) {
            listOf("How can I assist your order today?", "Your order is packed and ready!", "Direct call order confirmed.")
        } else {
            listOf("When will my order arrive?", "Is product in stock?", "Can I change delivery address?")
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickPresets) { preset ->
                        AssistChip(
                            onClick = { messageInput = preset },
                            label = { Text(preset, fontSize = 10.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // Chat Input Box
        Surface(
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text(if (isSeller) "Reply to buyer..." else "Ask seller or support team...") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input")
                )

                FloatingActionButton(
                    onClick = {
                        val text = messageInput.trim()
                        if (text.isNotBlank()) {
                            viewModel.sendChatMessage(text)
                            messageInput = ""
                        }
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("chat_send_btn")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send Message", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
