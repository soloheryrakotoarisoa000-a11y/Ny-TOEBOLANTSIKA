package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.FinanceUiState
import com.example.ui.PeriodFilter
import com.example.ui.SortOrder
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.FinanceIncomeGreen
import com.example.util.CurrencyFormatter

@Composable
fun HistoryScreen(
    uiState: FinanceUiState,
    onSearchChange: (String) -> Unit,
    onPeriodChange: (PeriodFilter) -> Unit,
    onTypeChange: (TransactionType?) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onSortChange: (SortOrder) -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    // Summary calculation for the filtered results
    val periodIncome = uiState.filteredTransactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }
    val periodExpense = uiState.filteredTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }
    val periodNet = periodIncome - periodExpense

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Title & Sort
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Historique",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Box {
                IconButton(
                    onClick = { sortMenuExpanded = true },
                    modifier = Modifier.testTag("sort_menu_button")
                ) {
                    Icon(Icons.Default.Sort, contentDescription = "Trier", tint = MaterialTheme.colorScheme.primary)
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    SortOrder.values().forEach { sort ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = sort.labelFr,
                                    fontWeight = if (uiState.sortOrder == sort) FontWeight.Bold else FontWeight.Normal,
                                    color = if (uiState.sortOrder == sort) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onSortChange(sort)
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Rechercher une opération...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Effacer")
                    }
                }
            } else null,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Period Filter Chips
        val periodScroll = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(periodScroll),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PeriodFilter.values().forEach { period ->
                FilterChip(
                    selected = uiState.periodFilter == period,
                    onClick = { onPeriodChange(period) },
                    label = { Text(period.labelFr, style = MaterialTheme.typography.bodySmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Type Filter Chips (Tous / Dépenses / Revenus)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.typeFilter == null,
                onClick = { onTypeChange(null) },
                label = { Text("Tous", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = uiState.typeFilter == TransactionType.EXPENSE,
                onClick = { onTypeChange(TransactionType.EXPENSE) },
                label = { Text("Dépenses", style = MaterialTheme.typography.bodySmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = FinanceExpenseRed.copy(alpha = 0.15f),
                    selectedLabelColor = FinanceExpenseRed
                ),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = uiState.typeFilter == TransactionType.INCOME,
                onClick = { onTypeChange(TransactionType.INCOME) },
                label = { Text("Revenus", style = MaterialTheme.typography.bodySmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = FinanceIncomeGreen.copy(alpha = 0.15f),
                    selectedLabelColor = FinanceIncomeGreen
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Period Summary Card (Totaux de la période filtrée)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Revenus", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "+${CurrencyFormatter.format(periodIncome, uiState.currencySymbol, uiState.isCurrencyPrefix)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = FinanceIncomeGreen
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Dépenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "-${CurrencyFormatter.format(periodExpense, uiState.currencySymbol, uiState.isCurrencyPrefix)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = FinanceExpenseRed
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Différence", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${if (periodNet >= 0) "+" else ""}${CurrencyFormatter.format(periodNet, uiState.currencySymbol, uiState.isCurrencyPrefix)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (periodNet >= 0) MaterialTheme.colorScheme.primary else FinanceExpenseRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Transactions List
        if (uiState.filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Aucune opération trouvée",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Modifiez vos filtres ou effectuez une nouvelle recherche.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.filteredTransactions, key = { it.id }) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        currencySymbol = uiState.currencySymbol,
                        isPrefix = uiState.isCurrencyPrefix,
                        onClick = { onTransactionClick(tx) }
                    )
                }
            }
        }
    }
}
