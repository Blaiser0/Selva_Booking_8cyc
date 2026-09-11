package com.company.selvabooking.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.company.selvabooking.domain.model.User
import com.company.selvabooking.domain.model.UserRole
import com.company.selvabooking.ui.components.LoadingIndicator
import com.company.selvabooking.ui.components.SelvaButton
import com.company.selvabooking.ui.components.SelvaOutlinedButton
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTextField
import com.company.selvabooking.ui.components.SelvaTopAppBar
import com.company.selvabooking.ui.theme.CreamSurfaceVariant
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.viewmodel.AdminAdministradoresViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAdministradoresScreen(viewModel: AdminAdministradoresViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var adminToDelete by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(uiState.message, uiState.error) {
        uiState.message?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    uiState.editingAdmin?.let { admin ->
        EditAdministradorDialog(
            admin = admin,
            nombre = uiState.editNombre,
            nombreError = uiState.editNombreError,
            isSaving = uiState.isUpdatingAdmin,
            isSendingReset = uiState.isSendingPasswordReset,
            onNombreChange = viewModel::updateEditNombre,
            onDismiss = viewModel::closeEditAdmin,
            onSave = viewModel::saveAdministradorChanges,
            onSendPasswordReset = viewModel::sendPasswordResetToEditingAdmin
        )
    }

    adminToDelete?.let { admin ->
        AlertDialog(
            onDismissRequest = { adminToDelete = null },
            title = { Text("Eliminar administrador") },
            text = {
                Text(
                    "¿Eliminar la cuenta de ${admin.nombre.ifBlank { admin.email }}? " +
                        "Se eliminará su perfil del sistema."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAdministrador(admin.id)
                    adminToDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { adminToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    SelvaScaffold(
        topBar = { SelvaTopAppBar(title = "Administradores") },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator(Modifier.padding(padding))
        } else if (uiState.accessDenied) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Acceso denegado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Solo SuperAdmin puede gestionar administradores.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Crear administrador",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                        Text(
                            text = "Complete correo y contraseña. El administrador podrá alternar a modo cliente desde su perfil.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SelvaTextField(
                            value = uiState.email,
                            onValueChange = viewModel::updateEmail,
                            label = "Correo electrónico",
                            keyboardType = KeyboardType.Email,
                            error = uiState.emailError
                        )
                        SelvaTextField(
                            value = uiState.password,
                            onValueChange = viewModel::updatePassword,
                            label = "Contraseña",
                            isPassword = true,
                            error = uiState.passwordError
                        )
                        SelvaTextField(
                            value = uiState.confirmPassword,
                            onValueChange = viewModel::updateConfirmPassword,
                            label = "Confirmar contraseña",
                            isPassword = true,
                            error = uiState.confirmPasswordError
                        )
                        SelvaButton(
                            text = if (uiState.isSaving) "Creando..." else "Crear administrador",
                            onClick = viewModel::createAdministrador,
                            enabled = !uiState.isSaving
                        )
                    }
                }

                Text(
                    text = "Administradores registrados (${uiState.administradores.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = ForestGreen
                )

                if (uiState.administradores.isEmpty()) {
                    Text(
                        text = "Aún no hay administradores creados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    uiState.administradores.forEach { admin ->
                        AdministradorCard(
                            admin = admin,
                            isDeleting = uiState.deletingAdminId == admin.id,
                            onEdit = { viewModel.openEditAdmin(admin) },
                            onDelete = { adminToDelete = admin }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditAdministradorDialog(
    admin: User,
    nombre: String,
    nombreError: String?,
    isSaving: Boolean,
    isSendingReset: Boolean,
    onNombreChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onSendPasswordReset: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving && !isSendingReset) onDismiss() },
        title = { Text("Editar administrador") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SelvaTextField(
                    value = admin.email,
                    onValueChange = {},
                    label = "Correo electrónico",
                    enabled = false
                )
                SelvaTextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    label = "Nombre",
                    error = nombreError
                )
                Text(
                    text = "Rol: ${UserRole.ADMINISTRADOR.displayLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SelvaOutlinedButton(
                    text = if (isSendingReset) "Enviando..." else "Enviar restablecimiento de contraseña",
                    onClick = onSendPasswordReset,
                    enabled = !isSaving && !isSendingReset,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = !isSaving && !isSendingReset
            ) {
                Text(if (isSaving) "Guardando..." else "Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving && !isSendingReset
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun AdministradorCard(
    admin: User,
    isDeleting: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = ForestGreen)
                Text(
                    text = admin.nombre.ifBlank { "Sin nombre" },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onEdit, enabled = !isDeleting) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = ForestGreen)
                }
                IconButton(onClick = onDelete, enabled = !isDeleting) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null, tint = ForestGreen)
                Text(
                    text = admin.email,
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = if (isDeleting) "Eliminando cuenta..." else UserRole.ADMINISTRADOR.displayLabel,
                style = MaterialTheme.typography.labelMedium,
                color = if (isDeleting) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    ForestGreen
                }
            )
        }
    }
}
