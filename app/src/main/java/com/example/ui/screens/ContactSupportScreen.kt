package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.LargeActionButton
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen

@Composable
fun ContactSupportScreen(
    viewModel: MainViewModel,
    onNavigate: (Screen) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val currentUser by viewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Seller Contact & Support",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "We are here to help with your daily orders and inquiries.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Seller Info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Vikas Agriculture & Hardware Store",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("+1 (800) 555-FARM", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("seller@shop.com", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("108 Industrial Farm Road, Zone 4, AgriHub")
                }
                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Daily Open: 6:30 AM – 7:30 PM")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Direct Message Form
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Send Message to Seller",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Ask about custom items, daily bulk orders, or delivery times...") },
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("support_message_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                LargeActionButton(
                    text = "Send Message",
                    icon = Icons.Default.Send,
                    onClick = {
                        if (messageText.isBlank()) {
                            viewModel.showMessage("Please type a message before sending.")
                            return@LargeActionButton
                        }
                        viewModel.sendSupportInquiry(messageText) {
                            messageText = ""
                        }
                    },
                    modifier = Modifier.testTag("send_support_msg_btn")
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // FAQ Section
        Text(
            text = "Frequently Asked Questions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        val faqs = listOf(
            "How do daily updates work?" to "The seller updates fresh stock every morning by 7:00 AM. Items marked 'Available Today' are ready for immediate local delivery.",
            "What payment options are accepted?" to "You can pay using Cash on Delivery, Mobile Wallet / UPI, or Card on Delivery.",
            "Can I cancel or change my order?" to "You can contact the seller directly or check your Order Status page before the seller accepts the order."
        )

        faqs.forEach { (q, a) ->
            OutlinedCard(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Q: $q", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(a, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
