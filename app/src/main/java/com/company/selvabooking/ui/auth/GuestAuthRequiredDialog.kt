package com.company.selvabooking.ui.auth

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun GuestAuthRequiredDialog(
    title: String = "Inicia sesión para continuar",
    message: String,
    onDismiss: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onLogin) {
                Text("Iniciar sesión")
            }
        },
        dismissButton = {
            TextButton(onClick = onRegister) {
                Text("Crear cuenta")
            }
        }
    )
}
