package com.company.selvabooking.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import com.company.selvabooking.domain.model.Hotel
import com.company.selvabooking.ui.components.ErrorMessage
import com.company.selvabooking.ui.components.RatingBadge
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTextField
import com.company.selvabooking.ui.components.SelvaTopAppBar
import com.company.selvabooking.ui.theme.CreamSurfaceVariant
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.ui.theme.ToucanOrange
import com.company.selvabooking.utils.DateUtils
import com.company.selvabooking.viewmodel.AdminHotelsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHotelsScreen(
    viewModel: AdminHotelsViewModel,
    onManageRooms: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var hotelToDelete by remember { mutableStateOf<Hotel?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message, uiState.error) {
        uiState.message?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    LaunchedEffect(uiState.message) {
        if (uiState.message == "Hotel guardado" && !uiState.isSaving && uiState.editingHotelId == null) {
            showForm = false
        }
    }

    if (showForm) {
        HotelFormDialog(
            viewModel = viewModel,
            onDismiss = { showForm = false; viewModel.clearForm() },
            onSaved = { showForm = false }
        )
    }

    hotelToDelete?.let { hotel ->
        AlertDialog(
            onDismissRequest = { hotelToDelete = null },
            title = { Text("Eliminar hotel") },
            text = {
                Text(
                    "¿Eliminar ${hotel.nombre}? También se eliminarán todas sus habitaciones."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHotel(hotel.id)
                    hotelToDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { hotelToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    SelvaScaffold(
        topBar = { SelvaTopAppBar(title = "Gestión de Hoteles") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.clearForm(); showForm = true },
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar hotel")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.hotels.isEmpty() && !uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No hay hoteles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Crea hoteles con imágenes, calificación y ofertas para la vista de comparación.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.hotels) { hotel ->
                    AdminHotelCard(
                        hotel = hotel,
                        onManageRooms = { onManageRooms(hotel.id) },
                        onEdit = {
                            viewModel.loadHotelForEdit(hotel)
                            showForm = true
                        },
                        onDelete = { hotelToDelete = hotel }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminHotelCard(
    hotel: Hotel,
    onManageRooms: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                if (hotel.imagenes.isNotEmpty()) {
                    AsyncImage(
                        model = hotel.imagenes.first(),
                        contentDescription = hotel.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(0.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = CreamSurfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = hotel.nombre.take(2).uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }
                        }
                    }
                }
                if (hotel.oferta) {
                    Surface(
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.TopStart),
                        shape = RoundedCornerShape(8.dp),
                        color = ToucanOrange
                    ) {
                        Text(
                            text = "Oferta",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                if (hotel.destacado) {
                    Surface(
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.TopEnd),
                        shape = RoundedCornerShape(8.dp),
                        color = ForestGreen
                    ) {
                        Text(
                            text = "Destacado",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    hotel.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${hotel.ciudad} · ${hotel.categoria}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RatingBadge(score = hotel.calificacion)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(hotel.estrellas.coerceAtMost(5)) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = ToucanOrange,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Desde ${DateUtils.formatCurrency(hotel.precioMinimo)} / noche",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ForestGreen
                )
                if (hotel.servicios.isNotEmpty()) {
                    Text(
                        hotel.servicios.take(3).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onManageRooms) {
                        Icon(Icons.Default.MeetingRoom, contentDescription = "Habitaciones")
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = onDelete) {
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

@Composable
private fun HotelFormDialog(
    viewModel: AdminHotelsViewModel,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.uploadImage(it) } }

    LaunchedEffect(uiState.message, uiState.isSaving) {
        if (uiState.message == "Hotel guardado" && !uiState.isSaving) {
            onSaved()
            viewModel.clearForm()
            viewModel.clearMessages()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (uiState.editingHotelId != null) "Editar hotel" else "Nuevo hotel") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Campos usados en la vista de comparación de hoteles",
                    style = MaterialTheme.typography.labelMedium,
                    color = ForestGreen
                )
                SelvaTextField(uiState.nombre, viewModel::updateNombre, "Nombre")
                SelvaTextField(uiState.ciudad, viewModel::updateCiudad, "Ciudad")
                SelvaTextField(uiState.direccion, viewModel::updateDireccion, "Dirección")
                SelvaTextField(
                    uiState.descripcion,
                    viewModel::updateDescripcion,
                    "Descripción",
                    maxLines = 3,
                    singleLine = false
                )
                SelvaTextField(uiState.categoria, viewModel::updateCategoria, "Categoría (Ecológico, Lujo...)")
                Text("Estrellas: ${uiState.estrellas}")
                Slider(
                    value = uiState.estrellas.toFloat(),
                    onValueChange = { viewModel.updateEstrellas(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 3
                )
                SelvaTextField(
                    uiState.precioMinimo,
                    viewModel::updatePrecioMinimo,
                    "Precio mínimo (desde)",
                    keyboardType = KeyboardType.Number
                )
                SelvaTextField(
                    uiState.calificacion,
                    viewModel::updateCalificacion,
                    "Calificación (ej. 4.8)",
                    keyboardType = KeyboardType.Decimal
                )
                SelvaTextField(
                    uiState.servicios,
                    viewModel::updateServicios,
                    "Servicios (WiFi, Piscina, Spa...)"
                )
                SelvaTextField(uiState.ubicacion, viewModel::updateUbicacion, "Ubicación (lat,lng)")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = uiState.destacado, onCheckedChange = viewModel::updateDestacado)
                    Text("Destacado en inicio")
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Checkbox(checked = uiState.oferta, onCheckedChange = viewModel::updateOferta)
                    Text("Etiqueta oferta")
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
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Quitar",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.error != null) ErrorMessage(uiState.error!!)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.saveHotel() },
                enabled = !uiState.isSaving
            ) {
                Text(if (uiState.isSaving) "Guardando..." else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
