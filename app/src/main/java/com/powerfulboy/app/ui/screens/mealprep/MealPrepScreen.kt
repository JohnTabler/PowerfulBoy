package com.powerfulboy.app.ui.screens.mealprep

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerfulboy.app.data.repository.WeeklyPlanRepository
import com.powerfulboy.app.ui.components.EmptyState
import com.powerfulboy.app.ui.components.SectionCard
import com.powerfulboy.app.ui.theme.*
import com.powerfulboy.app.util.DateUtil
import com.powerfulboy.app.util.MealType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

// ─── Model ────────────────────────────────────────────────────────────────────
data class PrepItem(
    val dayIndex: Int,
    val dayLabel: String,
    val mealType: MealType,
    val recipeName: String,
    val key: String = "${dayIndex}_${mealType.name}"
)

// ─── ViewModel ────────────────────────────────────────────────────────────────
@HiltViewModel
class MealPrepViewModel @Inject constructor(
    private val weeklyPlanRepository: WeeklyPlanRepository
) : ViewModel() {

    private val weekStart = DateUtil.currentWeekStart()

    val prepItems: StateFlow<List<PrepItem>> = weeklyPlanRepository.getPlanForWeek(weekStart)
        .map { plans ->
            plans
                .filter { it.recipeName.isNotBlank() }
                .sortedWith(compareBy({ it.dayIndex }, { it.mealType }))
                .map {
                    PrepItem(
                        dayIndex = it.dayIndex,
                        dayLabel = DateUtil.fullDayLabel(it.dayIndex),
                        mealType = MealType.fromString(it.mealType),
                        recipeName = it.recipeName
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPrepScreen(
    onBack: () -> Unit,
    viewModel: MealPrepViewModel = hiltViewModel()
) {
    val items by viewModel.prepItems.collectAsState()
    var checked by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Meal Prep", color = OnBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Lime) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            EmptyState(
                "No meals planned this week.\nAdd recipes to your weekly plan.",
                Modifier.padding(padding)
            )
        } else {
            val grouped = items.groupBy { it.dayLabel }
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                item {
                    val doneCount = items.count { it.key in checked }
                    Column {
                        Text(
                            "$doneCount / ${items.size} prepped",
                            color = Lime,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { doneCount.toFloat() / items.size },
                            modifier = Modifier.fillMaxWidth(),
                            color = Lime,
                            trackColor = Lime.copy(alpha = 0.15f)
                        )
                    }
                }

                grouped.forEach { (day, dayItems) ->
                    item {
                        SectionCard {
                            Text(day, fontWeight = FontWeight.Bold, color = Lime, fontSize = 15.sp)
                            Spacer(Modifier.height(8.dp))
                            dayItems.forEach { item ->
                                PrepCheckRow(
                                    item = item,
                                    isChecked = item.key in checked,
                                    onToggle = {
                                        checked = if (item.key in checked)
                                            checked - item.key
                                        else
                                            checked + item.key
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrepCheckRow(item: PrepItem, isChecked: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = Lime,
                uncheckedColor = OnSurfaceMuted,
                checkmarkColor = Background
            )
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(item.mealType.label, fontSize = 11.sp, color = OnSurfaceMuted)
            Text(
                item.recipeName,
                color = if (isChecked) OnSurfaceMuted else OnBackground,
                fontSize = 14.sp
            )
        }
    }
}
