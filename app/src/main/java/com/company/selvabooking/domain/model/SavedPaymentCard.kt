package com.company.selvabooking.domain.model

data class SavedPaymentCard(
    val lastFour: String,
    val expiry: String,
    val cardholderName: String,
    val country: String = "Perú",
    val addressLine1: String,
    val addressLine2: String = "",
    val district: String,
    val postalCode: String = "",
    val region: String = "Madre de Dios"
) {
    val maskedNumber: String
        get() = "•••• •••• •••• $lastFour"
}
