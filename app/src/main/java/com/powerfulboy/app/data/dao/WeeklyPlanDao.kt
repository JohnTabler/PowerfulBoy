package com.powerfulboy.app.data.dao

import androidx.room.*
import com.powerfulboy.app.data.entity.WeeklyPlanEntity
import kotlinx.coroutines.flow.Flow

data class ShoppingItem(
    val foodName: String,
    val totalGrams: Float
)

@Dao
interface WeeklyPlanDao {
    @Query("SELECT * FROM weekly_plan WHERE weekStart = :weekStart ORDER BY dayIndex, mealType")
    fun getPlanForWeek(weekStart: String): Flow<List<WeeklyPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSlot(slot: WeeklyPlanEntity)

    @Query("DELETE FROM weekly_plan WHERE weekStart = :weekStart AND dayIndex = :dayIndex AND mealType = :mealType")
    suspend fun clearSlot(weekStart: String, dayIndex: Int, mealType: String)

    @Query("DELETE FROM weekly_plan WHERE weekStart = :weekStart")
    suspend fun clearWeek(weekStart: String)

    /**
     * Aggregate all ingredients for a week's plan as a shopping list.
     * Joins weekly_plan → recipe_ingredients → foods.
     */
    @Query("""
        SELECT f.name AS foodName, SUM(ri.grams) AS totalGrams
        FROM weekly_plan wp
        INNER JOIN recipe_ingredients ri ON ri.recipeId = wp.recipeId
        INNER JOIN foods f ON f.id = ri.foodId
        WHERE wp.weekStart = :weekStart AND wp.recipeId IS NOT NULL
        GROUP BY f.name
        ORDER BY f.name
    """)
    suspend fun getShoppingList(weekStart: String): List<ShoppingItem>

    @Query("""
        SELECT wp.*, ri.grams AS ingredientGrams, f.name AS foodName,
               f.calories AS cal, f.protein AS pro, f.carbs AS carb, f.fat AS fat
        FROM weekly_plan wp
        INNER JOIN recipe_ingredients ri ON ri.recipeId = wp.recipeId
        INNER JOIN foods f ON f.id = ri.foodId
        WHERE wp.weekStart = :weekStart AND wp.dayIndex = :dayIndex
    """)
    suspend fun getMealPrepItemsForDay(weekStart: String, dayIndex: Int): List<MealPrepRow>
}

data class MealPrepRow(
    val weekStart: String,
    val dayIndex: Int,
    val mealType: String,
    val recipeId: Long?,
    val recipeName: String,
    val ingredientGrams: Float,
    val foodName: String,
    val cal: Float,
    val pro: Float,
    val carb: Float,
    val fat: Float
)
