package com.powerfulboy.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerfulboy.app.data.entity.SettingsEntity
import com.powerfulboy.app.data.repository.SettingsRepository
import com.powerfulboy.app.ui.components.*
import com.powerfulboy.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── State ────────────────────────────────────────────────────────────────────
data class SettingsState(
    val calories: String = "2000",
    val protein: String = "150",
    val carbs: String = "200",
    val fat: String = "65",
    val restrictions: String = "",
    val apiKey: String = "",
    val saved: Boolean = false
)

// ─── ViewModel ────────────────────────────────────────────────────────────────
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getSettings().filterNotNull().first().let { s ->
                _state.value = SettingsState(
                    calories = s.targetCalories.toString(),
                    protein = s.targetProtein.toString(),
                    carbs = s.targetCarbs.toString(),
                    fat = s.targetFat.toString(),
                    restrictions = s.dietaryRestrictions,
                    apiKey = s.anthropicApiKey
                )
            }
        }
    }

    fun update(f: SettingsState.() -> SettingsState) {
        _state.value = _state.value.f()
    }

    fun save() {
        viewModelScope.launch {
            val s = _state.value
            val existing = settingsRepository.getSettingsOnce()
            settingsRepository.saveSettings(
                SettingsEntity(
                    targetCalories = s.calories.toIntOrNull() ?: 2000,
                    targetProtein = s.protein.toIntOrNull() ?: 150,
                    targetCarbs = s.carbs.toIntOrNull() ?: 200,
                    targetFat = s.fat.toIntOrNull() ?: 65,
                    dietaryRestrictions = s.restrictions,
                    anthropicApiKey = s.apiKey.trim(),
                    onboardingDone = existing?.onboardingDone ?: true
                )
            )
            _state.value = _state.value.copy(saved = true)
        }
    }

    fun clearSaved() {
        _state.value = _state.value.copy(saved = false)
    }
}

// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showKey by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.clearSaved()
            onBack()
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = OnBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Lime)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.save() }) {
                        Icon(Icons.Default.Check, "Save", tint = Lime)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            SectionCard {
                Text("Daily Macro Targets", fontWeight = FontWeight.Bold, color = OnBackground)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PbTextField(
                        value = state.calories,
                        onValueChange = { viewModel.update { copy(calories = it) } },
                        label = "Calories",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    PbTextField(
                        value = state.protein,
                        onValueChange = { viewModel.update { copy(protein = it) } },
                        label = "Protein (g)",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PbTextField(
                        value = state.carbs,
                        onValueChange = { viewModel.update { copy(carbs = it) } },
                        label = "Carbs (g)",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    PbTextField(
                        value = state.fat,
                        onValueChange = { viewModel.update { copy(fat = it) } },
                        label = "Fat (g)",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            SectionCard {
                Text("Dietary Restrictions", fontWeight = FontWeight.Bold, color = OnBackground)
                Spacer(Modifier.height(8.dp))
                PbTextField(
                    value = state.restrictions,
                    onValueChange = { viewModel.update { copy(restrictions = it) } },
                    label = "Restrictions (comma-separated)",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
            }

            SectionCard {
                Text("Anthropic API Key", fontWeight = FontWeight.Bold, color = OnBackground)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Used only for AI meal suggestions. Stored locally, sent only to api.anthropic.com.",
                    fontSize = 12.sp,
                    color = OnSurfaceMuted
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.apiKey,
                    onValueChange = { viewModel.update { copy(apiKey = it) } },
                    label = { Text("sk-ant-...", color = OnSurfaceMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showKey) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { showKey = !showKey }) {
                            Text(if (showKey) "Hide" else "Show", color = Lime)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnBackground,
                        unfocusedTextColor = OnBackground,
                        focusedBorderColor = Lime,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = Lime,
                        focusedLabelColor = Lime
                    )
                )
            }

            Spacer(Modifier.height(8.dp))
            LimeButton(
                text = "Save Settings",
                onClick = { viewModel.save() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
