package com.example.util

import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExportHelper {

    fun generateCsv(transactions: List<TransactionEntity>, currencySymbol: String): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.FRENCH)

        val sb = StringBuilder()
        // Add UTF-8 Byte Order Mark (BOM) for Excel compatibility with accents
        sb.append('\uFEFF')
        sb.append("ID;Date;Heure;Type;Montant;Devise;Categorie;Mode de Paiement;Description\n")

        for (tx in transactions) {
            val date = Date(tx.dateMillis)
            val dateStr = dateFormat.format(date)
            val timeStr = timeFormat.format(date)
            val typeStr = if (tx.type == TransactionType.INCOME) "Revenu" else "Dépense"
            val cleanDesc = tx.description.replace(";", ",").replace("\n", " ").trim()
            val cleanCat = tx.categoryName.replace(";", ",").trim()

            sb.append("${tx.id};")
            sb.append("$dateStr;")
            sb.append("$timeStr;")
            sb.append("$typeStr;")
            sb.append("${tx.amount};")
            sb.append("$currencySymbol;")
            sb.append("$cleanCat;")
            sb.append("${tx.paymentMethod.labelFr};")
            sb.append("\"$cleanDesc\"\n")
        }

        return sb.toString()
    }
}
