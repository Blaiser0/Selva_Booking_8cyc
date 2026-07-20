package com.company.selvabooking.ui.client

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.company.selvabooking.domain.model.SavedPaymentCard
import com.company.selvabooking.ui.components.ErrorMessage
import com.company.selvabooking.ui.components.LoadingIndicator
import com.company.selvabooking.ui.components.PaymentCardCvcField
import com.company.selvabooking.ui.components.PaymentMethodForm
import com.company.selvabooking.ui.components.SavedPaymentCardSummary
import com.company.selvabooking.ui.components.SelvaButton
import com.company.selvabooking.ui.components.SelvaOutlinedButton
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTopAppBar
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.utils.DateUtils
import com.company.selvabooking.viewmodel.PaymentUiState
import com.company.selvabooking.viewmodel.PaymentViewModel
import com.company.selvabooking.viewmodel.toPaymentMethodFormState

@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel,
    onBack: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val reservation = uiState.reservation

    LaunchedEffect(uiState.paymentFlowComplete) {
        if (uiState.paymentFlowComplete) {
            kotlinx.coroutines.delay(800)
            onPaymentSuccess()
        }
    }

    SelvaScaffold(
        topBar = {
            SelvaTopAppBar(
                title = "Método de pago",
                showDrawerMenu = false,
                navigationIcon = {
                    if (!uiState.isSuccess) {
                        IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = ForestGreen
                        )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading || reservation == null -> {
                LoadingIndicator(Modifier.padding(padding))
            }

            uiState.isSuccess -> {
                PaymentSuccessContent(
                    modifier = Modifier.padding(padding),
                    showSaveCardPrompt = uiState.showSaveCardPrompt,
                    onSaveCard = viewModel::saveCardForFuture,
                    onDeclineSaveCard = viewModel::declineSaveCard
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    PaymentSummaryCard(reservation.precioTotal, reservation)
                    Spacer(modifier = Modifier.height(20.dp))
                    val savedCard = uiState.savedCard
                    if (savedCard != null) {
                        SavedCardSelector(
                            savedCard = savedCard,
                            useSavedCard = uiState.useSavedCard,
                            onUseSavedCard = viewModel::useSavedCard,
                            onUseNewCard = viewModel::switchToNewCard
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    PaymentGatewayForm(uiState, viewModel)
                    if (uiState.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ErrorMessage(uiState.error!!)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    SelvaButton(
                        text = if (uiState.isProcessing) "Procesando pago..." else "Confirmar pago",
                        onClick = viewModel::confirmPayment,
                        enabled = !uiState.isProcessing
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun PaymentSuccessContent(
    modifier: Modifier = Modifier,
    showSaveCardPrompt: Boolean,
    onSaveCard: () -> Unit,
    onDeclineSaveCard: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "¡Pago confirmado!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Tu reserva ha sido confirmada exitosamente",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        if (showSaveCardPrompt) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "¿Desea guardar la tarjeta para futuras reservas?",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Podrá usarla en sus próximas reservas o ingresar otra tarjeta alternativa.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            SelvaButton(
                text = "Sí, guardar tarjeta",
                onClick = onSaveCard
            )
            Spacer(modifier = Modifier.height(12.dp))
            SelvaOutlinedButton(
                text = "No, gracias",
                onClick = onDeclineSaveCard
            )
        }
    }
}

@Composable
private fun SavedCardSelector(
    savedCard: SavedPaymentCard,
    useSavedCard: Boolean,
    onUseSavedCard: () -> Unit,
    onUseNewCard: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Tarjetas disponibles",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onUseSavedCard),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (useSavedCard) {
                    ForestGreen.copy(alpha = 0.08f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = useSavedCard,
                    onClick = onUseSavedCard
                )
                Icon(
                    Icons.Filled.CreditCard,
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(22.dp)
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = savedCard.maskedNumber,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${savedCard.cardholderName} · Vence ${savedCard.expiry}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onUseNewCard),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (!useSavedCard) {
                    ForestGreen.copy(alpha = 0.08f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = !useSavedCard,
                    onClick = onUseNewCard
                )
                Text(
                    text = "Usar otra tarjeta",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (useSavedCard) {
            Text(
                text = "Seguir usando ${savedCard.maskedNumber}",
                style = MaterialTheme.typography.labelMedium,
                color = ForestGreen,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable(onClick = onUseSavedCard)
            )
        }
    }
}

@Composable
private fun PaymentSummaryCard(
    total: Double,
    reservation: com.company.selvabooking.domain.model.Reservation
) {
    Text(
        text = "Resumen de pago",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(12.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryRow("Hotel", reservation.hotelNombre)
            SummaryRow("Habitación", reservation.roomNombre)
            SummaryRow("Ingreso", DateUtils.formatDisplay(reservation.fechaIngreso))
            SummaryRow("Salida", DateUtils.formatDisplay(reservation.fechaSalida))
            SummaryRow("Huéspedes", "${reservation.huespedes}")
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total a pagar",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    DateUtils.formatCurrency(total),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PaymentGatewayForm(
    uiState: PaymentUiState,
    viewModel: PaymentViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.CreditCard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "  Tarjeta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.useSavedCard) {
            Text(
                text = "Información de la tarjeta",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            uiState.savedCard?.let { card ->
                SavedPaymentCardSummary(card = card)
            }
            Spacer(modifier = Modifier.height(8.dp))
            PaymentCardCvcField(
                value = uiState.cardCvc,
                onValueChange = viewModel::updateCardCvc,
                label = "CVC",
                error = uiState.cardCvcError,
                resetKey = uiState.useSavedCard
            )
        } else {
            PaymentMethodForm(
                state = uiState.toPaymentMethodFormState(),
                onCardNumberChange = viewModel::updateCardNumber,
                onCardExpiryChange = viewModel::updateCardExpiry,
                onCardCvcChange = viewModel::updateCardCvc,
                onCardholderNameChange = viewModel::updateCardholderName,
                onAddressLine1Change = viewModel::updateAddressLine1,
                onAddressLine2Change = viewModel::updateAddressLine2,
                onDistrictChange = viewModel::updateDistrict,
                onPostalCodeChange = viewModel::updatePostalCode,
                showCvc = true,
                resetKey = uiState.useSavedCard
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
