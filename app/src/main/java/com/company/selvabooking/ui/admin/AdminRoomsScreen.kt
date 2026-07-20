package com.company.selvabooking.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.company.selvabooking.domain.model.Room
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTextField
import com.company.selvabooking.ui.components.SelvaTopAppBar
import com.company.selvabooking.ui.theme.CreamSurfaceVariant
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.utils.DateUtils
import com.company.selvabooking.viewmodel.AdminRoomsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRoomsScreen(
    viewModel: AdminRoomsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var roomToDelete by remember { mutableStateOf<Room?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message, uiState.error) {
        uiState.message?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    LaunchedEffect(uiState.message) {
        if (uiState.message == "Habitación guardada" && !uiState.isSaving) {
            showForm = false
        }
    }

    if (showForm) {
        RoomFormDialog(
            viewModel = viewModel,
            onDismiss = { showForm = false; viewModel.clearForm() }
        )
    }

    roomToDelete?.let { room ->
        AlertDialog(
            onDismissRequest = { roomToDelete = null },
            title = { Text("Eliminar habitación") },
            text = { Text("¿Eliminar ${room.nombre}?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteRoom(room.id); roomToDelete = null }) {
                    Text("Eliminar")
                }
            },
            dismissButton = { TextButton(onClick = { roomToDelete = null }) { Text("Cancelar") } }
        )
    }

    SelvaScaffold(
        topBar = {
            SelvaTopAppBar(
                title = "Habitaciones - ${uiState.hotelName}",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = ForestGreen
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.clearForm(); showForm = true },
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar habitación")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.rooms) { room ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        if (room.imagenes.isNotEmpty()) {
                            AsyncImage(
                                model = room.imagenes.first(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .padding(0.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = CreamSurfaceVariant
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            room.nombre.take(1),
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                    }
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {
                            Text(room.nombre, fontWeight = FontWeight.Bold)
                            Text(
                                "${DateUtils.formatCurrency(room.precio)}/noche",
                                color = ForestGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Capacidad: ${room.capacidad} · ${if (room.disponible) "Disponible" else "No disponible"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (room.descripcion.isNotBlank()) {
                                Text(
                                    room.descripcion,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = { viewModel.loadRoomForEdit(room); showForm = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                                IconButton(onClick = { roomToDelete = room }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomFormDialog(viewModel: AdminRoomsViewModel, onDismiss: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.uploadImage(it) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (uiState.editingRoomId != null) "Editar habitación" else "Nueva habitación") },
        text = {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Visible en detalle y reserva del hotel",
                    style = MaterialTheme.typography.labelMedium,
                    color = ForestGreen
                )
                SelvaTextField(uiState.nombre, viewModel::updateNombre, "Nombre")
                SelvaTextField(
                    uiState.descripcion,
                    viewModel::updateDescripcion,
                    "Descripción",
                    maxLines = 3,
                    singleLine = false
                )
                SelvaTextField(uiState.precio, viewModel::updatePrecio, "Precio por noche", keyboardType = KeyboardType.Number)
                SelvaTextField(uiState.capacidad, viewModel::updateCapacidad, "Capacidad", keyboardType = KeyboardType.Number)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = uiState.disponible, onCheckedChange = viewModel::updateDisponible)
                    Text("Disponible para reservar")
                }
                TextButton(onClick = { imagePicker.launch("image/*") }) {
                    Text("Agregar imagen (${uiState.imagenes.size})")
                }
                if (uiState.imagenes.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(uiState.imagenes) { index, url ->
                            Box {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { viewModel.removeImage(index) },
                                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Quitar", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.saveRoom() }, enabled = !uiState.isSaving) {
                Text(if (uiState.isSaving) "Guardando..." else "Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
