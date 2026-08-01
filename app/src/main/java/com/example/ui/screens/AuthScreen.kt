package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LargeActionButton
import com.example.ui.components.VikasLogoBadge
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onNavigate: (Screen) -> Unit
) {
    var authMode by remember { mutableStateOf(0) } // 0: Log In, 1: Sign Up

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Registration specific fields
    val isSignUp = authMode == 1
    val selectedRole = "BUYER"
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Icon
        VikasLogoBadge(size = 64.dp, textSize = 36.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isSignUp) "Create Account" else "Welcome to Vikas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isSignUp) "Sign up to order agricultural tools & supplies" else "Log in to browse farm equipment and manage orders",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mode Switcher (Tab)
        SecondaryTabRow(
            selectedTabIndex = authMode,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = authMode == 0,
                onClick = { authMode = 0 },
                modifier = Modifier.testTag("login_tab")
            ) {
                Text("Log In", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Tab(
                selected = authMode == 1,
                onClick = { authMode = 1 },
                modifier = Modifier.testTag("signup_tab")
            ) {
                Text("Sign Up", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (authMode == 0) {
            // USER LOG IN FORM
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "⚡ Quick Demo Sign In",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.quickLoginBuyer() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_buyer_login_btn")
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Instant Customer Access", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Form Fields
        if (isSignUp) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_name_input")
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_email_input")
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_password_input")
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (isSignUp) {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_phone_input")
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Delivery Address") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_address_input")
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        LargeActionButton(
            text = if (isSignUp) "Create Account" else "Log In",
            icon = if (isSignUp) Icons.Default.PersonAdd else Icons.Default.Login,
            onClick = {
                if (isSignUp) {
                    if (name.isBlank() || email.isBlank() || password.isBlank() || phone.isBlank() || address.isBlank()) {
                        viewModel.showMessage("Please fill in all fields.")
                        return@LargeActionButton
                    }
                    viewModel.registerUser(name, email, password, phone, address, selectedRole) {
                        // Navigated by VM according to role
                    }
                } else {
                    if (email.isBlank() || password.isBlank()) {
                        viewModel.showMessage("Please enter email and password.")
                        return@LargeActionButton
                    }
                    viewModel.login(email, password) {
                        // Navigation handled in VM
                    }
                }
            },
            modifier = Modifier.testTag("auth_submit_btn")
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isSignUp) "Already have an account?" else "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = {
                    authMode = if (isSignUp) 0 else 1
                },
                modifier = Modifier.testTag("toggle_auth_mode_btn")
            ) {
                Text(
                    text = if (isSignUp) "Log In" else "Sign Up",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
