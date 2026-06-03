package com.powerfulboy.app.ui.screens.foodlog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerfulboy.app.data.entity.FoodEntity
import com.powerfulboy.app.ui.components.*
import com.powerfulboy.app.ui.theme.*
import com.powerfulboy.app.util.MacroCalc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLogScreen(
    onBack: () -> Unit,
    viewModel: FoodLogViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add to ${state.mealType.label}",
                        color = OnBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Lime)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Tabs
            TabRow(
                selectedTabIndex = state.tab,
                containerColor = Surface,
                contentColor = Lime
            ) {
                Tab(selected = state.tab == 0, onClick = { viewModel.setTab(0) }) {
                    Text("Search", modifier = Modifier.padding(vertical = 12.dp))
                }
                Tab(selected = state.tab == 1, onClick = { viewModel.setTab(1) }) {
                    Text("Manual Entry", modifier = Modifier.padding(vertical = 12.dp))
                }
            }

            when (state.tab) {
                0 -> SearchTab(state, viewModel)
                1 -> ManualTab(state, viewModel)
            }
        }
    }
}

@Composable
private fun SearchTab(state: FoodLogState, viewModel: FoodLogViewModel) {
    if (state.selectedFood != null) {
        ConfirmLogPanel(
            food = state.selectedFood,
            grams = state.grams,
            onGramsChange = { viewModel.setGrams(it) },
            onConfirm = { viewModel.logSelectedFood() },
            onCancel = { viewModel.clearSelection() }
        )
        return
    }

    Column(Modifier.padding(16.dp)) {
        PbTextField(
            value = state.query,
            onValueChange = { viewModel.setQuery(it) },
            label = "Search food...",
            modifier = Modifier.fillMaxWidth()
        )
        if (state.isSearching) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(color = Lime, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }

    val allResults = (state.localResults + state.remoteResults).distinctBy { it.name.lowercase() }

    if (allResults.isEmpty() && state.query.isNotBlank() && !state.isSearching) {
        EmptyState("No results for \"${state.query}\"")
    } else {
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
            if (state.localResults.isNotEmpty()) {
                item { SectionLabel("Saved") }
                items(state.localResults) { food ->
                    FoodResultRow(food) { viewModel.selectFood(food) }
                }
            }
            if (state.remoteResults.isNotEmpty()) {
                item { SectionLabel("From Database") }
                items(state.remoteResults) { food ->
                    FoodResultRow(food) { viewModel.selectFood(food) }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(label: String) {
    Text(
        label,
        fontSize = 12.sp,
        color = OnSurfaceMuted,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun FoodResultRow(food: FoodEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(food.name, color = OnBackground, fontWeight = FontWeight.Medium)
            food.brand?.let { Text(it, fontSize = 12.sp, color = OnSurfaceMuted) }
            Spacer(Modifier.height(4.dp))
            Text(
                "Per 100g: ${food.calories.toInt()} kcal · P:${food.protein.toInt()}g · C:${food.carbs.toInt()}g · F:${food.fat.toInt()}g",
                fontSize = 12.sp,
                color = OnSurfaceMuted
            )
        }
    }
}

@Composable
private fun ConfirmLogPanel(
    food: FoodEntity,
    grams: String,
    onGramsChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val gramsF = grams.toFloatOrNull() ?: 0f
    val macros = MacroCalc.calcFromPer100g(food.calories, food.protein, food.carbs, food.fat, gramsF)

    Column(Modifier.padding(16.dp)) {
        SectionCard {
            Text(food.name, fontWeight = FontWeight.Bold, color = OnBackground, fontSize = 16.sp)
            food.brand?.let { Text(it, fontSize = 13.sp, color = OnSurfaceMuted) }
            Spacer(Modifier.height(16.dp))

            PbTextField(
                value = grams,
                onValueChange = onGramsChange,
                label = "Grams",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(Modifier.height(16.dp))
            Text("Calculated macros:", fontSize = 13.sp, color = OnSurfaceMuted)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MacroChip("${macros.calories.toInt()}", "kcal", CalColor)
                MacroChip("${macros.protein.toInt()}g", "protein", ProteinColor)
                MacroChip("${macros.carbs.toInt()}g", "carbs", CarbColor)
                MacroChip("${macros.fat.toInt()}g", "fat", FatColor)
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedLimeButton("Cancel", onClick = onCancel, modifier = Modifier.weight(1f))
                LimeButton("Log It", onClick = onConfirm, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MacroChip(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 15.sp)
        Text(label, fontSize = 11.sp, color = OnSurfaceMuted)
    }
}

@Composable
private fun ManualTab(state: FoodLogState, viewModel: FoodLogViewModel) {
    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Enter macros per 100g. The full amount will be logged.",
            fontSize = 13.sp,
            color = OnSurfaceMuted
        )

        PbTextField(
            value = state.manualName,
            onValueChange = { viewModel.setManualName(it) },
            label = "Food name",
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PbTextField(
                value = state.manualCal,
                onValueChange = { viewModel.setManualCal(it) },
                label = "Calories",
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            PbTextField(
                value = state.manualProtein,
                onValueChange = { viewModel.setManualProtein(it) },
                label = "Protein (g)",
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PbTextField(
                value = state.manualCarbs,
                onValueChange = { viewModel.setManualCarbs(it) },
                label = "Carbs (g)",
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            PbTextField(
                value = state.manualFat,
                onValueChange = { viewModel.setManualFat(it) },
                label = "Fat (g)",
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        state.error?.let {
            Text(it, color = ErrorRed, fontSize = 13.sp)
        }

        LimeButton(
            text = "Log Food",
            onClick = { viewModel.logManualFood() },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        )
    }
}
