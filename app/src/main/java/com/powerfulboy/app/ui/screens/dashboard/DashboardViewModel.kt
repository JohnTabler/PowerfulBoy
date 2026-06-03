package com.powerfulboy.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerfulboy.app.data.dao.DayMacros
import com.powerfulboy.app.data.entity.MealLogEntity
import com.powerfulboy.app.data.entity.SettingsEntity
import com.powerfulboy.app.data.repository.MealLogRepository
import com.powerfulboy.app.data.repository.SettingsRepository
import com.powerfulboy.app.util.DateUtil
import com.powerfulboy.app.util.MealType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val date: String = DateUtil.today(),
    val meals: Map<MealType, List<MealLogEntity>> = emptyMap(),
    val dayMacros: DayMacros = DayMacros(0f, 0f, 0f, 0f),
    val settings: SettingsEntity = SettingsEntity()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val mealLogRepository: MealLogRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _date = MutableStateFlow(DateUtil.today())

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<DashboardState> = combine(
        _date,
        _date.flatMapLatest { date -> mealLogRepository.getMealsForDate(date) },
        _date.flatMapLatest { date -> mealLogRepository.getDayMacros(date) },
        settingsRepository.getSettings()
    ) { date, meals, macros, settings ->
        val grouped = MealType.entries.associateWith { mt ->
            meals.filter { it.mealType == mt.name }
        }
        DashboardState(
            date = date,
            meals = grouped,
            dayMacros = macros,
            settings = settings ?: SettingsEntity()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

    fun previousDay() {
        val current = java.time.LocalDate.parse(_date.value)
        _date.value = current.minusDays(1).toString()
    }

    fun nextDay() {
        val current = java.time.LocalDate.parse(_date.value)
        _date.value = current.plusDays(1).toString()
    }

    fun deleteMealEntry(id: Long) {
        viewModelScope.launch { mealLogRepository.deleteMealLogById(id) }
    }
}
