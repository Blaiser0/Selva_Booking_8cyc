package com.company.selvabooking.utils

import android.util.Patterns

object ValidationUtils {

    const val MAX_PHONE_LENGTH = 9
    private const val MIN_NAME_LENGTH = 3

    fun filterPhoneInput(value: String): String =
        value.filter { it.isDigit() }.take(MAX_PHONE_LENGTH)

    fun filterPersonNameInput(value: String): String =
        value.filter { it.isLetter() || it == ' ' }

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidPhone(phone: String): Boolean {
        val digits = phone.filter { it.isDigit() }
        return digits.length == MAX_PHONE_LENGTH
    }

    fun isValidName(name: String): Boolean {
        val trimmed = name.trim()
        return trimmed.length >= MIN_NAME_LENGTH &&
            trimmed.all { it.isLetter() || it == ' ' }
    }

    fun passwordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    fun isValidCardNumber(number: String): Boolean {
        val digits = number.filter { it.isDigit() }
        return digits.length == 16
    }

    fun isValidCardExpiry(expiry: String): Boolean {
        if (!Regex("""^\d{2}/\d{2}$""").matches(expiry.trim())) return false
        val parsed = CardExpiryUtils.parse(expiry) ?: return false
        return CardExpiryUtils.isSelectionValid(parsed.first, parsed.second)
    }

    fun isValidCvc(cvc: String): Boolean {
        val digits = cvc.filter { it.isDigit() }
        return digits.length in 3..4
    }
}
