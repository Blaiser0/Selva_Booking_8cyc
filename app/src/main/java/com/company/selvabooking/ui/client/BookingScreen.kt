package com.company.selvabooking.ui.client

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.company.selvabooking.ui.components.BookingDateCard
import com.company.selvabooking.ui.components.BookingSectionTitle
import com.company.selvabooking.ui.components.ErrorMessage
import com.company.selvabooking.ui.components.LoadingIndicator
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTopAppBar
import com.company.selvabooking.ui.components.StickyPriceBar
import com.company.selvabooking.ui.theme.CreamSurfaceVariant
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.ui.theme.LogoBackground
import com.company.selvabooking.utils.DateUtils
import com.company.selvabooking.viewmodel.BookingViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    viewModel: BookingViewModel,
    onBack: () -> Unit,
    onBookingComplete: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.reservationId) {
        uiState.reservationId?.let { onBookingComplete(it) }
    }

    SelvaScaffold(
        topBar = {
            SelvaTopAppBar(
                title = "Completa tu reserva",
                subtitle = "Confirma fechas y huéspedes",
                showDrawerMenu = false,
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
        bottomBar = {
            StickyPriceBar(
                label = if (uiState.totalNights > 0) {
                    "Total · ${uiState.totalNights} noches"
                } else {
                    "Total estimado"
                },
                price = DateUtils.formatCurrency(uiState.totalPrice),
                buttonText = if (uiState.isSubmitting) "Procesando..." else "Reservar ahora",
                onButtonClick = viewModel::confirmBooking,
                enabled = !uiState.isSubmitting
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator(Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LogoBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val imageUrl = uiState.hotel?.imagenes?.firstOrNull()
                            ?: uiState.room?.imagenes?.firstOrNull()
                        if (imageUrl != null) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = uiState.hotel?.nombre,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CreamSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.hotel?.nombre?.take(2)?.uppercase() ?: "SB",
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = uiState.hotel?.nombre ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = uiState.room?.nombre ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${DateUtils.formatCurrency(uiState.room?.precio ?: 0.0)} / noche",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }
                    }
                }

                BookingSectionTitle(title = "Fechas de estadía")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BookingDateCard(
                        label = "Entrada",
                        value = if (uiState.fechaIngreso.isEmpty()) {
                            ""
                        } else {
                            DateUtils.formatDisplay(uiState.fechaIngreso)
                        },
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
                        value = if (uiState.fechaSalida.isEmpty()) {
                            ""
                        } else {
                            DateUtils.formatDisplay(uiState.fechaSalida)
                        },
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

                if (uiState.fechaError != null) {
                    ErrorMessage(uiState.fechaError!!)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BookingSectionTitle(title = "Huéspedes")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Personas",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Máx. ${uiState.room?.capacidad ?: 1} por habitación",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.setHuespedes(uiState.huespedes - 1) }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Menos", tint = ForestGreen)
                                }
                                Text(
                                    text = "${uiState.huespedes}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(onClick = { viewModel.setHuespedes(uiState.huespedes + 1) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Más", tint = ForestGreen)
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LogoBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BookingSectionTitle(title = "Resumen de precio")
                        PriceRow(label = "Precio por noche", value = DateUtils.formatCurrency(uiState.room?.precio ?: 0.0))
                        PriceRow(label = "Noches", value = "${uiState.totalNights}")
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = ForestGreen.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total a pagar",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = DateUtils.formatCurrency(uiState.totalPrice),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }
                        }
                    }
                }

                if (uiState.error != null) {
                    ErrorMessage(uiState.error!!)
                }
            }
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
