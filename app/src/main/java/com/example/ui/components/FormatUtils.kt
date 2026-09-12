package com.example.ui.components

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object CurrencyUtils {
    private val decimalFormat = DecimalFormat("#,##0.00")
    private val integerFormat = DecimalFormat("#,##0")

    fun formatNaira(amount: Double, includeDecimals: Boolean = true): String {
        val formatted = if (includeDecimals) {
            decimalFormat.format(amount)
        } else {
            integerFormat.format(amount)
        }
        return "₦$formatted"
    }

    fun formatNairaCompact(amount: Double): String {
        return when {
            amount >= 1_000_000 -> {
                val value = amount / 1_000_000
                "₦${DecimalFormat("#,##0.1").format(value)}M"
            }
            amount >= 1_000 -> {
                val value = amount / 1_000
                "₦${DecimalFormat("#,##0.1").format(value)}K"
            }
            else -> "₦${integerFormat.format(amount)}"
        }
    }
}

object DateUtils {
    private val fullDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val shortDateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

    fun formatDate(millis: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = millis }

        val isToday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        if (isToday) return "Today, " + timeFormatter.format(Date(millis))

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        if (isYesterday) return "Yesterday, " + timeFormatter.format(Date(millis))

        return fullDateFormatter.format(Date(millis))
    }

    fun formatDateOnly(millis: Long): String {
        return fullDateFormatter.format(Date(millis))
    }

    fun formatDayMonth(millis: Long): String {
        return shortDateFormatter.format(Date(millis))
    }

    fun isToday(millis: Long): Boolean {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = millis }
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    fun isThisWeek(millis: Long): Boolean {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = millis }
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.WEEK_OF_YEAR) == target.get(Calendar.WEEK_OF_YEAR)
    }

    fun isThisMonth(millis: Long): Boolean {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = millis }
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.MONTH) == target.get(Calendar.MONTH)
    }

    fun isWithinLastDays(millis: Long, days: Int): Boolean {
        val threshold = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -days)
        }.timeInMillis
        return millis >= threshold
    }
}
