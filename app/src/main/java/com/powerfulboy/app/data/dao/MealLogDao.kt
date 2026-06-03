package com.powerfulboy.app.data.dao

import androidx.room.*
import com.powerfulboy.app.data.entity.MealLogEntity
import kotlinx.coroutines.flow.Flow

data class DayMacros(
    val totalCalories: Float,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float
)

@Dao
interface MealLogDao {
    @Query("SELECT * FROM meal_log WHERE date = :date ORDER BY loggedAt")
    fun getMealsForDate(date: String): Flow<List<MealLogEntity>>

    @Query("""
        SELECT 
            COALESCE(SUM(calories), 0) AS totalCalories,
            COALESCE(SUM(protein), 0) AS totalProtein,
            COALESCE(SUM(carbs), 0) AS totalCarbs,
            COALESCE(SUM(fat), 0) AS totalFat
        FROM meal_log WHERE date = :date
    """)
    fun getDayMacros(date: String): Flow<DayMacros>

    @Query("""
        SELECT 
            COALESCE(SUM(calories), 0) AS totalCalories,
            COALESCE(SUM(protein), 0) AS totalProtein,
            COALESCE(SUM(carbs), 0) AS totalCarbs,
            COALESCE(SUM(fat), 0) AS totalFat
        FROM meal_log WHERE date = :date AND mealType = :mealType
    """)
    suspend fun getMealTypeMacros(date: String, mealType: String): DayMacros

    @Insert
    suspend fun insertMealLog(entry: MealLogEntity): Long

    @Delete
    suspend fun deleteMealLog(entry: MealLogEntity)

    @Query("DELETE FROM meal_log WHERE id = :id")
    suspend fun deleteMealLogById(id: Long)
}
