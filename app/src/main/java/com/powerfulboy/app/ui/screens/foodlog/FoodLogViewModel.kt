package com.powerfulboy.app.ui.screens.foodlog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerfulboy.app.data.entity.FoodEntity
import com.powerfulboy.app.data.entity.MealLogEntity
import com.powerfulboy.app.data.repository.FoodRepository
import com.powerfulboy.app.data.repository.MealLogRepository
import com.powerfulboy.app.util.MacroCalc
import com.powerfulboy.app.util.MealType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodLogState(
    val date: String = "",
    val mealType: MealType = MealType.BREAKFAST,
    val query: String = "",
    val localResults: List<FoodEntity> = emptyList(),
    val remoteResults: List<FoodEntity> = emptyList(),
    val isSearching: Boolean = false,
    val selectedFood: FoodEntity? = null,
    val grams: String = "100",
    val tab: Int = 0, // 0=search, 1=manual
    val manualName: String = "",
    val manualCal: String = "",
    val manualProtein: String = "",
    val manualCarbs: String = "",
    val manualFat: String = "",
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FoodLogViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val mealLogRepository: MealLogRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val date = savedStateHandle.get<String>("date") ?: ""
    private val mealTypeStr = savedStateHandle.get<String>("mealType") ?: "BREAKFAST"

    private val _state = MutableStateFlow(
        FoodLogState(
            date = date,
            mealType = MealType.fromString(mealTypeStr)
        )
    )
    val state = _state.asStateFlow()

    private var searchJob: Job? = null

    fun setTab(tab: Int) { _state.value = _state.value.copy(tab = tab, query = "", localResults = emptyList(), remoteResults = emptyList()) }
    fun setQuery(q: String) {
        _state.value = _state.value.copy(query = q)
        searchJob?.cancel()
        if (q.isBlank()) {
            _state.value = _state.value.copy(localResults = emptyList(), remoteResults = emptyList())
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            _state.value = _state.value.copy(isSearching = true)
            // local
            foodRepository.searchLocalFoods(q).first().let { local ->
                _state.value = _state.value.copy(localResults = local)
            }
            // remote
            val remote = foodRepository.searchRemoteFoods(q)
            _state.value = _state.value.copy(remoteResults = remote, isSearching = false)
        }
    }

    fun selectFood(food: FoodEntity) {
        _state.value = _state.value.copy(selectedFood = food, grams = "100")
    }

    fun clearSelection() {
        _state.value = _state.value.copy(selectedFood = null)
    }

    fun setGrams(g: String) { _state.value = _state.value.copy(grams = g) }
    fun setManualName(v: String) { _state.value = _state.value.copy(manualName = v) }
    fun setManualCal(v: String) { _state.value = _state.value.copy(manualCal = v) }
    fun setManualProtein(v: String) { _state.value = _state.value.copy(manualProtein = v) }
    fun setManualCarbs(v: String) { _state.value = _state.value.copy(manualCarbs = v) }
    fun setManualFat(v: String) { _state.value = _state.value.copy(manualFat = v) }

    fun logSelectedFood() {
        val s = _state.value
        val food = s.selectedFood ?: return
        val grams = s.grams.toFloatOrNull() ?: return
        viewModelScope.launch {
            // save food if it came from remote (no id yet)
            val foodId = if (food.id == 0L) foodRepository.saveFood(food) else food.id
            val macros = MacroCalc.calcFromPer100g(food.calories, food.protein, food.carbs, food.fat, grams)
            mealLogRepository.logMeal(
                MealLogEntity(
                    date = s.date,
                    mealType = s.mealType.name,
                    foodId = foodId,
                    foodName = food.name,
                    grams = grams,
                    calories = macros.calories,
                    protein = macros.protein,
                    carbs = macros.carbs,
                    fat = macros.fat
                )
            )
            _state.value = _state.value.copy(saved = true)
        }
    }

    fun logManualFood() {
        val s = _state.value
        if (s.manualName.isBlank()) {
            _state.value = _state.value.copy(error = "Name required")
            return
        }
        val cal = s.manualCal.toFloatOrNull() ?: 0f
        val pro = s.manualProtein.toFloatOrNull() ?: 0f
        val carbs = s.manualCarbs.toFloatOrNull() ?: 0f
        val fat = s.manualFat.toFloatOrNull() ?: 0f
        viewModelScope.launch {
            val food = FoodEntity(name = s.manualName, calories = cal, protein = pro, carbs = carbs, fat = fat)
            val savedId = foodRepository.saveFood(food)
            mealLogRepository.logMeal(
                MealLogEntity(
                    date = s.date,
                    mealType = s.mealType.name,
                    foodId = savedId,
                    foodName = s.manualName,
                    grams = 100f,
                    calories = cal,
                    protein = pro,
                    carbs = carbs,
                    fat = fat
                )
            )
            _state.value = _state.value.copy(saved = true)
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}
