package com.company.selvabooking.utils

import java.util.Calendar

object CardExpiryUtils {

    private val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    const val MIN_YEAR = 2021
    const val YEARS_AHEAD = 15

    fun monthLabel(month: Int): String =
        "${String.format("%02d", month)} - ${monthNames[month - 1]}"

    fun monthFromLabel(label: String): Int =
        label.substring(0, 2).toIntOrNull()?.coerceIn(1, 12) ?: 1

    fun format(month: Int, year: Int): String {
        val monthPart = String.format("%02d", month)
        val yearPart = String.format("%02d", year % 100)
        return "$monthPart/$yearPart"
    }

    fun parse(expiry: String): Pair<Int, Int>? {
        if (!Regex("""^\d{2}/\d{2}$""").matches(expiry.trim())) return null
        val month = expiry.substring(0, 2).toIntOrNull() ?: return null
        val yearSuffix = expiry.substring(3, 5).toIntOrNull() ?: return null
        if (month !in 1..12) return null

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val century = currentYear / 100 * 100
        val fullYear = century + yearSuffix
        val adjustedYear = if (fullYear < currentYear - 50) fullYear + 100 else fullYear

        return month to adjustedYear
    }

    fun allMonths(): List<Int> = (1..12).toList()

    fun availableYears(): List<Int> {
        val maxYear = maxSelectableYear()
        return (MIN_YEAR..maxYear).toList()
    }

    fun maxSelectableYear(): Int {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return currentYear + YEARS_AHEAD
    }

    fun isSelectionValid(month: Int, year: Int): Boolean {
        if (month !in 1..12) return false
        if (year !in availableYears()) return false
        return !isExpired(month, year)
    }

    fun isExpired(month: Int, year: Int): Boolean {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        return year < currentYear || (year == currentYear && month < currentMonth)
    }

    fun validationError(month: Int, year: Int): String? {
        if (year !in availableYears()) {
            return "Seleccione un año entre $MIN_YEAR y ${maxSelectableYear()}"
        }
        if (month !in 1..12) {
            return "Seleccione un mes válido"
        }
        if (isExpired(month, year)) {
            return "La fecha no puede ser anterior al mes actual"
        }
        return null
    }
}
