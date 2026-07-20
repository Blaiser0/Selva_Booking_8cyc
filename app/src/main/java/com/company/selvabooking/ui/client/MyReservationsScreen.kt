package com.company.selvabooking.ui.client

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.company.selvabooking.domain.model.Reservation
import com.company.selvabooking.ui.components.LoadingIndicator
import com.company.selvabooking.ui.components.ReservationStatusFilterRow
import com.company.selvabooking.ui.components.SelvaOutlinedButton
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTopAppBar
import com.company.selvabooking.ui.components.StatusChip
import com.company.selvabooking.utils.DateUtils
import com.company.selvabooking.viewmodel.MyReservationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationsScreen(viewModel: MyReservationsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.cancelMessage, uiState.error) {
        uiState.cancelMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    SelvaScaffold(
        topBar = { SelvaTopAppBar(title = "Mis Reservas") },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator(Modifier.padding(padding))
        } else if (uiState.reservations.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No tienes reservas aún",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Explora hoteles y realiza tu primera reserva",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                ReservationStatusFilterRow(
                    selectedStatus = uiState.statusFilter,
                    onStatusSelected = viewModel::updateStatusFilter,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                if (uiState.filteredReservations.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No hay reservas con este estado",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.filteredReservations) { reservation ->
                            ReservationCard(
                                reservation = reservation,
                                canCancel = viewModel.canCancel(reservation),
                                onCancel = { viewModel.cancelReservation(reservation.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationCard(
    reservation: Reservation,
    canCancel: Boolean,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reservation.hotelNombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusChip(status = reservation.estado.value)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Habitación: ${reservation.roomNombre}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${DateUtils.formatDisplay(reservation.fechaIngreso)} - ${DateUtils.formatDisplay(reservation.fechaSalida)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = DateUtils.formatCurrency(reservation.precioTotal),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (canCancel) {
                Spacer(modifier = Modifier.height(12.dp))
                SelvaOutlinedButton(
                    text = "Cancelar reserva",
                    onClick = onCancel
                )
            }
        }
    }
}
