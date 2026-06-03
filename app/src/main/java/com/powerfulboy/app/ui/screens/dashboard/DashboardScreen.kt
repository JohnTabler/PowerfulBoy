package com.powerfulboy.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerfulboy.app.data.entity.MealLogEntity
import com.powerfulboy.app.ui.components.*
import com.powerfulboy.app.ui.theme.*
import com.powerfulboy.app.util.DateUtil
import com.powerfulboy.app.util.MealType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogMeal: (date: String, mealType: String) -> Unit,
    onNavigateWeeklyPlan: () -> Unit,
    onNavigateRecipes: () -> Unit,
    onNavigateAi: () -> Unit,
    onNavigateMealPrep: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val settings = state.settings
    val macros = state.dayMacros

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Powerful Boy",
                        fontWeight = FontWeight.Black,
                        color = Lime,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Date navigator
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { viewModel.previousDay() }) {
                        Icon(Icons.Default.ChevronLeft, null, tint = Lime)
                    }
                    Text(
                        DateUtil.format(state.date),
                        fontWeight = FontWeight.SemiBold,
                        color = OnBackground,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = { viewModel.nextDay() }) {
                        Icon(Icons.Default.ChevronRight, null, tint = Lime)
                    }
                }
            }

            // Macro summary
            item {
                SectionCard {
                    Text("Today's Macros", fontWeight = FontWeight.Bold, color = OnBackground)
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MacroRing("Cal", macros.totalCalories, settings.targetCalories.toFloat(), CalColor)
                        MacroRing("Protein", macros.totalProtein, settings.targetProtein.toFloat(), ProteinColor)
                        MacroRing("Carbs", macros.totalCarbs, settings.targetCarbs.toFloat(), CarbColor)
                        MacroRing("Fat", macros.totalFat, settings.targetFat.toFloat(), FatColor)
                    }
                }
            }

            // Quick nav
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickNavButton("Plan", Icons.Default.CalendarMonth, Modifier.weight(1f)) { onNavigateWeeklyPlan() }
                    QuickNavButton("Prep", Icons.Default.CheckBox, Modifier.weight(1f)) { onNavigateMealPrep() }
                    QuickNavButton("Recipes", Icons.Default.MenuBook, Modifier.weight(1f)) { onNavigateRecipes() }
                    QuickNavButton("AI", Icons.Default.AutoAwesome, Modifier.weight(1f)) { onNavigateAi() }
                }
            }

            // Meal sections
            MealType.entries.forEach { mealType ->
                item {
                    MealSection(
                        mealType = mealType,
                        entries = state.meals[mealType] ?: emptyList(),
                        onAddClick = { onLogMeal(state.date, mealType.name) },
                        onDeleteEntry = { viewModel.deleteMealEntry(it) }
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun QuickNavButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            Modifier.padding(vertical = 12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = Lime, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, color = OnSurface)
        }
    }
}

@Composable
private fun MealSection(
    mealType: MealType,
    entries: List<MealLogEntity>,
    onAddClick: () -> Unit,
    onDeleteEntry: (Long) -> Unit
) {
    SectionCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(mealType.label, fontWeight = FontWeight.Bold, color = OnBackground)
            if (entries.isNotEmpty()) {
                val totalCal = entries.sumOf { it.calories.toDouble() }.toInt()
                Text("${totalCal} kcal", fontSize = 12.sp, color = CalColor)
            }
        }

        entries.forEach { entry ->
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(entry.foodName, color = OnSurface, fontSize = 14.sp)
                    Text(
                        "${entry.grams.toInt()}g · ${entry.calories.toInt()} kcal · P:${entry.protein.toInt()} C:${entry.carbs.toInt()} F:${entry.fat.toInt()}",
                        fontSize = 11.sp,
                        color = OnSurfaceMuted
                    )
                }
                IconButton(
                    onClick = { onDeleteEntry(entry.id) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Close, null, tint = OnSurfaceMuted, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onAddClick,
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(Icons.Default.Add, null, tint = Lime, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Add food", color = Lime, fontSize = 13.sp)
        }
    }
}
