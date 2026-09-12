package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExportUtils {

    fun generateCsvString(transactions: List<TransactionEntity>): String {
        val sb = StringBuilder()
        // Header
        sb.append("ID,Type,Category,Amount (NGN),Note,Date,Timestamp\n")

        var totalIncome = 0.0
        var totalExpense = 0.0

        transactions.forEach { tx ->
            val typeStr = tx.type.name
            val category = escapeCsv(tx.category)
            val amount = String.format(Locale.US, "%.2f", tx.amount)
            val note = escapeCsv(tx.note)
            val dateStr = escapeCsv(DateUtils.formatDate(tx.dateMillis))

            if (tx.type == TransactionType.INCOME) {
                totalIncome += tx.amount
            } else {
                totalExpense += tx.amount
            }

            sb.append("${tx.id},$typeStr,$category,$amount,$note,$dateStr,${tx.dateMillis}\n")
        }

        // Summary row
        sb.append("\n")
        sb.append("--- SUMMARY ---\n")
        sb.append("Total Transactions,${transactions.size}\n")
        sb.append("Total Income (NGN),${String.format(Locale.US, "%.2f", totalIncome)}\n")
        sb.append("Total Expense (NGN),${String.format(Locale.US, "%.2f", totalExpense)}\n")
        sb.append("Net Balance (NGN),${String.format(Locale.US, "%.2f", totalIncome - totalExpense)}\n")
        sb.append("Exported Date,${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")

        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    fun exportAndShareCsv(context: Context, transactions: List<TransactionEntity>): Boolean {
        return try {
            val csvContent = generateCsvString(transactions)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "naira_tracker_export_$timeStamp.csv"

            val cacheDir = context.cacheDir
            val exportFile = File(cacheDir, fileName)
            exportFile.writeText(csvContent)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                exportFile
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Naira Tracker - Transactions Export ($timeStamp)")
                putExtra(Intent.EXTRA_TEXT, "Attached is your transaction history export from Naira Tracker.")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(sendIntent, "Share Transactions CSV")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun writeToUri(context: Context, outputStream: OutputStream?, transactions: List<TransactionEntity>): Boolean {
        return try {
            outputStream?.use { stream ->
                val csvContent = generateCsvString(transactions)
                stream.write(csvContent.toByteArray(Charsets.UTF_8))
                stream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
