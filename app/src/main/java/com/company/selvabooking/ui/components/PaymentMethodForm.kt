package com.company.selvabooking.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.company.selvabooking.domain.model.PaymentMethodFormState
import com.company.selvabooking.domain.model.SavedPaymentCard
import com.company.selvabooking.utils.MadreDeDiosDistricts

@Composable
fun PaymentMethodForm(
    state: PaymentMethodFormState,
    onCardNumberChange: (String) -> Unit,
    onCardExpiryChange: (String) -> Unit,
    onCardCvcChange: (String) -> Unit,
    onCardholderNameChange: (String) -> Unit,
    onAddressLine1Change: (String) -> Unit,
    onAddressLine2Change: (String) -> Unit,
    onDistrictChange: (String) -> Unit,
    onPostalCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    showCvc: Boolean = true,
    cardNumberLabel: String = "1234 1234 1234 1234",
    resetKey: Any? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Información de la tarjeta",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PaymentCardNumberField(
                value = state.cardNumber,
                onValueChange = onCardNumberChange,
                label = cardNumberLabel,
                error = state.cardNumberError,
                resetKey = resetKey
            )
            if (showCvc) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PaymentCardExpiryField(
                            value = state.cardExpiry,
                            onValueChange = onCardExpiryChange,
                            label = "MM/AA",
                            error = state.cardExpiryError,
                            modifier = Modifier.weight(1f),
                            resetKey = resetKey
                        )
                        PaymentCardCvcField(
                            value = state.cardCvc,
                            onValueChange = onCardCvcChange,
                            label = "CVC",
                            error = state.cardCvcError,
                            modifier = Modifier.weight(1f),
                            resetKey = resetKey
                        )
                    }
                    Text(
                        text = "VISA · Mastercard · AMEX · Discover",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                PaymentCardExpiryField(
                    value = state.cardExpiry,
                    onValueChange = onCardExpiryChange,
                    label = "MM/AA",
                    error = state.cardExpiryError,
                    resetKey = resetKey
                )
            }
            SelvaTextField(
                value = state.cardholderName,
                onValueChange = onCardholderNameChange,
                label = "Nombre del titular de tarjeta",
                error = state.cardholderNameError
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        PaymentBillingAddressSection(
            state = state,
            onAddressLine1Change = onAddressLine1Change,
            onAddressLine2Change = onAddressLine2Change,
            onDistrictChange = onDistrictChange,
            onPostalCodeChange = onPostalCodeChange
        )
    }
}

@Composable
fun SavedPaymentCardSummary(
    card: SavedPaymentCard,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = card.maskedNumber,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = card.cardholderName,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Vence ${card.expiry}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = card.addressLine1,
            style = MaterialTheme.typography.bodySmall
        )
        if (card.addressLine2.isNotBlank()) {
            Text(
                text = card.addressLine2,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = "${card.district}, ${card.region} ${card.postalCode}, ${card.country}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PaymentBillingAddressSection(
    state: PaymentMethodFormState,
    onAddressLine1Change: (String) -> Unit,
    onAddressLine2Change: (String) -> Unit,
    onDistrictChange: (String) -> Unit,
    onPostalCodeChange: (String) -> Unit
) {
    Text(
        text = "Dirección de facturación",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(8.dp))
    PaymentBillingStaticField(label = "País", value = state.country)
    Spacer(modifier = Modifier.height(8.dp))
    SelvaTextField(
        value = state.addressLine1,
        onValueChange = onAddressLine1Change,
        label = "Línea 1 de dirección",
        error = state.addressLine1Error
    )
    Spacer(modifier = Modifier.height(8.dp))
    SelvaTextField(
        value = state.addressLine2,
        onValueChange = onAddressLine2Change,
        label = "Línea 2 de dirección"
    )
    Spacer(modifier = Modifier.height(8.dp))
    SelvaDropdownField(
        value = state.district,
        onValueChange = onDistrictChange,
        label = "Distrito",
        options = MadreDeDiosDistricts.districts,
        error = state.districtError,
        placeholder = "Seleccionar distrito"
    )
    Spacer(modifier = Modifier.height(8.dp))
    PaymentBillingPostalCodeField(
        district = state.district,
        postalCode = state.postalCode,
        postalCodeError = state.postalCodeError,
        onPostalCodeChange = onPostalCodeChange
    )
    Spacer(modifier = Modifier.height(8.dp))
    PaymentBillingStaticField(label = "Departamento / Región", value = state.region)
}

@Composable
private fun PaymentBillingPostalCodeField(
    district: String,
    postalCode: String,
    postalCodeError: String?,
    onPostalCodeChange: (String) -> Unit
) {
    val postalCodes = MadreDeDiosDistricts.postalCodesFor(district)

    when {
        district.isBlank() -> {
            PaymentBillingStaticField(
                label = "Código postal",
                value = "Seleccione un distrito primero"
            )
        }

        postalCodes.size == 1 -> {
            PaymentBillingStaticField(
                label = "Código postal",
                value = postalCodes.first()
            )
        }

        else -> {
            SelvaDropdownField(
                value = postalCode,
                onValueChange = onPostalCodeChange,
                label = "Código postal",
                options = postalCodes,
                error = postalCodeError,
                placeholder = "Seleccionar código postal"
            )
        }
    }
}

@Composable
fun PaymentBillingStaticField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        )
    }
}
