package com.company.selvabooking.utils

import android.util.Patterns

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.isNotBlank() && phone.trim().length >= 9
    }

    fun isValidName(name: String): Boolean {
        return name.trim().length >= 3
    }

    fun passwordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    fun isValidCardNumber(number: String): Boolean {
        val digits = number.filter { it.isDigit() }
        return digits.length in 15..19
    }

    fun isValidCardExpiry(expiry: String): Boolean {
        if (!Regex("""^\d{2}/\d{2}$""").matches(expiry.trim())) return false
        val parts = expiry.split("/")
        val month = parts[0].toIntOrNull() ?: return false
        val year = parts[1].toIntOrNull() ?: return false
        return month in 1..12 && year in 0..99
    }

    fun isValidCvc(cvc: String): Boolean {
        val digits = cvc.filter { it.isDigit() }
        return digits.length in 3..4
    }
}
