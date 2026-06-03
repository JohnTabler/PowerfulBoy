package com.powerfulboy.app.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

object DateUtil {
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun today(): String = LocalDate.now().format(fmt)

    fun currentWeekStart(): String {
        val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return monday.format(fmt)
    }

    fun weekStartFor(date: LocalDate): String {
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return monday.format(fmt)
    }

    fun daysOfWeek(weekStart: String): List<LocalDate> {
        val monday = LocalDate.parse(weekStart, fmt)
        return (0..6).map { monday.plusDays(it.toLong()) }
    }

    fun format(date: String): String {
        return try {
            val ld = LocalDate.parse(date, fmt)
            ld.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
        } catch (e: Exception) {
            date
        }
    }

    fun dayLabel(dayIndex: Int): String =
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")[dayIndex.coerceIn(0, 6)]

    fun fullDayLabel(dayIndex: Int): String =
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")[dayIndex.coerceIn(0, 6)]
}

object MacroCalc {
    fun calcFromPer100g(
        calories100g: Float,
        protein100g: Float,
        carbs100g: Float,
        fat100g: Float,
        grams: Float
    ): Quad {
        val factor = grams / 100f
        return Quad(
            calories = calories100g * factor,
            protein = protein100g * factor,
            carbs = carbs100g * factor,
            fat = fat100g * factor
        )
    }
}

data class Quad(
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float
) {
    operator fun plus(other: Quad) = Quad(
        calories + other.calories,
        protein + other.protein,
        carbs + other.carbs,
        fat + other.fat
    )

    companion object {
        val ZERO = Quad(0f, 0f, 0f, 0f)
    }
}

enum class MealType(val label: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner");

    companion object {
        fun fromString(s: String) = entries.firstOrNull { it.name == s } ?: BREAKFAST
    }
}
