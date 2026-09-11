package com.company.selvabooking.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val storageFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun formatDisplay(date: String): String {
        return try {
            val parsed = storageFormat.parse(date)
            if (parsed != null) displayFormat.format(parsed) else date
        } catch (_: Exception) {
            date
        }
    }

    fun formatStorage(calendar: Calendar): String {
        return storageFormat.format(calendar.time)
    }

    fun parseStorage(date: String): Date? {
        return try {
            storageFormat.parse(date)
        } catch (_: Exception) {
            null
        }
    }

    fun daysBetween(startDate: String, endDate: String): Int {
        val start = parseStorage(startDate) ?: return 0
        val end = parseStorage(endDate) ?: return 0
        val diff = end.time - start.time
        return TimeUnit.MILLISECONDS.toDays(diff).toInt().coerceAtLeast(0)
    }

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
        return format.format(amount)
    }

    fun todayStorage(): String = formatStorage(Calendar.getInstance())

    /** Devuelve true si la fecha de salida ya pasó (habitación puede liberarse). */
    fun isCheckoutPast(fechaSalida: String): Boolean {
        return fechaSalida < todayStorage()
    }

    fun formatTimestamp(timestamp: Long): String {
        if (timestamp <= 0L) return ""
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.format(Date(timestamp))
    }
}
