package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AppDatabase
import com.example.data.model.TransactionEntity
import com.example.data.repository.FinanceRepository
import com.example.ui.FinanceViewModel
import com.example.ui.components.AddTransactionSheet
import com.example.ui.components.PinLockScreen
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppTab(val title: String, val icon: ImageVector) {
    HOME("Accueil", Icons.Default.Home),
    HISTORY("Historique", Icons.Default.ReceiptLong),
    BUDGET("Budget", Icons.Default.PieChart),
    REPORTS("Rapports", Icons.Default.BarChart),
    SETTINGS("Paramètres", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appDatabase = AppDatabase.getDatabase(applicationContext, kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO))
        val repository = FinanceRepository(appDatabase)

        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FinanceViewModel(repository) as T
            }
        }

        setContent {
            val viewModel: FinanceViewModel = viewModel(factory = viewModelFactory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val darkTheme = when (uiState.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: FinanceViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var currentTab by remember { mutableStateOf(AppTab.HOME) }
    var showAddEditSheet by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle user feedback messages (toasts/snackbars)
    LaunchedEffect(uiState.userFeedbackMessage) {
        uiState.userFeedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedbackMessage()
        }
    }

    // Storage Access Framework launchers for offline backup & export
    val jsonExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    val json = viewModel.exportBackupJson()
                    stream.write(json.toByteArray(Charsets.UTF_8))
                }
                scope.launch { snackbarHostState.showSnackbar("Sauvegarde JSON exportée avec succès !") }
            } catch (e: Exception) {
                scope.launch { snackbarHostState.showSnackbar("Erreur lors de l'exportation : ${e.localizedMessage}") }
            }
        }
    }

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    val csv = viewModel.exportCsv()
                    stream.write(csv.toByteArray(Charsets.UTF_8))
                }
                scope.launch { snackbarHostState.showSnackbar("Fichier CSV exporté avec succès !") }
            } catch (e: Exception) {
                scope.launch { snackbarHostState.showSnackbar("Erreur lors de l'exportation CSV : ${e.localizedMessage}") }
            }
        }
    }

    val jsonImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val json = stream.bufferedReader().use { r -> r.readText() }
                    val ok = viewModel.restoreBackupJson(json)
                    if (!ok) {
                        scope.launch { snackbarHostState.showSnackbar("Fichier de sauvegarde corrompu ou invalide.") }
                    }
                }
            } catch (e: Exception) {
                scope.launch { snackbarHostState.showSnackbar("Erreur lors de l'importation : ${e.localizedMessage}") }
            }
        }
    }

    // Predictive Back navigation: if not on Home, return to Home
    BackHandler(enabled = currentTab != AppTab.HOME) {
        currentTab = AppTab.HOME
    }

    // PIN Protection Gate
    if (uiState.isPinEnabled && uiState.isPinLocked) {
        PinLockScreen(
            onPinEntered = { pin -> viewModel.unlockApp(pin) }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                AppTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            // High visibility Floating Action Button on Home and History tabs
            AnimatedVisibility(
                visible = currentTab == AppTab.HOME || currentTab == AppTab.HISTORY,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        editingTransaction = null
                        showAddEditSheet = true
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                    modifier = Modifier.testTag("quick_add_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter une opération",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.HOME -> {
                    DashboardScreen(
                        uiState = uiState,
                        onAddTransactionClick = {
                            editingTransaction = null
                            showAddEditSheet = true
                        },
                        onTransactionClick = { tx ->
                            editingTransaction = tx
                            showAddEditSheet = true
                        },
                        onNavigateToHistory = { currentTab = AppTab.HISTORY },
                        onNavigateToBudget = { currentTab = AppTab.BUDGET }
                    )
                }
                AppTab.HISTORY -> {
                    HistoryScreen(
                        uiState = uiState,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onPeriodChange = { viewModel.setPeriodFilter(it) },
                        onTypeChange = { viewModel.setTypeFilter(it) },
                        onCategoryChange = { viewModel.setCategoryFilter(it) },
                        onSortChange = { viewModel.setSortOrder(it) },
                        onTransactionClick = { tx ->
                            editingTransaction = tx
                            showAddEditSheet = true
                        }
                    )
                }
                AppTab.BUDGET -> {
                    BudgetScreen(
                        uiState = uiState,
                        onSetGlobalBudget = { viewModel.setGlobalBudget(it) },
                        onSetCategoryBudget = { catId, amt -> viewModel.setCategoryBudget(catId, amt) },
                        onDeleteBudget = { viewModel.deleteBudget(it) }
                    )
                }
                AppTab.REPORTS -> {
                    ReportsScreen(
                        uiState = uiState,
                        onExportCsvClick = {
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                            csvExportLauncher.launch("operations_ny_toebolantsika_$timeStamp.csv")
                        }
                    )
                }
                AppTab.SETTINGS -> {
                    SettingsScreen(
                        uiState = uiState,
                        onSetCurrency = { code, symbol, prefix -> viewModel.setCurrency(code, symbol, prefix) },
                        onSetInitialBalance = { viewModel.setInitialBalance(it) },
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        onSetPin = { viewModel.setPin(it) },
                        onDisablePin = { viewModel.disablePin() },
                        onLoadDemoData = { viewModel.loadDemoData() },
                        onClearAllData = { viewModel.clearAllData() },
                        onExportBackup = {
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                            jsonExportLauncher.launch("sauvegarde_ny_toebolantsika_$timeStamp.json")
                        },
                        onImportBackupClick = {
                            jsonImportLauncher.launch(arrayOf("application/json", "*/*"))
                        },
                        onExportCsv = {
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                            csvExportLauncher.launch("operations_ny_toebolantsika_$timeStamp.csv")
                        },
                        onAddCategory = { name, type, icon, color -> viewModel.addCategory(name, type, icon, color) },
                        onUpdateCategory = { viewModel.updateCategory(it) },
                        onDeleteCategory = { viewModel.deleteCategory(it) }
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for Adding / Modifying an Operation
    if (showAddEditSheet) {
        AddTransactionSheet(
            sheetState = sheetState,
            initialTransaction = editingTransaction,
            categories = uiState.categories,
            currencySymbol = uiState.currencySymbol,
            onDismiss = {
                showAddEditSheet = false
                editingTransaction = null
            },
            onSave = { type, amount, category, dateMillis, description, paymentMethod ->
                if (editingTransaction != null) {
                    viewModel.updateTransaction(
                        id = editingTransaction!!.id,
                        type = type,
                        amount = amount,
                        category = category,
                        dateMillis = dateMillis,
                        description = description,
                        paymentMethod = paymentMethod
                    )
                    scope.launch { snackbarHostState.showSnackbar("Opération modifiée avec succès") }
                } else {
                    viewModel.addTransaction(
                        type = type,
                        amount = amount,
                        category = category,
                        dateMillis = dateMillis,
                        description = description,
                        paymentMethod = paymentMethod
                    )
                    scope.launch { snackbarHostState.showSnackbar("Opération enregistrée") }
                }
                showAddEditSheet = false
                editingTransaction = null
            },
            onDelete = { id ->
                viewModel.deleteTransaction(id)
                showAddEditSheet = false
                editingTransaction = null
                scope.launch { snackbarHostState.showSnackbar("Opération supprimée") }
            }
        )
    }
}
