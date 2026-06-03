package com.powerfulboy.app.data.dao

import androidx.room.*
import com.powerfulboy.app.data.entity.RecipeEntity
import com.powerfulboy.app.data.entity.RecipeIngredientEntity
import kotlinx.coroutines.flow.Flow

data class RecipeWithIngredients(
    val recipe: RecipeEntity,
    val ingredients: List<RecipeIngredientDetail>
)

data class RecipeIngredientDetail(
    val ingredientId: Long,
    val foodId: Long,
    val foodName: String,
    val grams: Float,
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val carbsPer100g: Float,
    val fatPer100g: Float
)

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY name")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Long): RecipeEntity?

    @Query("""
        SELECT 
            ri.id AS ingredientId,
            ri.foodId,
            f.name AS foodName,
            ri.grams,
            f.calories AS caloriesPer100g,
            f.protein AS proteinPer100g,
            f.carbs AS carbsPer100g,
            f.fat AS fatPer100g
        FROM recipe_ingredients ri
        INNER JOIN foods f ON f.id = ri.foodId
        WHERE ri.recipeId = :recipeId
    """)
    suspend fun getIngredientsForRecipe(recipeId: Long): List<RecipeIngredientDetail>

    @Query("""
        SELECT 
            ri.id AS ingredientId,
            ri.foodId,
            f.name AS foodName,
            ri.grams,
            f.calories AS caloriesPer100g,
            f.protein AS proteinPer100g,
            f.carbs AS carbsPer100g,
            f.fat AS fatPer100g
        FROM recipe_ingredients ri
        INNER JOIN foods f ON f.id = ri.foodId
        WHERE ri.recipeId = :recipeId
    """)
    fun getIngredientsForRecipeFlow(recipeId: Long): Flow<List<RecipeIngredientDetail>>

    @Insert
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)

    @Insert
    suspend fun insertIngredient(ingredient: RecipeIngredientEntity): Long

    @Delete
    suspend fun deleteIngredient(ingredient: RecipeIngredientEntity)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteAllIngredientsForRecipe(recipeId: Long)

    @Query("SELECT * FROM recipe_ingredients WHERE id = :id")
    suspend fun getIngredientById(id: Long): RecipeIngredientEntity?
}
