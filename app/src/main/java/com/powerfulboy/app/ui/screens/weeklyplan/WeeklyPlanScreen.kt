package com.powerfulboy.app.ui.screens.weeklyplan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.powerfulboy.app.data.entity.RecipeEntity
import com.powerfulboy.app.ui.components.*
import com.powerfulboy.app.ui.theme.*
import com.powerfulboy.app.util.DateUtil
import com.powerfulboy.app.util.MealType
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyPlanScreen(
    onBack: () -> Unit,
    onShoppingList: (String) -> Unit,
    viewModel: WeeklyPlanViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var pickerTarget by remember { mutableStateOf<Pair<Int, MealType>?>(null) }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Weekly Plan", color = OnBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Lime) }
                },
                actions = {
                    IconButton(onClick = { onShoppingList(viewModel.currentWeekStart) }) {
                        Icon(Icons.Default.ShoppingCart, "Shopping list", tint = Lime)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Week navigator
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.previousWeek() }) {
                    Icon(Icons.Default.ChevronLeft, null, tint = Lime)
                }
                val monday = LocalDate.parse(state.weekStart)
                val sunday = monday.plusDays(6)
                Text(
                    "Week of ${monday.monthValue}/${monday.dayOfMonth} – ${sunday.dayOfMonth}",
                    color = OnBackground,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { viewModel.nextWeek() }) {
                    Icon(Icons.Default.ChevronRight, null, tint = Lime)
                }
            }

            LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(7) { dayIndex ->
                    DayCard(
                        dayLabel = DateUtil.fullDayLabel(dayIndex),
                        dayIndex = dayIndex,
                        plan = state.plan,
                        onSlotClick = { mi -> pickerTarget = Pair(dayIndex, mi) }
                    )
                }
            }
        }
    }

    pickerTarget?.let { (dayIndex, mealType) ->
        RecipePickerDialog(
            recipes = state.recipes,
            currentRecipeId = state.plan[Pair(dayIndex, mealType)]?.recipeId,
            onSelect = { recipe ->
                viewModel.assignRecipe(dayIndex, mealType, recipe)
                pickerTarget = null
            },
            onDismiss = { pickerTarget = null }
        )
    }
}

@Composable
private fun DayCard(
    dayLabel: String,
    dayIndex: Int,
    plan: Map<Pair<Int, MealType>, com.powerfulboy.app.data.entity.WeeklyPlanEntity>,
    onSlotClick: (MealType) -> Unit
) {
    SectionCard {
        Text(dayLabel, fontWeight = FontWeight.Bold, color = Lime, fontSize = 15.sp)
        Spacer(Modifier.height(8.dp))
        MealType.entries.forEach { mealType ->
            val slot = plan[Pair(dayIndex, mealType)]
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onSlotClick(mealType) }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    mealType.label,
                    fontSize = 13.sp,
                    color = OnSurfaceMuted,
                    modifier = Modifier.width(80.dp)
                )
                if (slot?.recipeName?.isNotBlank() == true) {
                    Text(slot.recipeName, fontSize = 14.sp, color = OnBackground, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Edit, null, tint = OnSurfaceMuted, modifier = Modifier.size(14.dp))
                } else {
                    Text("+ Add recipe", fontSize = 13.sp, color = Lime.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
                }
            }
            if (mealType != MealType.DINNER) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun RecipePickerDialog(
    recipes: List<RecipeEntity>,
    currentRecipeId: Long?,
    onSelect: (RecipeEntity?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        title = { Text("Pick a Recipe", color = OnBackground, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (currentRecipeId != null) {
                    TextButton(
                        onClick = { onSelect(null) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Clear slot", color = ErrorRed) }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
                if (recipes.isEmpty()) {
                    Text("No recipes yet. Create one in the Recipes screen.", color = OnSurfaceMuted, fontSize = 13.sp)
                }
                recipes.forEach { recipe ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(recipe) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(recipe.name, color = OnBackground, modifier = Modifier.weight(1f))
                        if (recipe.id == currentRecipeId) {
                            Icon(Icons.Default.Check, null, tint = Lime, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Lime) }
        }
    )
}
