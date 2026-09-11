package com.company.selvabooking.ui.admin

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.company.selvabooking.domain.model.Reservation
import com.company.selvabooking.domain.model.ReservationStatus
import com.company.selvabooking.ui.components.BookingDateCard
import com.company.selvabooking.ui.components.ErrorMessage
import com.company.selvabooking.ui.components.LoadingIndicator
import com.company.selvabooking.ui.components.ReservationStatusFilterRow
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTextField
import com.company.selvabooking.ui.components.SelvaTopAppBar
import com.company.selvabooking.ui.components.StatusChip
import com.company.selvabooking.ui.theme.CreamSurfaceVariant
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.utils.DateUtils
import com.company.selvabooking.viewmodel.AdminReservationsViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReservationsScreen(viewModel: AdminReservationsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message, uiState.error) {
        uiState.message?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    if (uiState.showForm) {
        ReservationFormDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeForm() }
        )
    }

    if (uiState.selectedReservation != null) {
        ReservationDetailDialog(
            reservation = uiState.selectedReservation!!,
            canManage = uiState.canManageReservations,
            onDismiss = { viewModel.selectReservation(null) },
            onEdit = { viewModel.openEditForm(uiState.selectedReservation!!) },
            onDelete = { viewModel.deleteReservation(uiState.selectedReservation!!.id) },
            onTerminate = { viewModel.terminateReservation(uiState.selectedReservation!!.id) }
        )
    }

    SelvaScaffold(
        topBar = {
            SelvaTopAppBar(
                title = when {
                    uiState.isGerenteMode -> "Historial de reservas"
                    uiState.canManageReservations -> "Gestionar reservas"
                    else -> "Reservas"
                }
            )
        },
        floatingActionButton = {
            if (uiState.canManageReservations) {
                FloatingActionButton(
                    onClick = { viewModel.openCreateForm() },
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva reserva")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (!uiState.isRoleResolved || uiState.isLoading) {
            LoadingIndicator(Modifier.padding(padding))
            return@SelvaScaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                    if (uiState.isGerenteMode) {
                        Text(
                            text = uiState.hotelName.ifBlank { "Su hotel" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReservationCountChip(
                                label = "Total",
                                count = uiState.totalCount,
                                modifier = Modifier.weight(1f)
                            )
                            ReservationCountChip(
                                label = "Confirmadas",
                                count = uiState.confirmadasCount,
                                modifier = Modifier.weight(1f)
                            )
                            ReservationCountChip(
                                label = "Terminadas",
                                count = uiState.terminadasCount,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    SelvaTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::updateSearchQuery,
                        label = if (uiState.isGerenteMode) {
                            "Buscar por cliente, habitación o email"
                        } else {
                            "Buscar por hotel, cliente o email"
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ReservationStatusFilterRow(
                        selectedStatus = uiState.statusFilter,
                        onStatusSelected = viewModel::updateStatusFilter
                    )
                }
            }
            if (uiState.filteredReservations.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (uiState.isGerenteMode) "No hay reservas en su hotel" else "No hay reservas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (uiState.isGerenteMode) {
                                "Las reservas confirmadas y terminadas de su hotel aparecerán aquí. Use los filtros para buscar."
                            } else {
                                "Las reservas confirmadas y terminadas aparecerán aquí. Use los filtros para ver por estado."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(
                    items = uiState.filteredReservations,
                    key = { it.id }
                ) { reservation ->
                    AdminReservationCard(
                        reservation = reservation,
                        showHotelName = !uiState.isGerenteMode,
                        onClick = { viewModel.selectReservation(reservation) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReservationCountChip(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AdminReservationCard(
    reservation: Reservation,
    showHotelName: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (showHotelName) {
                        Text(
                            reservation.hotelNombre,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        reservation.roomNombre,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (showHotelName) FontWeight.Normal else FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusChip(reservation.estado.value)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Cliente: ${reservation.userNombre}")
            Text(
                "${DateUtils.formatDisplay(reservation.fechaIngreso)} - ${DateUtils.formatDisplay(reservation.fechaSalida)}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "${reservation.huespedes} huésped(es) · ${DateUtils.formatCurrency(reservation.precioTotal)}",
                color = ForestGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ReservationFormDialog(
    viewModel: AdminReservationsViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (uiState.editingReservationId != null) "Editar reserva" else "Nueva reserva")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Reserva de la vista de comparación",
                    style = MaterialTheme.typography.labelMedium,
                    color = ForestGreen
                )

                Text("Hotel", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                uiState.hotels.forEach { hotel ->
                    val selected = uiState.selectedHotelId == hotel.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectHotel(hotel.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) CreamSurfaceVariant else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            "${hotel.nombre} · ${hotel.ciudad}",
                            modifier = Modifier.padding(12.dp),
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }

                if (uiState.selectedHotelId.isNotBlank()) {
                    Text("Habitación", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    if (uiState.rooms.isEmpty()) {
                        Text(
                            "Este hotel no tiene habitaciones. Agrégalas desde Gestión de Hoteles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    uiState.rooms.forEach { room ->
                        val selected = uiState.selectedRoomId == room.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectRoom(room.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) CreamSurfaceVariant else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                "${room.nombre} · ${DateUtils.formatCurrency(room.precio)}/noche",
                                modifier = Modifier.padding(12.dp),
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                SelvaTextField(uiState.userNombre, viewModel::updateUserNombre, "Nombre del cliente")
                SelvaTextField(uiState.userEmail, viewModel::updateUserEmail, "Email del cliente")
                SelvaTextField(uiState.userTelefono, viewModel::updateUserTelefono, "Teléfono")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BookingDateCard(
                        label = "Entrada",
                        value = if (uiState.fechaIngreso.isEmpty()) "" else DateUtils.formatDisplay(uiState.fechaIngreso),
                        placeholder = "Seleccionar",
                        onClick = {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val c = Calendar.getInstance()
                                    c.set(y, m, d)
                                    viewModel.setFechaIngreso(c)
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    BookingDateCard(
                        label = "Salida",
                        value = if (uiState.fechaSalida.isEmpty()) "" else DateUtils.formatDisplay(uiState.fechaSalida),
                        placeholder = "Seleccionar",
                        onClick = {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val c = Calendar.getInstance()
                                    c.set(y, m, d)
                                    viewModel.setFechaSalida(c)
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                SelvaTextField(
                    uiState.huespedes,
                    viewModel::updateHuespedes,
                    "Huéspedes",
                    keyboardType = KeyboardType.Number
                )

                Text("Estado", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReservationStatus.publicStatuses.forEach { status ->
                        FilterChip(
                            selected = uiState.estado == status,
                            onClick = { viewModel.updateEstado(status) },
                            label = { Text(status.value) }
                        )
                    }
                }

                SelvaTextField(
                    uiState.precioTotal,
                    viewModel::updatePrecioTotal,
                    "Precio total",
                    keyboardType = KeyboardType.Number
                )

                if (uiState.error != null) ErrorMessage(uiState.error!!)
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.saveReservation() }, enabled = !uiState.isSaving) {
                Text(if (uiState.isSaving) "Guardando..." else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun ReservationDetailDialog(
    reservation: Reservation,
    canManage: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTerminate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalle de reserva") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Hotel: ${reservation.hotelNombre}", fontWeight = FontWeight.Medium)
                Text("Habitación: ${reservation.roomNombre}")
                Text("Cliente: ${reservation.userNombre}")
                Text("Email: ${reservation.userEmail}")
                Text("Teléfono: ${reservation.userTelefono.ifBlank { "—" }}")
                Text(
                    "Fechas: ${DateUtils.formatDisplay(reservation.fechaIngreso)} - " +
                        DateUtils.formatDisplay(reservation.fechaSalida)
                )
                Text("Huéspedes: ${reservation.huespedes}")
                Text("Total: ${DateUtils.formatCurrency(reservation.precioTotal)}")
                Text("Estado: ${reservation.estado.value}")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
        dismissButton = {
            if (canManage) {
                Row {
                    if (reservation.estado == ReservationStatus.CONFIRMADA) {
                        TextButton(onClick = onTerminate) { Text("Terminar") }
                    }
                    TextButton(onClick = onEdit) { Text("Editar") }
                    TextButton(onClick = onDelete) { Text("Eliminar") }
                }
            }
        }
    )
}
