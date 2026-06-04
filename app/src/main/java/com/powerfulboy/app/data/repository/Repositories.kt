package com.powerfulboy.app.data.repository

import com.google.gson.Gson
import com.powerfulboy.app.data.dao.*
import com.powerfulboy.app.data.entity.*
import com.powerfulboy.app.network.AnthropicApi
import com.powerfulboy.app.network.OpenFoodFactsApi
import com.powerfulboy.app.network.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// ─── Food Repository ──────────────────────────────────────────────────────────
@Singleton
class FoodRepository @Inject constructor(
    private val foodDao: FoodDao,
    private val offApi: OpenFoodFactsApi
) {
    fun searchLocalFoods(query: String): Flow<List<FoodEntity>> =
        foodDao.searchFoods(query)

    fun getAllFoods(): Flow<List<FoodEntity>> = foodDao.getAllFoods()

    suspend fun getFoodById(id: Long): FoodEntity? = foodDao.getFoodById(id)

    suspend fun searchRemoteFoods(query: String): List<FoodEntity> {
        return try {
            val response = offApi.searchFoods(query)
            response.products
                .filter { it.productName.isNotBlank() }
                .map { p ->
                    FoodEntity(
                        name = p.productName.trim(),
                        brand = p.brands.takeIf { it.isNotBlank() },
                        calories = p.nutriments.calories ?: 0f,
                        protein = p.nutriments.protein ?: 0f,
                        carbs = p.nutriments.carbs ?: 0f,
                        fat = p.nutriments.fat ?: 0f,
                        source = "openfoodfacts",
                        openFoodFactsId = p.id
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveFood(food: FoodEntity): Long = foodDao.insertFood(food)
    suspend fun updateFood(food: FoodEntity) = foodDao.updateFood(food)
    suspend fun deleteFood(food: FoodEntity) = foodDao.deleteFood(food)

    fun getAllPantry(): Flow<List<PantryEntity>> = foodDao.getAllPantry()

    suspend fun setPantryPreference(foodId: Long, preference: String) {
        if (preference == "NEUTRAL") {
            foodDao.deletePantryEntry(foodId)
        } else {
            val existing = foodDao.getPantryByFood(foodId)
            foodDao.upsertPantry(
                PantryEntity(id = existing?.id ?: 0, foodId = foodId, preference = preference)
            )
        }
    }

    suspend fun getAvoidedIngredientNames(): List<String> = foodDao.getAvoidedIngredientNames()
    suspend fun getLovedIngredientNames(): List<String> = foodDao.getLovedIngredientNames()
}

// ─── Recipe Repository ────────────────────────────────────────────────────────
@Singleton
class RecipeRepository @Inject constructor(private val recipeDao: RecipeDao) {
    fun getAllRecipes(): Flow<List<RecipeEntity>> = recipeDao.getAllRecipes()

    suspend fun getRecipeById(id: Long): RecipeEntity? = recipeDao.getRecipeById(id)

    suspend fun getRecipeWithIngredients(recipeId: Long): RecipeWithIngredients? {
        val recipe = recipeDao.getRecipeById(recipeId) ?: return null
        val ingredients = recipeDao.getIngredientsForRecipe(recipeId)
        return RecipeWithIngredients(recipe, ingredients)
    }

    fun getIngredientsFlow(recipeId: Long): Flow<List<RecipeIngredientDetail>> =
        recipeDao.getIngredientsForRecipeFlow(recipeId)

    suspend fun insertRecipe(recipe: RecipeEntity): Long = recipeDao.insertRecipe(recipe)
    suspend fun updateRecipe(recipe: RecipeEntity) = recipeDao.updateRecipe(recipe)
    suspend fun deleteRecipe(recipe: RecipeEntity) = recipeDao.deleteRecipe(recipe)

    suspend fun addIngredient(ingredient: RecipeIngredientEntity): Long =
        recipeDao.insertIngredient(ingredient)

    suspend fun deleteIngredient(ingredient: RecipeIngredientEntity) =
        recipeDao.deleteIngredient(ingredient)

    suspend fun replaceIngredients(recipeId: Long, ingredients: List<RecipeIngredientEntity>) {
        recipeDao.deleteAllIngredientsForRecipe(recipeId)
        ingredients.forEach { recipeDao.insertIngredient(it) }
    }
}

// ─── MealLog Repository ───────────────────────────────────────────────────────
@Singleton
class MealLogRepository @Inject constructor(private val mealLogDao: MealLogDao) {
    fun getMealsForDate(date: String): Flow<List<MealLogEntity>> =
        mealLogDao.getMealsForDate(date)

    fun getDayMacros(date: String): Flow<DayMacros> = mealLogDao.getDayMacros(date)

    suspend fun logMeal(entry: MealLogEntity): Long = mealLogDao.insertMealLog(entry)
    suspend fun deleteMealLog(entry: MealLogEntity) = mealLogDao.deleteMealLog(entry)
    suspend fun deleteMealLogById(id: Long) = mealLogDao.deleteMealLogById(id)
}

// ─── WeeklyPlan Repository ────────────────────────────────────────────────────
@Singleton
class WeeklyPlanRepository @Inject constructor(private val weeklyPlanDao: WeeklyPlanDao) {
    fun getPlanForWeek(weekStart: String): Flow<List<WeeklyPlanEntity>> =
        weeklyPlanDao.getPlanForWeek(weekStart)

    suspend fun upsertSlot(slot: WeeklyPlanEntity) = weeklyPlanDao.upsertSlot(slot)
    suspend fun clearSlot(weekStart: String, dayIndex: Int, mealType: String) =
        weeklyPlanDao.clearSlot(weekStart, dayIndex, mealType)

    suspend fun getShoppingList(weekStart: String): List<ShoppingItem> =
        weeklyPlanDao.getShoppingList(weekStart)
}

// ─── Settings Repository ──────────────────────────────────────────────────────
@Singleton
class SettingsRepository @Inject constructor(private val settingsDao: SettingsDao) {
    fun getSettings(): Flow<SettingsEntity?> = settingsDao.getSettings()

    suspend fun getSettingsOnce(): SettingsEntity =
        settingsDao.getSettingsOnce() ?: SettingsEntity()

    suspend fun saveSettings(settings: SettingsEntity) = settingsDao.upsertSettings(settings)
}

// ─── AI Repository ────────────────────────────────────────────────────────────
@Singleton
class AiRepository @Inject constructor(
    private val anthropicApi: AnthropicApi,
    private val gson: Gson
) {
    private val systemPrompt = """
        You are a meal suggestion engine. Return ONLY valid JSON, no markdown, no extra text.
        Schema: {"suggestions":[{"name":"","description":"","cal":0,"pro":0,"carb":0,"fat":0,"ingredients":[""]}]}
        Rules: respect restrictions and avoid list strictly. Favor love list. Output 3-5 suggestions.
    """.trimIndent()

    suspend fun getSuggestions(
        payload: SuggestionPayload,
        apiKey: String
    ): Result<List<MealSuggestion>> {
        return try {
            val userMsg = gson.toJson(payload)
            val request = AnthropicRequest(
                system = systemPrompt,
                messages = listOf(AnthropicMessage(role = "user", content = userMsg))
            )
            val response = anthropicApi.complete(apiKey = apiKey, request = request)
            val text = response.content.firstOrNull { it.type == "text" }?.text
                ?: return Result.failure(Exception("Empty response"))

            val clean = text.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val parsed = gson.fromJson(clean, AiSuggestionsWrapper::class.java)
            val suggestions = parsed.suggestions.map { s ->
                MealSuggestion(
                    name = s.name,
                    description = s.description,
                    estimatedCal = s.cal,
                    estimatedPro = s.pro,
                    estimatedCarb = s.carb,
                    estimatedFat = s.fat,
                    ingredients = s.ingredients
                )
            }
            Result.success(suggestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private data class AiSuggestionsWrapper(val suggestions: List<AiSuggestionRaw> = emptyList())
    private data class AiSuggestionRaw(
        val name: String = "",
        val description: String = "",
        val cal: Int = 0,
        val pro: Int = 0,
        val carb: Int = 0,
        val fat: Int = 0,
        val ingredients: List<String> = emptyList()
    )
}
