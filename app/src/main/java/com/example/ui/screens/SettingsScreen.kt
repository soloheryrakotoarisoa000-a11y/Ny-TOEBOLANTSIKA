package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionType
import com.example.ui.FinanceUiState
import com.example.ui.components.CategoryDialog
import com.example.ui.components.CategoryIconHelper
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.FinanceIncomeGreen
import com.example.ui.theme.FinanceWarningOrange
import com.example.util.CurrencyFormatter

@Composable
fun SettingsScreen(
    uiState: FinanceUiState,
    onSetCurrency: (String, String, Boolean) -> Unit,
    onSetInitialBalance: (Long) -> Unit,
    onSetThemeMode: (String) -> Unit,
    onSetPin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onLoadDemoData: () -> Unit,
    onClearAllData: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackupClick: () -> Unit,
    onExportCsv: () -> Unit,
    onAddCategory: (String, TransactionType, String, String) -> Unit,
    onUpdateCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showInitialBalanceDialog by remember { mutableStateOf(false) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showCategoryManagerDialog by remember { mutableStateOf(false) }
    var showDemoConfirmDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Paramètres",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Configuration, sécurité et sauvegardes locales",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: Localisation & Monnaie
        SettingsSectionHeader("Devise & Finances")
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsClickableItem(
                    icon = Icons.Default.MonetizationOn,
                    title = "Devise principale",
                    subtitle = "${uiState.currencyCode} (${uiState.currencySymbol})",
                    onClick = { showCurrencyDialog = true },
                    tag = "setting_currency_item"
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                SettingsClickableItem(
                    icon = Icons.Default.AccountBalance,
                    title = "Solde initial de départ",
                    subtitle = CurrencyFormatter.format(uiState.initialBalance, uiState.currencySymbol, uiState.isCurrencyPrefix),
                    onClick = { showInitialBalanceDialog = true },
                    tag = "setting_initial_balance_item"
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                SettingsClickableItem(
                    icon = Icons.Default.Category,
                    title = "Gestion des catégories",
                    subtitle = "${uiState.categories.size} catégories disponibles",
                    onClick = { showCategoryManagerDialog = true },
                    tag = "setting_categories_item"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Apparence
        SettingsSectionHeader("Apparence")
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsClickableItem(
                    icon = Icons.Default.DarkMode,
                    title = "Thème visuel",
                    subtitle = when (uiState.themeMode) {
                        "DARK" -> "Sombre"
                        "LIGHT" -> "Clair"
                        else -> "Système"
                    },
                    onClick = {
                        val nextMode = when (uiState.themeMode) {
                            "SYSTEM" -> "LIGHT"
                            "LIGHT" -> "DARK"
                            else -> "SYSTEM"
                        }
                        onSetThemeMode(nextMode)
                    },
                    tag = "setting_theme_item"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Sécurité & Confidentialité
        SettingsSectionHeader("Sécurité & Confidentialité")
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Verrouillage par code PIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (uiState.isPinEnabled) "Actif (sécurisé par hachage SHA-256 local)" else "Désactivé",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = uiState.isPinEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                showPinSetupDialog = true
                            } else {
                                onDisablePin()
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("pin_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Confidentialité absolue : toutes vos données sont stockées uniquement sur votre appareil. Aucun serveur, aucun cookie et aucun pistage.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Sauvegarde & Restauration
        SettingsSectionHeader("Sauvegarde & Exportation")
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsClickableItem(
                    icon = Icons.Default.Backup,
                    title = "Exporter la sauvegarde JSON",
                    subtitle = if (uiState.lastBackupDate != null) "Dernière sauvegarde : ${uiState.lastBackupDate}" else "Sauvegardez toutes vos données hors-ligne",
                    onClick = onExportBackup,
                    tag = "setting_export_backup"
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                SettingsClickableItem(
                    icon = Icons.Default.FileUpload,
                    title = "Restaurer une sauvegarde JSON",
                    subtitle = "Restaure vos opérations et catégories depuis un fichier",
                    onClick = onImportBackupClick,
                    tag = "setting_import_backup"
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                SettingsClickableItem(
                    icon = Icons.Default.FileDownload,
                    title = "Exporter en fichier CSV",
                    subtitle = "Tableau Excel / Feuilles de calcul universel",
                    onClick = onExportCsv,
                    tag = "setting_export_csv"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Mode Démonstration & Données
        SettingsSectionHeader("Gestion des Données")
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsClickableItem(
                    icon = Icons.Default.PlayArrow,
                    title = "Mode démonstration",
                    subtitle = "Charger des données d'exemple réalistes (budget malgache)",
                    onClick = { showDemoConfirmDialog = true },
                    tag = "setting_demo_data"
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                SettingsClickableItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Réinitialiser toutes les données",
                    subtitle = "Effacer les opérations et revenir à l'état initial",
                    iconTint = FinanceExpenseRed,
                    onClick = { showClearConfirmDialog = true },
                    tag = "setting_clear_data"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App Footer Info
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "NY TOEBOLANTSIKA • Version 1.0",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Suivi budgétaire 100% hors ligne et autonome",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Dialog: Currency Selection
    if (showCurrencyDialog) {
        val currencies = listOf(
            Triple("MGA", "Ar", false),
            Triple("EUR", "€", false),
            Triple("USD", "$", true),
            Triple("GBP", "£", true),
            Triple("CHF", "CHF", false),
            Triple("CAD", "$", true)
        )
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Sélectionner la devise") },
            text = {
                Column {
                    currencies.forEach { (code, symbol, isPrefix) ->
                        val isSelected = uiState.currencyCode == code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onSetCurrency(code, symbol, isPrefix)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$code ($symbol)",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Text("Sélectionné", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showCurrencyDialog = false }) { Text("Fermer") }
            }
        )
    }

    // Dialog: Initial Balance Setup
    if (showInitialBalanceDialog) {
        var balanceText by remember { mutableStateOf(uiState.initialBalance.toString()) }
        AlertDialog(
            onDismissRequest = { showInitialBalanceDialog = false },
            title = { Text("Solde initial de départ") },
            text = {
                Column {
                    Text(
                        "Si vous possédez déjà une somme au moment de commencer à utiliser l'application, saisissez-la ici.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = { balanceText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Montant (${uiState.currencySymbol})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = balanceText.toLongOrNull() ?: 0L
                        onSetInitialBalance(amount)
                        showInitialBalanceDialog = false
                    }
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showInitialBalanceDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Dialog: PIN Setup
    if (showPinSetupDialog) {
        var pinInput by remember { mutableStateOf("") }
        var confirmPinInput by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPinSetupDialog = false },
            title = { Text("Créer un code PIN") },
            text = {
                Column {
                    Text(
                        "Choisissez un code PIN à 4 chiffres pour protéger l'accès à vos finances hors ligne.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = { Text("Code PIN (4 chiffres)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmPinInput = it },
                        label = { Text("Confirmez le code PIN") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(pinError ?: "", color = FinanceExpenseRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length != 4) {
                            pinError = "Le code PIN doit comporter 4 chiffres"
                        } else if (pinInput != confirmPinInput) {
                            pinError = "Les codes ne correspondent pas"
                        } else {
                            onSetPin(pinInput)
                            showPinSetupDialog = false
                        }
                    }
                ) {
                    Text("Activer")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPinSetupDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Dialog: Category Manager
    if (showCategoryManagerDialog) {
        CategoryManagerSheet(
            categories = uiState.categories,
            onDismiss = { showCategoryManagerDialog = false },
            onAddCategory = onAddCategory,
            onUpdateCategory = onUpdateCategory,
            onDeleteCategory = onDeleteCategory
        )
    }

    // Dialog: Confirm Load Demo Data
    if (showDemoConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDemoConfirmDialog = false },
            title = { Text("Charger les données d'exemple ?") },
            text = { Text("Cette action va précharger des opérations réalistes (salaire, courses, carburant, factures Jirama) et des budgets d'exemple.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDemoConfirmDialog = false
                        onLoadDemoData()
                    }
                ) {
                    Text("Charger la démo")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDemoConfirmDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Dialog: Confirm Clear All Data
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Réinitialiser toutes les données ?") },
            text = { Text("Attention : cette action supprimera définitivement toutes vos opérations enregistrées. Pensez à exporter une sauvegarde au préalable.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearAllData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceExpenseRed)
                ) {
                    Text("Effacer tout", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirmDialog = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tag: String,
    iconTint: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val tint = iconTint ?: MaterialTheme.colorScheme.primary
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun CategoryManagerSheet(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onAddCategory: (String, TransactionType, String, String) -> Unit,
    onUpdateCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (String) -> Unit
) {
    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Gestion des Catégories", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showNewCategoryDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = MaterialTheme.colorScheme.primary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Touchez une catégorie pour modifier son nom, son icône ou sa couleur :",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                categories.forEach { cat ->
                    val color = CategoryIconHelper.parseColor(cat.colorHex)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { categoryToEdit = cat }
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                CategoryIconHelper.getIcon(cat.iconName),
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(cat.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (cat.type == TransactionType.EXPENSE) "Dépense" else "Revenu",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (cat.isDefault) {
                            Text("Défaut", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Terminé") }
        }
    )

    // Edit Existing Category Dialog
    categoryToEdit?.let { cat ->
        CategoryDialog(
            initialCategory = cat,
            onDismiss = { categoryToEdit = null },
            onSave = { name, type, icon, colorHex ->
                onUpdateCategory(cat.copy(name = name, type = type, iconName = icon, colorHex = colorHex))
                categoryToEdit = null
            },
            onDelete = if (!cat.isDefault) {
                { catId ->
                    onDeleteCategory(catId)
                    categoryToEdit = null
                }
            } else null
        )
    }

    // Create New Category Dialog
    if (showNewCategoryDialog) {
        CategoryDialog(
            initialCategory = null,
            onDismiss = { showNewCategoryDialog = false },
            onSave = { name, type, icon, colorHex ->
                onAddCategory(name, type, icon, colorHex)
                showNewCategoryDialog = false
            }
        )
    }
}
