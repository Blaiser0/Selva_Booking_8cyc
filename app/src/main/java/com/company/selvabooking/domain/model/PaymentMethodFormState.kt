package com.company.selvabooking.domain.model

import com.company.selvabooking.utils.MadreDeDiosDistricts

data class PaymentMethodFormState(
    val cardNumber: String = "",
    val cardExpiry: String = "",
    val cardCvc: String = "",
    val cardholderName: String = "",
    val country: String = "Perú",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val district: String = "",
    val postalCode: String = "",
    val region: String = "Madre de Dios",
    val cardNumberError: String? = null,
    val cardExpiryError: String? = null,
    val cardCvcError: String? = null,
    val cardholderNameError: String? = null,
    val addressLine1Error: String? = null,
    val districtError: String? = null,
    val postalCodeError: String? = null
)

fun SavedPaymentCard.toFormState(): PaymentMethodFormState {
    val district = district.takeIf { MadreDeDiosDistricts.isValidDistrict(it) }.orEmpty()
    val postalCode = MadreDeDiosDistricts.resolvePostalCodeOnDistrictChange(
        district = district,
        currentPostalCode = postalCode
    )
    return PaymentMethodFormState(
        cardNumber = "",
        cardExpiry = expiry,
        cardholderName = cardholderName,
        country = country,
        addressLine1 = addressLine1,
        addressLine2 = addressLine2,
        district = district,
        postalCode = postalCode,
        region = region
    )
}

fun PaymentMethodFormState.toSavedPaymentCard(): SavedPaymentCard? {
    val digits = cardNumber.filter { it.isDigit() }
    if (digits.length < 4) return null
    return SavedPaymentCard(
        lastFour = digits.takeLast(4),
        expiry = cardExpiry,
        cardholderName = cardholderName,
        country = country,
        addressLine1 = addressLine1,
        addressLine2 = addressLine2,
        district = district,
        postalCode = postalCode,
        region = region
    )
}
