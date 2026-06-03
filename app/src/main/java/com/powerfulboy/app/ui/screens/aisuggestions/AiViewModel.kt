package com.powerfulboy.app.ui.screens.aisuggestions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerfulboy.app.data.entity.FoodEntity
import com.powerfulboy.app.data.entity.PantryEntity
import com.powerfulboy.app.data.entity.RecipeEntity
import com.powerfulboy.app.data.entity.RecipeIngredientEntity
import com.powerfulboy.app.data.repository.*
import com.powerfulboy.app.network.model.*
import com.powerfulboy.app.util.DateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiState(
    val suggestions: List<MealSuggestion> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val pantryItems: List<PantryFoodItem> = emptyList(),
    val settings: com.powerfulboy.app.data.entity.SettingsEntity = com.powerfulboy.app.data.entity.SettingsEntity(),
    val savedIndex: Int? = null
)

data class PantryFoodItem(
    val food: FoodEntity,
    val preference: String // LOVE | AVOID | NEUTRAL
)

@HiltViewModel
class AiViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val foodRepository: FoodRepository,
    private val recipeRepository: RecipeRepository,
    private val mealLogRepository: MealLogRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Load pantry items (all foods + their preferences)
            combine(
                foodRepository.getAllFoods(),
                foodRepository.getAllPantry()
            ) { foods, pantry ->
                val prefMap = pantry.associate { it.foodId to it.preference }
                foods.map { food ->
                    PantryFoodItem(food = food, preference = prefMap[food.id] ?: "NEUTRAL")
                }
            }.collect { items ->
                _state.value = _state.value.copy(pantryItems = items)
            }
        }
        viewModelScope.launch {
            settingsRepository.getSettings().filterNotNull().collect { settings ->
                _state.value = _state.value.copy(settings = settings)
            }
        }
    }

    fun setPreference(foodId: Long, preference: String) {
        viewModelScope.launch {
            foodRepository.setPantryPreference(foodId, preference)
        }
    }

    fun getSuggestions() {
        val settings = _state.value.settings
        if (settings.anthropicApiKey.isBlank()) {
            _state.value = _state.value.copy(error = "No API key set. Go to Settings to add your Anthropic API key.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, suggestions = emptyList())

            // Get today's remaining macros
            val dayMacros = mealLogRepository.getDayMacros(DateUtil.today()).first()
            val remainingCal = (settings.targetCalories - dayMacros.totalCalories).toInt().coerceAtLeast(0)
            val remainingPro = (settings.targetProtein - dayMacros.totalProtein).toInt().coerceAtLeast(0)
            val remainingCarb = (settings.targetCarbs - dayMacros.totalCarbs).toInt().coerceAtLeast(0)
            val remainingFat = (settings.targetFat - dayMacros.totalFat).toInt().coerceAtLeast(0)

            val avoided = foodRepository.getAvoidedIngredientNames()
            val loved = foodRepository.getLovedIngredientNames()
            val restrictions = settings.dietaryRestrictions
                .split(",").map { it.trim() }.filter { it.isNotBlank() }

            val payload = SuggestionPayload(
                targets = MacroTargets(settings.targetCalories, settings.targetProtein, settings.targetCarbs, settings.targetFat),
                remaining = MacroTargets(remainingCal, remainingPro, remainingCarb, remainingFat),
                restrictions = restrictions,
                avoid = avoided,
                love = loved
            )

            val result = aiRepository.getSuggestions(payload, settings.anthropicApiKey)
            result.fold(
                onSuccess = { suggestions ->
                    _state.value = _state.value.copy(suggestions = suggestions, isLoading = false)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        error = "Error: ${e.message ?: "Unknown error"}",
                        isLoading = false
                    )
                }
            )
        }
    }

    fun saveAsRecipe(suggestion: MealSuggestion, index: Int) {
        viewModelScope.launch {
            val recipe = RecipeEntity(name = suggestion.name, instructions = suggestion.description, servings = 1)
            val recipeId = recipeRepository.insertRecipe(recipe)

            // Save each ingredient as a generic food with estimated macros
            suggestion.ingredients.forEachIndexed { i, ingredientName ->
                val food = FoodEntity(
                    name = ingredientName,
                    calories = if (i == 0) suggestion.estimatedCal.toFloat() else 0f,
                    protein = if (i == 0) suggestion.estimatedPro.toFloat() else 0f,
                    carbs = if (i == 0) suggestion.estimatedCarb.toFloat() else 0f,
                    fat = if (i == 0) suggestion.estimatedFat.toFloat() else 0f,
                    source = "ai"
                )
                val foodId = foodRepository.saveFood(food)
                recipeRepository.addIngredient(
                    RecipeIngredientEntity(recipeId = recipeId, foodId = foodId, grams = 100f)
                )
            }
            _state.value = _state.value.copy(savedIndex = index)
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
    fun clearSaved() { _state.value = _state.value.copy(savedIndex = null) }
}
