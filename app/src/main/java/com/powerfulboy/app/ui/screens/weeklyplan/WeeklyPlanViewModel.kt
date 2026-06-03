package com.powerfulboy.app.ui.screens.weeklyplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerfulboy.app.data.entity.RecipeEntity
import com.powerfulboy.app.data.entity.WeeklyPlanEntity
import com.powerfulboy.app.data.repository.RecipeRepository
import com.powerfulboy.app.data.repository.WeeklyPlanRepository
import com.powerfulboy.app.util.DateUtil
import com.powerfulboy.app.util.MealType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeeklyPlanState(
    val weekStart: String = DateUtil.currentWeekStart(),
    val plan: Map<Pair<Int, MealType>, WeeklyPlanEntity> = emptyMap(),
    val recipes: List<RecipeEntity> = emptyList()
)

@HiltViewModel
class WeeklyPlanViewModel @Inject constructor(
    private val weeklyPlanRepository: WeeklyPlanRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _weekStart = MutableStateFlow(DateUtil.currentWeekStart())

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<WeeklyPlanState> = combine(
        _weekStart,
        _weekStart.flatMapLatest { weeklyPlanRepository.getPlanForWeek(it) },
        recipeRepository.getAllRecipes()
    ) { weekStart, planList, recipes ->
        val planMap = planList.associate {
            Pair(it.dayIndex, MealType.fromString(it.mealType)) to it
        }
        WeeklyPlanState(weekStart = weekStart, plan = planMap, recipes = recipes)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeeklyPlanState())

    fun previousWeek() {
        val monday = java.time.LocalDate.parse(_weekStart.value)
        _weekStart.value = monday.minusWeeks(1).toString()
    }

    fun nextWeek() {
        val monday = java.time.LocalDate.parse(_weekStart.value)
        _weekStart.value = monday.plusWeeks(1).toString()
    }

    fun assignRecipe(dayIndex: Int, mealType: MealType, recipe: RecipeEntity?) {
        val weekStart = _weekStart.value
        viewModelScope.launch {
            if (recipe == null) {
                weeklyPlanRepository.clearSlot(weekStart, dayIndex, mealType.name)
            } else {
                weeklyPlanRepository.upsertSlot(
                    WeeklyPlanEntity(
                        weekStart = weekStart,
                        dayIndex = dayIndex,
                        mealType = mealType.name,
                        recipeId = recipe.id,
                        recipeName = recipe.name
                    )
                )
            }
        }
    }

    fun getShoppingList(onResult: (List<com.powerfulboy.app.data.dao.ShoppingItem>) -> Unit) {
        viewModelScope.launch {
            val list = weeklyPlanRepository.getShoppingList(_weekStart.value)
            onResult(list)
        }
    }

    val currentWeekStart get() = _weekStart.value
}
