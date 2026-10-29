package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryEntity

@Composable
fun BudgetDialog(
    initialAmount: Long,
    category: CategoryEntity? = null,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var amountText by remember { mutableStateOf(if (initialAmount > 0) initialAmount.toString() else "") }

    val isGlobal = category == null
    val title = if (isGlobal) "Budget mensuel global" else "Budget pour : ${category?.name}"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Définissez le plafond de dépenses pour ce mois. Des alertes visuelles s'afficheront dès 75%, 90% et 100% de consommation.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input -> amountText = input.filter { it.isDigit() } },
                    label = { Text("Plafond en $currencySymbol") },
                    placeholder = { Text("Ex: 1000000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("budget_amount_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    onSave(amount)
                },
                enabled = (amountText.toLongOrNull() ?: 0L) > 0L,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_budget_button")
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
}
