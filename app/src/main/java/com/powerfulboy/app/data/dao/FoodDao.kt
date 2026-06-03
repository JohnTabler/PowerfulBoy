package com.powerfulboy.app.data.dao

import androidx.room.*
import com.powerfulboy.app.data.entity.FoodEntity
import com.powerfulboy.app.data.entity.PantryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' ORDER BY name LIMIT 50")
    fun searchFoods(query: String): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods ORDER BY name")
    fun getAllFoods(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE id = :id")
    suspend fun getFoodById(id: Long): FoodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity): Long

    @Update
    suspend fun updateFood(food: FoodEntity)

    @Delete
    suspend fun deleteFood(food: FoodEntity)

    // ─── Pantry ──────────────────────────────────────────────────────────────
    @Query("SELECT * FROM pantry")
    fun getAllPantry(): Flow<List<PantryEntity>>

    @Query("SELECT * FROM pantry WHERE foodId = :foodId LIMIT 1")
    suspend fun getPantryByFood(foodId: Long): PantryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPantry(pantry: PantryEntity)

    @Query("DELETE FROM pantry WHERE foodId = :foodId")
    suspend fun deletePantryEntry(foodId: Long)

    @Query("""
        SELECT f.name FROM foods f
        INNER JOIN pantry p ON p.foodId = f.id
        WHERE p.preference = 'AVOID'
    """)
    suspend fun getAvoidedIngredientNames(): List<String>

    @Query("""
        SELECT f.name FROM foods f
        INNER JOIN pantry p ON p.foodId = f.id
        WHERE p.preference = 'LOVE'
    """)
    suspend fun getLovedIngredientNames(): List<String>
}
