package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppBottomNavigation
import com.example.ui.components.AppTopBar
import com.example.ui.components.NotificationDialog
import com.example.ui.components.SmartCenteredLoadingAnimation
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppHost()
            }
        }
    }
}

@Composable
fun MainAppHost(viewModel: MainViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isScreenLoading by viewModel.isScreenLoading.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isSeller by viewModel.isSeller.collectAsState()

    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val hasSelectedLanguage by viewModel.hasSelectedLanguage.collectAsState()

    val cartItemCount by viewModel.cartItemCount.collectAsState()
    val unreadNotificationsCount by viewModel.unreadNotificationCount.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showInitialAuthDialog by remember { mutableStateOf(currentUser == null) }
    var showLanguageSelectionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            showInitialAuthDialog = false
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearSnackbar()
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                currentScreen = currentScreen,
                userRole = currentUser?.role,
                userName = currentUser?.name,
                unreadNotificationsCount = unreadNotificationsCount,
                cartItemCount = cartItemCount,
                onNavigate = { viewModel.navigateTo(it) },
                onOpenNotifications = { showNotificationsDialog = true },
                onLogout = { viewModel.logout() },
                onQuickLoginSeller = { viewModel.quickLoginSeller() },
                onLoginSeller = { email, pass -> viewModel.login(email, pass) {} },
                onOpenLanguageSelector = { showLanguageSelectionDialog = true }
            )
        },
        bottomBar = {
            AppBottomNavigation(
                currentScreen = currentScreen,
                userRole = currentUser?.role,
                cartCount = cartItemCount,
                onNavigate = { viewModel.navigateTo(it) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isScreenLoading) {
                SmartCenteredLoadingAnimation(
                    title = "Loading",
                    subtitle = "Opening page, please wait..."
                )
            } else {
                when (val screen = currentScreen) {
                is Screen.Home -> HomeScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )

                is Screen.Catalog -> ProductCatalogScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )

                is Screen.DirectCallContact -> DirectContactScreen(
                    viewModel = viewModel,
                    onNavigateToCatalog = { viewModel.navigateTo(Screen.Catalog) },
                    onNavigateToOrders = { viewModel.navigateTo(Screen.OrderHistory) }
                )

                is Screen.ProductDetail -> ProductDetailScreen(
                    productId = screen.productId,
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )

                is Screen.Cart -> CartScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )

                is Screen.Checkout -> CheckoutScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )

                is Screen.OrderStatus -> OrderStatusScreen(
                    orderId = screen.orderId,
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )

                is Screen.OrderHistory -> OrderHistoryScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )

                is Screen.SellerDashboard -> SellerDashboardScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )

                is Screen.SellerContactManagement -> SellerContactManagementScreen(
                    viewModel = viewModel,
                    onNavigateToDashboard = { viewModel.navigateTo(Screen.SellerDashboard) }
                )

                is Screen.Auth -> AuthScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )

                is Screen.ContactSupport -> ContactSupportScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )

                is Screen.Profile -> ProfileScreen(
                    viewModel = viewModel,
                    onNavigateBack = { viewModel.navigateTo(Screen.Home) }
                )

                is Screen.ChatSupport -> ChatSupportScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )

                is Screen.AdminDashboard -> AdminDashboardScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        }
    }

        if (showNotificationsDialog) {
            NotificationDialog(
                notifications = notifications,
                onDismiss = { showNotificationsDialog = false },
                onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
                onSelectOrder = { orderId ->
                    viewModel.navigateTo(Screen.OrderStatus(orderId))
                }
            )
        }

        if (showInitialAuthDialog && currentUser == null) {
            com.example.ui.components.InitialAuthDialog(
                viewModel = viewModel,
                onDismiss = {
                    showInitialAuthDialog = false
                },
                onSuccess = {
                    showInitialAuthDialog = false
                }
            )
        }

        if (showLanguageSelectionDialog) {
            com.example.ui.components.LanguageSelectionDialog(
                currentLanguage = currentLanguage,
                onLanguageSelected = { viewModel.setLanguage(it) },
                onContinue = {
                    viewModel.markLanguageSelected()
                    showLanguageSelectionDialog = false
                }
            )
        }
    }
}
