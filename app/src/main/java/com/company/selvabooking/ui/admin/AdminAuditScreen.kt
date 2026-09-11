package com.company.selvabooking.ui.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.unit.dp
import com.company.selvabooking.domain.model.AuditAction
import com.company.selvabooking.domain.model.AuditEntityType
import com.company.selvabooking.domain.model.AuditLog
import com.company.selvabooking.ui.components.LoadingIndicator
import com.company.selvabooking.ui.components.SelvaOutlinedButton
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTopAppBar
import com.company.selvabooking.ui.theme.CreamSurfaceVariant
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.utils.DateUtils
import com.company.selvabooking.viewmodel.AdminAuditViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminAuditScreen(viewModel: AdminAuditViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var logToRevert by remember { mutableStateOf<AuditLog?>(null) }

    LaunchedEffect(uiState.message, uiState.error) {
        uiState.message?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    logToRevert?.let { log ->
        AlertDialog(
            onDismissRequest = { logToRevert = null },
            title = { Text("Revertir cambio") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "¿Desea revertir la ${log.action.label.lowercase()} de " +
                            "${log.entityType.label.lowercase()} \"${log.entityLabel}\" " +
                            "realizada por ${log.actorName.ifBlank { log.actorEmail }}?"
                    )
                    Text(
                        when (log.action) {
                            AuditAction.CREATE -> "Se eliminará el registro creado."
                            AuditAction.UPDATE -> "Se restaurará el estado anterior."
                            AuditAction.DELETE -> {
                                if (log.entityType == AuditEntityType.HOTEL && log.relatedData.isNotEmpty()) {
                                    "Se restaurará el hotel y ${log.relatedData.size} habitación(es) eliminada(s)."
                                } else {
                                    "Se restaurará el registro eliminado."
                                }
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.revertChange(log.id)
                        logToRevert = null
                    },
                    enabled = uiState.revertingLogId == null
                ) {
                    Text("Revertir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { logToRevert = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    SelvaScaffold(
        topBar = { SelvaTopAppBar(title = "Registro y respaldos") },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator(Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
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
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Copia de seguridad de cambios",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                        Text(
                            text = "Cada acción de un encargado del hotel queda registrada con respaldo " +
                                "del estado anterior. Puede revertir creaciones, modificaciones o eliminaciones.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Filtros",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.encargadoFilter.isBlank(),
                        onClick = { viewModel.setEncargadoFilter("") },
                        label = { Text("Todos") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreen.copy(alpha = 0.15f)
                        )
                    )
                    uiState.encargadoOptions.forEach { name ->
                        FilterChip(
                            selected = uiState.encargadoFilter == name,
                            onClick = { viewModel.setEncargadoFilter(name) },
                            label = { Text(name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreen.copy(alpha = 0.15f)
                            )
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AuditAction.entries.forEach { action ->
                        FilterChip(
                            selected = uiState.actionFilter == action,
                            onClick = {
                                viewModel.setActionFilter(
                                    if (uiState.actionFilter == action) null else action
                                )
                            },
                            label = { Text(action.label) }
                        )
                    }
                    AuditEntityType.entries.forEach { type ->
                        FilterChip(
                            selected = uiState.entityFilter == type,
                            onClick = {
                                viewModel.setEntityFilter(
                                    if (uiState.entityFilter == type) null else type
                                )
                            },
                            label = { Text(type.label) }
                        )
                    }
                    FilterChip(
                        selected = uiState.showOnlyPending,
                        onClick = { viewModel.setShowOnlyPending(!uiState.showOnlyPending) },
                        label = { Text("Solo pendientes") }
                    )
                }

                Text(
                    text = "${uiState.filteredLogs.size} registro(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (uiState.filteredLogs.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant)
                    ) {
                        Text(
                            text = "No hay registros de auditoría con los filtros seleccionados.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    uiState.filteredLogs.forEach { log ->
                        AuditLogCard(
                            log = log,
                            isReverting = uiState.revertingLogId == log.id,
                            onRevert = { logToRevert = log }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditLogCard(
    log: AuditLog,
    isReverting: Boolean,
    onRevert: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (log.reverted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                CreamSurfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (log.entityType) {
                        AuditEntityType.HOTEL -> Icons.Default.Hotel
                        AuditEntityType.ROOM -> Icons.Default.MeetingRoom
                    },
                    contentDescription = null,
                    tint = ForestGreen
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = "${log.action.label}: ${log.entityLabel}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${log.entityType.label} · ${DateUtils.formatTimestamp(log.timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = ForestGreen)
                Text(
                    text = log.actorName.ifBlank { log.actorEmail }.ifBlank { "Encargado desconocido" },
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (log.action == AuditAction.DELETE && log.relatedData.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = ForestGreen)
                    Text(
                        text = "Respaldo: ${log.relatedData.size} habitación(es) incluida(s)",
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (log.reverted) {
                Text(
                    text = "Revertido el ${DateUtils.formatTimestamp(log.revertedAt)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (log.canRevert && !isReverting) {
                SelvaOutlinedButton(
                    text = "Revertir cambio",
                    onClick = onRevert,
                    modifier = Modifier.fillMaxWidth(0.6f)
                )
            } else if (isReverting) {
                Text(
                    text = "Revirtiendo...",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
