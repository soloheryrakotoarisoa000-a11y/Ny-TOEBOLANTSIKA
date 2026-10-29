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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionType
import com.example.ui.FinanceUiState
import com.example.ui.components.BudgetDialog
import com.example.ui.components.CategoryIconHelper
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.FinanceIncomeGreen
import com.example.ui.theme.FinanceSavingsBlue
import com.example.ui.theme.FinanceWarningOrange
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@Composable
fun BudgetScreen(
    uiState: FinanceUiState,
    onSetGlobalBudget: (Long) -> Unit,
    onSetCategoryBudget: (String, Long) -> Unit,
    onDeleteBudget: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showGlobalBudgetDialog by remember { mutableStateOf(false) }
    var selectedCategoryForBudget by remember { mutableStateOf<CategoryEntity?>(null) }

    val expenseCategories = uiState.categories.filter { it.type == TransactionType.EXPENSE }
    val categoryBudgetsMap = uiState.budgetsForMonth
        .filter { it.categoryId != null }
        .associateBy { it.categoryId!! }

    val categoryExpensesMap = uiState.categoryExpenses.associate { it.category.id to it.amount }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Title
        Text(
            text = "Budgets & Objectifs",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Contrôlez vos dépenses pour ${DateUtils.formatMonthLabel(uiState.currentMonthKey)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Global Budget Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("global_budget_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Budget Global du Mois",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (uiState.globalBudget > 0) "Plafond mensuel défini" else "Aucun plafond défini",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { showGlobalBudgetDialog = true },
                        modifier = Modifier.testTag("edit_global_budget_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.globalBudget > 0) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = "Modifier",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (uiState.globalBudget > 0) {
                    val progress = (uiState.budgetPercent / 100f).coerceIn(0f, 1f)
                    val alertColor = when {
                        uiState.budgetPercent >= 100f -> FinanceExpenseRed
                        uiState.budgetPercent >= 90f -> FinanceExpenseRed
                        uiState.budgetPercent >= 75f -> FinanceWarningOrange
                        else -> MaterialTheme.colorScheme.primary
                    }

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = alertColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Plafond total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                CurrencyFormatter.format(uiState.globalBudget, uiState.currencySymbol, uiState.isCurrencyPrefix),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Consommé", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                CurrencyFormatter.format(uiState.budgetSpent, uiState.currencySymbol, uiState.isCurrencyPrefix),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = alertColor
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Reste", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                CurrencyFormatter.format(uiState.budgetRemaining, uiState.currencySymbol, uiState.isCurrencyPrefix),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.budgetRemaining >= 0) MaterialTheme.colorScheme.primary else FinanceExpenseRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Alert pill
                    val alertText = when {
                        uiState.budgetPercent >= 100f -> "Alerte 100% : Vous avez dépassé votre budget mensuel !"
                        uiState.budgetPercent >= 90f -> "Attention 90% : Vous approchez de votre plafond !"
                        uiState.budgetPercent >= 75f -> "Information 75% : Trois quarts de votre budget sont atteints."
                        else -> "Budget sous contrôle (${String.format("%.0f", uiState.budgetPercent)}% consommé)."
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = alertColor.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when {
                                    uiState.budgetPercent >= 100f -> Icons.Default.Warning
                                    uiState.budgetPercent >= 75f -> Icons.Default.NotificationsActive
                                    else -> Icons.Default.CheckCircle
                                },
                                contentDescription = null,
                                tint = alertColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = alertText,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = alertColor
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showGlobalBudgetDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Définir un budget global")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Category Budgets Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Limites par catégorie",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Categories List with individual budget progress
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            expenseCategories.forEach { category ->
                val budget = categoryBudgetsMap[category.id]
                val spent = categoryExpensesMap[category.id] ?: 0L
                val budgetAmount = budget?.amount ?: 0L
                val percent = if (budgetAmount > 0) (spent.toFloat() / budgetAmount.toFloat()) * 100f else 0f
                val catColor = CategoryIconHelper.parseColor(category.colorHex)

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCategoryForBudget = category }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(catColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = CategoryIconHelper.getIcon(category.iconName),
                                        contentDescription = null,
                                        tint = catColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (budgetAmount > 0) {
                                            "${CurrencyFormatter.format(spent, uiState.currencySymbol, uiState.isCurrencyPrefix)} / ${CurrencyFormatter.format(budgetAmount, uiState.currencySymbol, uiState.isCurrencyPrefix)}"
                                        } else {
                                            "Dépensé : ${CurrencyFormatter.format(spent, uiState.currencySymbol, uiState.isCurrencyPrefix)}"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (budgetAmount > 0) {
                                Text(
                                    text = "${String.format("%.0f", percent)}%",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        percent >= 100f -> FinanceExpenseRed
                                        percent >= 75f -> FinanceWarningOrange
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            } else {
                                Text(
                                    text = "+ Définir",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (budgetAmount > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            val progress = (percent / 100f).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = when {
                                    percent >= 100f -> FinanceExpenseRed
                                    percent >= 75f -> FinanceWarningOrange
                                    else -> catColor
                                },
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Global Budget Dialog
    if (showGlobalBudgetDialog) {
        BudgetDialog(
            initialAmount = uiState.globalBudget,
            category = null,
            currencySymbol = uiState.currencySymbol,
            onDismiss = { showGlobalBudgetDialog = false },
            onSave = { amount ->
                onSetGlobalBudget(amount)
                showGlobalBudgetDialog = false
            }
        )
    }

    // Category Specific Budget Dialog
    selectedCategoryForBudget?.let { cat ->
        val existing = categoryBudgetsMap[cat.id]
        BudgetDialog(
            initialAmount = existing?.amount ?: 0L,
            category = cat,
            currencySymbol = uiState.currencySymbol,
            onDismiss = { selectedCategoryForBudget = null },
            onSave = { amount ->
                onSetCategoryBudget(cat.id, amount)
                selectedCategoryForBudget = null
            },
            onDelete = if (existing != null) {
                {
                    onDeleteBudget(existing.id)
                    selectedCategoryForBudget = null
                }
            } else null
        )
    }
}
