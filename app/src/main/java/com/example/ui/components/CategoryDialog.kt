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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.FinanceExpenseRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDialog(
    initialCategory: CategoryEntity? = null,
    defaultType: TransactionType = TransactionType.EXPENSE,
    onDismiss: () -> Unit,
    onSave: (name: String, type: TransactionType, iconName: String, colorHex: String) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialCategory?.name ?: "") }
    var type by remember { mutableStateOf(initialCategory?.type ?: defaultType) }
    var selectedIcon by remember { mutableStateOf(initialCategory?.iconName ?: "category") }
    var selectedColor by remember { mutableStateOf(initialCategory?.colorHex ?: "#0B6646") }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialCategory != null) "Modifier la catégorie" else "Nouvelle catégorie",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Type selection (if new category)
                if (initialCategory == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = type == TransactionType.EXPENSE,
                            onClick = { type = TransactionType.EXPENSE },
                            label = { Text("Dépense") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = type == TransactionType.INCOME,
                            onClick = { type = TransactionType.INCOME },
                            label = { Text("Revenu") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de la catégorie") },
                    placeholder = { Text("Ex: Épargne projet, Carburant...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("category_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Icon selection
                Text("Icône", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CategoryIconHelper.AVAILABLE_ICONS.forEach { (iconKey, _) ->
                        val isSel = selectedIcon == iconKey
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSel) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isSel) 2.dp else 0.dp,
                                    color = if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedIcon = iconKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = CategoryIconHelper.getIcon(iconKey),
                                contentDescription = null,
                                tint = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Color selection
                Text("Couleur", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CategoryIconHelper.AVAILABLE_COLORS.forEach { colorHex ->
                        val color = CategoryIconHelper.parseColor(colorHex)
                        val isSel = selectedColor.equals(colorHex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSel) 3.dp else 1.dp,
                                    color = if (isSel) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }

                if (initialCategory != null && !initialCategory.isDefault && onDelete != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FinanceExpenseRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Supprimer la catégorie")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, type, selectedIcon, selectedColor) },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )

    if (showDeleteConfirm && initialCategory != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer la catégorie ?") },
            text = { Text("Les opérations associées conserveront leur libellé, mais la catégorie sera retirée de vos filtres.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(initialCategory.id)
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
