package com.powerfulboy.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerfulboy.app.data.entity.SettingsEntity
import com.powerfulboy.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val calories: String = "2000",
    val protein: String = "150",
    val carbs: String = "200",
    val fat: String = "65",
    val restrictions: String = "no cheese, very minimal dairy, no beets",
    val apiKey: String = ""
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state.asStateFlow()

    fun updateCalories(v: String) { _state.value = _state.value.copy(calories = v) }
    fun updateProtein(v: String) { _state.value = _state.value.copy(protein = v) }
    fun updateCarbs(v: String) { _state.value = _state.value.copy(carbs = v) }
    fun updateFat(v: String) { _state.value = _state.value.copy(fat = v) }
    fun updateRestrictions(v: String) { _state.value = _state.value.copy(restrictions = v) }
    fun updateApiKey(v: String) { _state.value = _state.value.copy(apiKey = v) }

    fun complete(onDone: () -> Unit) {
        viewModelScope.launch {
            val s = _state.value
            settingsRepository.saveSettings(
                SettingsEntity(
                    targetCalories = s.calories.toIntOrNull() ?: 2000,
                    targetProtein = s.protein.toIntOrNull() ?: 150,
                    targetCarbs = s.carbs.toIntOrNull() ?: 200,
                    targetFat = s.fat.toIntOrNull() ?: 65,
                    dietaryRestrictions = s.restrictions,
                    anthropicApiKey = s.apiKey.trim(),
                    onboardingDone = true
                )
            )
            onDone()
        }
    }
}
