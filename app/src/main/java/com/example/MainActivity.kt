package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ThemeMode
import com.example.ui.screens.AddEditTransactionBottomSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BiometricLockScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RecurringExpensesSheet
import com.example.ui.screens.SettingsSheet
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

enum class AppTab(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    TRANSACTIONS("Transactions", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    ANALYTICS("Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics)
}

class MainActivity : FragmentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isSystemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemDark
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            MyApplicationTheme(darkTheme = isDark) {
                val isAppUnlocked by viewModel.isAppUnlocked.collectAsStateWithLifecycle()
                val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()

                if (isBiometricEnabled && !isAppUnlocked) {
                    BiometricLockScreen(
                        onUnlockSuccess = { viewModel.unlockApp() }
                    )
                } else {
                    NairaTrackerApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun NairaTrackerApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSheetOpen by viewModel.isAddEditSheetOpen.collectAsStateWithLifecycle()
    val editingTx by viewModel.editingTransaction.collectAsStateWithLifecycle()
    val preselectedCat by viewModel.preselectedCategory.collectAsStateWithLifecycle()
    val recentlyDeleted by viewModel.recentlyDeleted.collectAsStateWithLifecycle()
    val recurringExpenses by viewModel.allRecurringExpenses.collectAsStateWithLifecycle()
    val autoRecordedNotification by viewModel.autoRecordedNotification.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(AppTab.HOME) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isRecurringOpen by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show undo snackbar when item is deleted
    LaunchedEffect(recentlyDeleted) {
        if (recentlyDeleted != null) {
            val result = snackbarHostState.showSnackbar(
                message = "Transaction deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearRecentlyDeleted()
            }
        }
    }

    // Show snackbar when recurring expenses are auto-recorded
    LaunchedEffect(autoRecordedNotification) {
        if (autoRecordedNotification != null) {
            snackbarHostState.showSnackbar(
                message = autoRecordedNotification!!,
                duration = SnackbarDuration.Short
            )
            viewModel.clearAutoRecordedNotification()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("naira_tracker_root"),
        topBar = {
            // App Bar with bold green & charcoal identity
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Custom Naira Icon Badge
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldGreenPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "₦",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Naira Tracker",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Personal & Business",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Action Icons: Subscriptions / Recurring & Settings
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.isOverBudget) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEF2F2))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Over Budget",
                                    color = Color(0xFFDC2626),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        // Recurring Subscriptions Shortcut
                        IconButton(
                            onClick = { isRecurringOpen = true },
                            modifier = Modifier.size(36.dp).testTag("open_recurring_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = "Recurring Subscriptions",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Settings Button
                        IconButton(
                            onClick = { isSettingsOpen = true },
                            modifier = Modifier.size(36.dp).testTag("open_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings & Security",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Bottom navigation with 3 tabs: "Home", "Transactions", "Analytics"
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                AppTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldGreenPrimary,
                            selectedTextColor = EmeraldGreenPrimary,
                            indicatorColor = EmeraldGreenPrimary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("tab_${tab.title.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            // Quick FAB to add expense/income from any tab
            FloatingActionButton(
                onClick = { viewModel.openAddSheet() },
                containerColor = EmeraldGreenPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("main_add_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Crossfade(
            targetState = currentTab,
            label = "TabTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { tab ->
            when (tab) {
                AppTab.HOME -> {
                    HomeScreen(
                        uiState = uiState,
                        onAddExpenseClick = { category -> viewModel.openAddSheet(category) },
                        onTransactionClick = { tx -> viewModel.openEditSheet(tx) },
                        onViewAllTransactions = { currentTab = AppTab.TRANSACTIONS }
                    )
                }
                AppTab.TRANSACTIONS -> {
                    TransactionsScreen(
                        uiState = uiState,
                        onFilterSelect = { filter -> viewModel.setFilter(filter) },
                        onSearchChange = { query -> viewModel.setSearchQuery(query) },
                        onTransactionClick = { tx -> viewModel.openEditSheet(tx) },
                        onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) }
                    )
                }
                AppTab.ANALYTICS -> {
                    AnalyticsScreen(
                        uiState = uiState,
                        onUpdateBudget = { newBudget -> viewModel.updateMonthlyBudget(newBudget) }
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for Add/Edit Transaction
    AddEditTransactionBottomSheet(
        isOpen = isSheetOpen,
        editingTransaction = editingTx,
        preselectedCategory = preselectedCat,
        onDismiss = { viewModel.closeAddEditSheet() },
        onSave = { id, type, amount, category, note, dateMillis ->
            viewModel.saveTransaction(id, type, amount, category, note, dateMillis)
        }
    )

    // Modal Bottom Sheet for Recurring Expenses (Subscriptions & Rent)
    RecurringExpensesSheet(
        isOpen = isRecurringOpen,
        recurringExpenses = recurringExpenses,
        onDismiss = { isRecurringOpen = false },
        onSave = { id, title, amount, category, frequency, startDate, note ->
            viewModel.saveRecurringExpense(id, title, amount, category, frequency, startDate, note)
        },
        onDelete = { item -> viewModel.deleteRecurringExpense(item) },
        onToggleActive = { item -> viewModel.toggleRecurringActive(item) },
        onRecordNow = { item -> viewModel.recordRecurringNow(item) }
    )

    // Modal Bottom Sheet for Settings, Dark Mode, CSV Export & Biometrics
    SettingsSheet(
        isOpen = isSettingsOpen,
        themeMode = themeMode,
        isBiometricEnabled = isBiometricEnabled,
        transactions = uiState.transactions,
        onDismiss = { isSettingsOpen = false },
        onThemeModeChange = { mode -> viewModel.setThemeMode(mode) },
        onBiometricToggle = { enabled -> viewModel.setBiometricEnabled(enabled) },
        onOpenRecurring = { isRecurringOpen = true },
        onLockApp = { viewModel.lockApp() }
    )
}
