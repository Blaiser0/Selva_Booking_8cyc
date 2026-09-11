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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.company.selvabooking.domain.model.UserRole
import com.company.selvabooking.ui.components.LoadingIndicator
import com.company.selvabooking.ui.components.SelvaButton
import com.company.selvabooking.ui.components.SelvaOutlinedButton
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTextField
import com.company.selvabooking.ui.components.SelvaTopAppBar
import com.company.selvabooking.ui.theme.CreamSurfaceVariant
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.viewmodel.AdminGerentesViewModel
import com.company.selvabooking.viewmodel.GerenteListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGerentesScreen(viewModel: AdminGerentesViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var gerenteToDelete by remember { mutableStateOf<GerenteListItem?>(null) }

    LaunchedEffect(uiState.message, uiState.error) {
        uiState.message?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    gerenteToDelete?.let { item ->
        val gerente = item.user
        AlertDialog(
            onDismissRequest = { gerenteToDelete = null },
            title = { Text("Eliminar encargado") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "¿Eliminar la cuenta de ${gerente.nombre.ifBlank { gerente.email }}?"
                    )
                    Text(
                        "Se eliminarán de forma permanente:",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("• La cuenta del encargado")
                    Text("• ${item.hotelsCount} hotel(es) registrado(s)")
                    Text("• Todas las habitaciones de esos hoteles")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGerente(gerente.id)
                        gerenteToDelete = null
                    },
                    enabled = uiState.deletingGerenteId == null
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { gerenteToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    SelvaScaffold(
        topBar = { SelvaTopAppBar(title = "Encargados del hotel") },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator(Modifier.padding(padding))
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
                            text = "Crear encargado del hotel",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                        Text(
                            text = "Complete correo y contraseña. El encargado actualizará sus datos al ingresar por primera vez.",
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
                            text = if (uiState.isSaving) "Creando..." else "Crear encargado",
                            onClick = viewModel::createGerente,
                            enabled = !uiState.isSaving
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = ForestGreen.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(4.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Encargados registrados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Text(
                        text = "${uiState.gerentes.size} cuenta(s). Puede eliminar un encargado; " +
                            "sus hoteles y habitaciones se borrarán automáticamente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (uiState.gerentes.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant)
                        ) {
                            Text(
                                text = "Aún no hay encargados del hotel creados.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        uiState.gerentes.forEach { item ->
                            GerenteCard(
                                item = item,
                                isDeleting = uiState.deletingGerenteId == item.user.id,
                                onDelete = { gerenteToDelete = item }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GerenteCard(
    item: GerenteListItem,
    isDeleting: Boolean,
    onDelete: () -> Unit
) {
    val gerente = item.user
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
                Icon(Icons.Default.Person, contentDescription = null, tint = ForestGreen)
                Text(
                    text = gerente.nombre.ifBlank { "Sin nombre" },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null, tint = ForestGreen)
                Text(
                    text = gerente.email,
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Hotel, contentDescription = null, tint = ForestGreen)
                Text(
                    text = "${item.hotelsCount} hotel(es) registrado(s)",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = when {
                    isDeleting -> "Eliminando cuenta y contenido..."
                    gerente.rol == UserRole.CLIENTE -> "Modo cliente activo"
                    gerente.perfilCompleto -> "Perfil completado"
                    else -> "Pendiente de completar perfil"
                },
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    isDeleting -> MaterialTheme.colorScheme.onSurfaceVariant
                    gerente.perfilCompleto || gerente.rol == UserRole.CLIENTE -> ForestGreen
                    else -> MaterialTheme.colorScheme.error
                }
            )
            SelvaOutlinedButton(
                text = if (isDeleting) "Eliminando..." else "Eliminar cuenta",
                onClick = { if (!isDeleting) onDelete() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
