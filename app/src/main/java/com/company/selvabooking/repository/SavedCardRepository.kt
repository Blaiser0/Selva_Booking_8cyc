package com.company.selvabooking.repository

import android.content.Context
import com.company.selvabooking.domain.model.SavedPaymentCard

class SavedCardRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSavedCard(): SavedPaymentCard? {
        val lastFour = prefs.getString(KEY_LAST_FOUR, null) ?: return null
        val expiry = prefs.getString(KEY_EXPIRY, null) ?: return null
        val cardholderName = prefs.getString(KEY_CARDHOLDER, null) ?: return null
        val addressLine1 = prefs.getString(KEY_ADDRESS_1, null) ?: return null
        val district = prefs.getString(KEY_DISTRICT, null) ?: return null

        return SavedPaymentCard(
            lastFour = lastFour,
            expiry = expiry,
            cardholderName = cardholderName,
            country = prefs.getString(KEY_COUNTRY, "Perú") ?: "Perú",
            addressLine1 = addressLine1,
            addressLine2 = prefs.getString(KEY_ADDRESS_2, "").orEmpty(),
            district = district,
            postalCode = prefs.getString(KEY_POSTAL_CODE, "").orEmpty(),
            region = prefs.getString(KEY_REGION, "Madre de Dios") ?: "Madre de Dios"
        )
    }

    fun saveCard(card: SavedPaymentCard) {
        prefs.edit()
            .putString(KEY_LAST_FOUR, card.lastFour)
            .putString(KEY_EXPIRY, card.expiry)
            .putString(KEY_CARDHOLDER, card.cardholderName)
            .putString(KEY_COUNTRY, card.country)
            .putString(KEY_ADDRESS_1, card.addressLine1)
            .putString(KEY_ADDRESS_2, card.addressLine2)
            .putString(KEY_DISTRICT, card.district)
            .putString(KEY_POSTAL_CODE, card.postalCode)
            .putString(KEY_REGION, card.region)
            .apply()
    }

    fun clearCard() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "selva_saved_payment_card"
        private const val KEY_LAST_FOUR = "last_four"
        private const val KEY_EXPIRY = "expiry"
        private const val KEY_CARDHOLDER = "cardholder"
        private const val KEY_COUNTRY = "country"
        private const val KEY_ADDRESS_1 = "address_1"
        private const val KEY_ADDRESS_2 = "address_2"
        private const val KEY_DISTRICT = "district"
        private const val KEY_POSTAL_CODE = "postal_code"
        private const val KEY_REGION = "region"
    }
}
