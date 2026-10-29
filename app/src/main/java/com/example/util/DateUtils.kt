package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val frenchLocale = Locale.FRENCH

    fun formatDateTime(millis: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = millis }

        val isToday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        val timeFormat = SimpleDateFormat("HH:mm", frenchLocale)
        val timeStr = timeFormat.format(Date(millis))

        return when {
            isToday -> "Aujourd'hui à $timeStr"
            isYesterday -> "Hier à $timeStr"
            now.get(Calendar.YEAR) == target.get(Calendar.YEAR) -> {
                val dateFormat = SimpleDateFormat("d MMM à HH:mm", frenchLocale)
                dateFormat.format(Date(millis))
            }
            else -> {
                val fullFormat = SimpleDateFormat("d MMM yyyy à HH:mm", frenchLocale)
                fullFormat.format(Date(millis))
            }
        }
    }

    fun formatDateOnly(millis: Long): String {
        val format = SimpleDateFormat("d MMMM yyyy", frenchLocale)
        return format.format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        val format = SimpleDateFormat("dd/MM/yyyy", frenchLocale)
        return format.format(Date(millis))
    }

    fun getCurrentMonthKey(): String {
        val format = SimpleDateFormat("yyyy-MM", Locale.US)
        return format.format(Date())
    }

    fun getMonthKey(dateMillis: Long): String {
        val format = SimpleDateFormat("yyyy-MM", Locale.US)
        return format.format(Date(dateMillis))
    }

    fun formatMonthLabel(monthKey: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM", Locale.US)
            val date = parser.parse(monthKey) ?: return monthKey
            val formatter = SimpleDateFormat("MMMM yyyy", frenchLocale)
            formatter.format(date).replaceFirstChar { it.uppercase() }
        } catch (_: Exception) {
            monthKey
        }
    }

    fun getStartOfMonth(cal: Calendar = Calendar.getInstance()): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH, 1)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun getEndOfMonth(cal: Calendar = Calendar.getInstance()): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }

    fun getStartOfToday(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun getEndOfToday(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }

    fun getStartOfWeek(): Long {
        val c = Calendar.getInstance()
        c.firstDayOfWeek = Calendar.MONDAY
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun getPreviousMonthRange(): Pair<Long, Long> {
        val c = Calendar.getInstance()
        c.add(Calendar.MONTH, -1)
        return Pair(getStartOfMonth(c), getEndOfMonth(c))
    }

    fun getLast7DaysDays(): List<Triple<Long, Long, String>> {
        val days = mutableListOf<Triple<Long, Long, String>>()
        val dayFormat = SimpleDateFormat("EEE", frenchLocale)
        for (i in 6 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            val start = c.timeInMillis

            c.set(Calendar.HOUR_OF_DAY, 23)
            c.set(Calendar.MINUTE, 59)
            c.set(Calendar.SECOND, 59)
            c.set(Calendar.MILLISECOND, 999)
            val end = c.timeInMillis

            val label = if (i == 0) "Auj." else dayFormat.format(Date(start)).replaceFirstChar { it.uppercase() }
            days.add(Triple(start, end, label))
        }
        return days
    }
}
