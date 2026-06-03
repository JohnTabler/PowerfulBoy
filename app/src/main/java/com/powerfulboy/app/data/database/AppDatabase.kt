package com.powerfulboy.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.powerfulboy.app.data.dao.*
import com.powerfulboy.app.data.entity.*

@Database(
    entities = [
        FoodEntity::class,
        PantryEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        MealLogEntity::class,
        WeeklyPlanEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun recipeDao(): RecipeDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun weeklyPlanDao(): WeeklyPlanDao
    abstract fun settingsDao(): SettingsDao
}
