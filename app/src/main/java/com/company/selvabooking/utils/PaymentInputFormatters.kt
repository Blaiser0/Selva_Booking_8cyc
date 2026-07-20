package com.company.selvabooking.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object PaymentInputFormatters {

    fun formatCardNumber(value: TextFieldValue): TextFieldValue {
        val digits = value.text.filter { it.isDigit() }.take(19)
        val formatted = digits.chunked(4).joinToString(" ")
        val cursor = cursorAfterDigits(
            source = value.text,
            formatted = formatted,
            selectionStart = value.selection.start
        )
        return TextFieldValue(formatted, TextRange(cursor))
    }

    fun formatCardExpiry(value: TextFieldValue): TextFieldValue {
        val digits = value.text.filter { it.isDigit() }.take(4)
        val formatted = when {
            digits.length <= 2 -> digits
            else -> "${digits.take(2)}/${digits.drop(2)}"
        }
        val digitsBeforeCursor = digitsBeforeCursor(value.text, value.selection.start)
        val cursor = when {
            digitsBeforeCursor <= 2 -> digitsBeforeCursor.coerceIn(0, formatted.length)
            else -> (digitsBeforeCursor + 1).coerceIn(0, formatted.length)
        }
        return TextFieldValue(formatted, TextRange(cursor))
    }

    fun formatCvc(input: String): String = input.filter { it.isDigit() }.take(4)

    private fun digitsBeforeCursor(text: String, selectionStart: Int): Int {
        return text.take(selectionStart.coerceIn(0, text.length)).count { it.isDigit() }
    }

    private fun cursorAfterDigits(source: String, formatted: String, selectionStart: Int): Int {
        val digitsBeforeCursor = digitsBeforeCursor(source, selectionStart)
        if (digitsBeforeCursor <= 0) return 0

        var digitsSeen = 0
        formatted.forEachIndexed { index, char ->
            if (char.isDigit()) {
                digitsSeen++
                if (digitsSeen == digitsBeforeCursor) {
                    return index + 1
                }
            }
        }
        return formatted.length
    }
}
