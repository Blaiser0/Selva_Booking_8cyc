package com.company.selvabooking.utils

import com.company.selvabooking.domain.model.PaymentMethodFormState

object PaymentMethodFormValidator {

    fun normalizeBillingState(state: PaymentMethodFormState): PaymentMethodFormState {
        if (state.district.isBlank()) return state
        return state.copy(
            postalCode = MadreDeDiosDistricts.resolvePostalCodeOnDistrictChange(
                district = state.district,
                currentPostalCode = state.postalCode
            )
        )
    }

    fun validate(
        state: PaymentMethodFormState,
        requireCvc: Boolean = true,
        requireCardNumber: Boolean = true
    ): PaymentMethodFormState? {
        val cardNumberError = if (requireCardNumber && !ValidationUtils.isValidCardNumber(state.cardNumber)) {
            "Ingrese un número de tarjeta de 16 dígitos"
        } else null
        val cardExpiryError = if (!ValidationUtils.isValidCardExpiry(state.cardExpiry)) {
            "Seleccione una fecha de vencimiento válida"
        } else null
        val cardCvcError = when {
            !requireCvc -> null
            !ValidationUtils.isValidCvc(state.cardCvc) -> "CVC inválido"
            else -> null
        }
        val cardholderNameError = if (!ValidationUtils.isValidName(state.cardholderName)) {
            "Nombre del titular requerido"
        } else null
        val addressLine1Error = if (state.addressLine1.isBlank()) {
            "Ingrese la dirección de facturación"
        } else null
        val districtError = when {
            state.district.isBlank() -> "Seleccione el distrito"
            !MadreDeDiosDistricts.isValidDistrict(state.district) -> "Distrito no válido"
            else -> null
        }
        val postalCodeError = when {
            state.district.isBlank() -> null
            state.postalCode.isBlank() -> "Seleccione el código postal"
            !MadreDeDiosDistricts.isValidPostalCode(state.district, state.postalCode) ->
                "Código postal no válido para el distrito"
            else -> null
        }

        if (listOf(
                cardNumberError,
                cardExpiryError,
                cardCvcError,
                cardholderNameError,
                addressLine1Error,
                districtError,
                postalCodeError
            ).any { it != null }
        ) {
            return state.copy(
                cardNumberError = cardNumberError,
                cardExpiryError = cardExpiryError,
                cardCvcError = cardCvcError,
                cardholderNameError = cardholderNameError,
                addressLine1Error = addressLine1Error,
                districtError = districtError,
                postalCodeError = postalCodeError
            )
        }
        return null
    }
}
