package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.FinanceIncomeGreen
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionSheet(
    sheetState: SheetState,
    initialTransaction: TransactionEntity? = null,
    categories: List<CategoryEntity>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (
        type: TransactionType,
        amount: Long,
        category: CategoryEntity,
        dateMillis: Long,
        description: String,
        paymentMethod: PaymentMethod
    ) -> Unit,
    onDelete: ((Long) -> Unit)? = null
) {
    var type by remember { mutableStateOf(initialTransaction?.type ?: TransactionType.EXPENSE) }
    var amountString by remember { mutableStateOf(initialTransaction?.amount?.toString() ?: "") }
    var description by remember { mutableStateOf(initialTransaction?.description ?: "") }
    var paymentMethod by remember { mutableStateOf(initialTransaction?.paymentMethod ?: PaymentMethod.CASH) }
    var selectedDateMillis by remember { mutableStateOf(initialTransaction?.dateMillis ?: System.currentTimeMillis()) }

    // Filter categories by selected type
    val filteredCategories = categories.filter { it.type == type }
    var selectedCategory by remember(type, filteredCategories) {
        val initialCat = if (initialTransaction != null) {
            categories.find { it.id == initialTransaction.categoryId }
        } else null
        mutableStateOf(initialCat ?: filteredCategories.firstOrNull())
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialTransaction != null) "Modifier l'opération" else "Nouvelle opération",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_sheet_button")) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Type Toggle (Dépense / Revenu)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dépense Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { type = TransactionType.EXPENSE }
                        .testTag("type_expense_button"),
                    color = if (type == TransactionType.EXPENSE) FinanceExpenseRed else Color.Transparent,
                    contentColor = if (type == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dépense", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Revenu Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { type = TransactionType.INCOME }
                        .testTag("type_income_button"),
                    color = if (type == TransactionType.INCOME) FinanceIncomeGreen else Color.Transparent,
                    contentColor = if (type == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Revenu", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input
            OutlinedTextField(
                value = amountString,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }
                    amountString = digits
                },
                label = { Text("Montant ($currencySymbol)") },
                placeholder = { Text("Ex: 25000") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    Text(
                        text = currencySymbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input")
            )

            // Quick amount shortcut buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5000L, 10000L, 25000L, 50000L, 100000L).forEach { quickVal ->
                    OutlinedButton(
                        onClick = {
                            val current = amountString.toLongOrNull() ?: 0L
                            amountString = (current + quickVal).toString()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                    ) {
                        Text("+${quickVal / 1000}k", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Selection
            Text(
                text = "Catégorie",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredCategories.forEach { category ->
                    val isSelected = selectedCategory?.id == category.id
                    val catColor = CategoryIconHelper.parseColor(category.colorHex)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedCategory = category }
                            .testTag("cat_chip_${category.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(catColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = CategoryIconHelper.getIcon(category.iconName),
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Method Selection
            Text(
                text = "Mode de paiement",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PaymentMethod.values().forEach { method ->
                    FilterChip(
                        selected = paymentMethod == method,
                        onClick = { paymentMethod = method },
                        label = { Text(method.labelFr, style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = if (paymentMethod == method) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date & Time Display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = DateUtils.formatDateTime(selectedDateMillis),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Quick shortcuts
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Aujourd'hui",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { selectedDateMillis = System.currentTimeMillis() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Text(
                        text = "Hier",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DAY_OF_YEAR, -1)
                                selectedDateMillis = cal.timeInMillis
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description / Note (Optional)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description / Note (facultatif)") },
                placeholder = { Text("Ex: Marché, plein carburant, restaurant...") },
                singleLine = false,
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("description_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            val parsedAmount = amountString.toLongOrNull() ?: 0L
            val isValid = parsedAmount > 0L && selectedCategory != null

            Button(
                onClick = {
                    selectedCategory?.let { cat ->
                        onSave(type, parsedAmount, cat, selectedDateMillis, description, paymentMethod)
                    }
                },
                enabled = isValid,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == TransactionType.INCOME) FinanceIncomeGreen else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_transaction_button")
            ) {
                Text(
                    text = if (initialTransaction != null) "Mettre à jour l'opération" else "Enregistrer l'opération",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Delete button if modifying existing transaction
            if (initialTransaction != null && onDelete != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FinanceExpenseRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("delete_transaction_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Supprimer cette opération")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Confirmation dialog before definitive deletion
    if (showDeleteConfirm && initialTransaction != null && onDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Confirmer la suppression") },
            text = { Text("Êtes-vous sûr de vouloir supprimer définitivement cette opération de ${CurrencyFormatter.format(initialTransaction.amount, currencySymbol)} ? Cette action est irréversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(initialTransaction.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceExpenseRed)
                ) {
                    Text("Supprimer", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
