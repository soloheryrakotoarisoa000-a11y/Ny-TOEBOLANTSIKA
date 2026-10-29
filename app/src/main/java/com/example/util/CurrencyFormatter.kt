package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {

    fun format(amount: Long, symbol: String = "Ar", isPrefix: Boolean = false): String {
        val symbols = DecimalFormatSymbols(Locale.FRENCH).apply {
            groupingSeparator = ' '
        }
        val formatter = DecimalFormat("#,###", symbols)
        val formattedNumber = formatter.format(amount)

        return if (isPrefix) {
            "$symbol $formattedNumber"
        } else {
            "$formattedNumber $symbol"
        }
    }

    fun parseAmount(input: String): Long {
        val cleaned = input.replace("[^0-9]".toRegex(), "")
        return cleaned.toLongOrNull() ?: 0L
    }
}
