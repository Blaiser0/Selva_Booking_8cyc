package com.company.selvabooking.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.company.selvabooking.utils.CardExpiryUtils
import java.util.Calendar

@Composable
fun CardExpiryPickerDialog(
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH) + 1
    val parsed = remember(initialValue) { CardExpiryUtils.parse(initialValue) }
    val defaultYear = currentYear.coerceIn(CardExpiryUtils.MIN_YEAR, CardExpiryUtils.maxSelectableYear())

    var selectedYear by remember(initialValue) {
        mutableIntStateOf(parsed?.second?.coerceIn(CardExpiryUtils.MIN_YEAR, CardExpiryUtils.maxSelectableYear()) ?: defaultYear)
    }
    var selectedMonth by remember(initialValue) {
        mutableIntStateOf(parsed?.first?.coerceIn(1, 12) ?: currentMonth)
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val yearOptions = remember { CardExpiryUtils.availableYears().map { it.toString() } }
    val monthOptions = remember { CardExpiryUtils.allMonths().map(CardExpiryUtils::monthLabel) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vencimiento de tarjeta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Seleccione mes y año de vencimiento",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SelvaDropdownField(
                        value = CardExpiryUtils.monthLabel(selectedMonth),
                        onValueChange = { label ->
                            selectedMonth = CardExpiryUtils.monthFromLabel(label)
                            errorMessage = null
                        },
                        label = "Mes",
                        options = monthOptions,
                        placeholder = "Mes",
                        modifier = Modifier.weight(1f)
                    )
                    SelvaDropdownField(
                        value = selectedYear.toString(),
                        onValueChange = { year ->
                            selectedYear = year.toIntOrNull()?.coerceIn(
                                CardExpiryUtils.MIN_YEAR,
                                CardExpiryUtils.maxSelectableYear()
                            ) ?: selectedYear
                            errorMessage = null
                        },
                        label = "Año",
                        options = yearOptions,
                        placeholder = "Año",
                        modifier = Modifier.weight(1f)
                    )
                }
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val validationError = CardExpiryUtils.validationError(selectedMonth, selectedYear)
                    if (validationError == null) {
                        onConfirm(CardExpiryUtils.format(selectedMonth, selectedYear))
                        onDismiss()
                    } else {
                        errorMessage = validationError
                    }
                }
            ) {
                Text("Aceptar", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
