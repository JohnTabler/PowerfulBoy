package com.powerfulboy.app.ui.screens.recipe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerfulboy.app.data.dao.RecipeIngredientDetail
import com.powerfulboy.app.data.entity.FoodEntity
import com.powerfulboy.app.data.entity.RecipeEntity
import com.powerfulboy.app.data.entity.RecipeIngredientEntity
import com.powerfulboy.app.data.repository.FoodRepository
import com.powerfulboy.app.data.repository.RecipeRepository
import com.powerfulboy.app.ui.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── List VM ─────────────────────────────────────────────────────────────────
@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {
    val recipes = recipeRepository.getAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteRecipe(recipe: RecipeEntity) {
        viewModelScope.launch { recipeRepository.deleteRecipe(recipe) }
    }
}

// ─── Detail / Edit VM ─────────────────────────────────────────────────────────
data class RecipeEditState(
    val name: String = "",
    val instructions: String = "",
    val servings: String = "1",
    val ingredients: List<RecipeIngredientDetail> = emptyList(),
    val foodQuery: String = "",
    val foodResults: List<FoodEntity> = emptyList(),
    val isSearching: Boolean = false,
    val pendingFood: FoodEntity? = null,
    val pendingGrams: String = "100",
    val saved: Boolean = false,
    val isNew: Boolean = true
)

@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val foodRepository: FoodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val recipeId = savedStateHandle.get<Long>("recipeId") ?: Screen.RecipeEdit.NEW_ID

    private val _state = MutableStateFlow(RecipeEditState(isNew = recipeId == Screen.RecipeEdit.NEW_ID))
    val state = _state.asStateFlow()

    private var loadedRecipeId: Long? = null
    private var searchJob: Job? = null

    init {
        if (recipeId != Screen.RecipeEdit.NEW_ID) {
            viewModelScope.launch {
                val rwi = recipeRepository.getRecipeWithIngredients(recipeId)
                if (rwi != null) {
                    loadedRecipeId = rwi.recipe.id
                    _state.value = _state.value.copy(
                        name = rwi.recipe.name,
                        instructions = rwi.recipe.instructions,
                        servings = rwi.recipe.servings.toString(),
                        ingredients = rwi.ingredients,
                        isNew = false
                    )
                }
            }
        }
    }

    fun setName(v: String) { _state.value = _state.value.copy(name = v) }
    fun setInstructions(v: String) { _state.value = _state.value.copy(instructions = v) }
    fun setServings(v: String) { _state.value = _state.value.copy(servings = v) }
    fun setPendingGrams(v: String) { _state.value = _state.value.copy(pendingGrams = v) }

    fun searchFoods(q: String) {
        _state.value = _state.value.copy(foodQuery = q)
        searchJob?.cancel()
        if (q.isBlank()) { _state.value = _state.value.copy(foodResults = emptyList()); return }
        searchJob = viewModelScope.launch {
            delay(300)
            _state.value = _state.value.copy(isSearching = true)
            val local = foodRepository.searchLocalFoods(q).first()
            val remote = foodRepository.searchRemoteFoods(q)
            _state.value = _state.value.copy(
                foodResults = (local + remote).distinctBy { it.name.lowercase() },
                isSearching = false
            )
        }
    }

    fun selectFood(food: FoodEntity) {
        _state.value = _state.value.copy(pendingFood = food, pendingGrams = "100", foodQuery = "", foodResults = emptyList())
    }

    fun cancelPendingFood() {
        _state.value = _state.value.copy(pendingFood = null, pendingGrams = "100")
    }

    fun addPendingIngredient() {
        val food = _state.value.pendingFood ?: return
        val grams = _state.value.pendingGrams.toFloatOrNull() ?: return
        val detail = RecipeIngredientDetail(
            ingredientId = 0L,
            foodId = food.id,
            foodName = food.name,
            grams = grams,
            caloriesPer100g = food.calories,
            proteinPer100g = food.protein,
            carbsPer100g = food.carbs,
            fatPer100g = food.fat
        )
        _state.value = _state.value.copy(
            ingredients = _state.value.ingredients + detail,
            pendingFood = null,
            pendingGrams = "100"
        )
    }

    fun removeIngredient(index: Int) {
        _state.value = _state.value.copy(
            ingredients = _state.value.ingredients.toMutableList().also { it.removeAt(index) }
        )
    }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) return
        viewModelScope.launch {
            val recipe = RecipeEntity(
                id = loadedRecipeId ?: 0L,
                name = s.name,
                instructions = s.instructions,
                servings = s.servings.toIntOrNull() ?: 1
            )
            val savedId = if (s.isNew) recipeRepository.insertRecipe(recipe)
            else { recipeRepository.updateRecipe(recipe); loadedRecipeId!! }

            // Save ingredients - first save any unsaved foods
            val newIngredients = s.ingredients.map { detail ->
                val foodId = if (detail.foodId == 0L) {
                    foodRepository.saveFood(
                        FoodEntity(
                            name = detail.foodName,
                            calories = detail.caloriesPer100g,
                            protein = detail.proteinPer100g,
                            carbs = detail.carbsPer100g,
                            fat = detail.fatPer100g
                        )
                    )
                } else detail.foodId
                RecipeIngredientEntity(recipeId = savedId, foodId = foodId, grams = detail.grams)
            }
            recipeRepository.replaceIngredients(savedId, newIngredients)
            _state.value = _state.value.copy(saved = true)
        }
    }
}
