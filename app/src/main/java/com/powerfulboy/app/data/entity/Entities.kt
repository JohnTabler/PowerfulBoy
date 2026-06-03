package com.powerfulboy.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ─── Food ────────────────────────────────────────────────────────────────────
@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String? = null,
    /** All macros stored per 100g */
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val source: String = "manual", // "manual" | "openfoodfacts"
    val openFoodFactsId: String? = null
)

// ─── Ingredient preference (pantry) ──────────────────────────────────────────
@Entity(
    tableName = "pantry",
    foreignKeys = [ForeignKey(
        entity = FoodEntity::class,
        parentColumns = ["id"],
        childColumns = ["foodId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("foodId")]
)
data class PantryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val foodId: Long,
    /** LOVE | AVOID | NEUTRAL */
    val preference: String = "NEUTRAL"
)

// ─── Recipe ──────────────────────────────────────────────────────────────────
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val instructions: String = "",
    val servings: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

// ─── Recipe ingredients ───────────────────────────────────────────────────────
@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId"), Index("foodId")]
)
data class RecipeIngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val foodId: Long,
    val grams: Float
)

// ─── Meal log (daily tracking) ────────────────────────────────────────────────
@Entity(
    tableName = "meal_log",
    foreignKeys = [
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("foodId"), Index("date")]
)
data class MealLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** ISO date string yyyy-MM-dd */
    val date: String,
    /** BREAKFAST | LUNCH | DINNER */
    val mealType: String,
    val foodId: Long? = null,
    val foodName: String = "", // cached for display even if food deleted
    val grams: Float,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val loggedAt: Long = System.currentTimeMillis()
)

// ─── Weekly plan ──────────────────────────────────────────────────────────────
@Entity(
    tableName = "weekly_plan",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("recipeId")],
    primaryKeys = ["weekStart", "dayIndex", "mealType"]
)
data class WeeklyPlanEntity(
    /** Monday of the week, yyyy-MM-dd */
    val weekStart: String,
    /** 0=Mon … 6=Sun */
    val dayIndex: Int,
    /** BREAKFAST | LUNCH | DINNER */
    val mealType: String,
    val recipeId: Long? = null,
    val recipeName: String = "" // cached
)

// ─── Settings ─────────────────────────────────────────────────────────────────
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val targetCalories: Int = 2000,
    val targetProtein: Int = 150,
    val targetCarbs: Int = 200,
    val targetFat: Int = 65,
    val dietaryRestrictions: String = "no cheese, very minimal dairy, no beets",
    val anthropicApiKey: String = "",
    val onboardingDone: Boolean = false
)
