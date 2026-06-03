package com.powerfulboy.app.ui.screens.aisuggestions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.powerfulboy.app.network.model.MealSuggestion
import com.powerfulboy.app.ui.components.*
import com.powerfulboy.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSuggestionsScreen(
    onBack: () -> Unit,
    viewModel: AiViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showPantry by remember { mutableStateOf(false) }

    LaunchedEffect(state.savedIndex) {
        if (state.savedIndex != null) viewModel.clearSaved()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text("AI Meal Ideas", color = OnBackground, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Lime) }
                },
                actions = {
                    IconButton(onClick = { showPantry = !showPantry }) {
                        Icon(
                            if (showPantry) Icons.Default.Kitchen else Icons.Default.Kitchen,
                            "Pantry",
                            tint = if (showPantry) Lime else OnSurfaceMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (showPantry) {
                item {
                    SectionCard {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("My Ingredients", fontWeight = FontWeight.Bold, color = OnBackground)
                            Text("❤️ Love   🚫 Avoid", fontSize = 11.sp, color = OnSurfaceMuted)
                        }
                        Spacer(Modifier.height(8.dp))
                        if (state.pantryItems.isEmpty()) {
                            Text(
                                "No saved foods yet. Add foods via the dashboard to mark preferences.",
                                fontSize = 13.sp,
                                color = OnSurfaceMuted
                            )
                        }
                        state.pantryItems.forEach { item ->
                            PantryRow(
                                name = item.food.name,
                                preference = item.preference,
                                onSetLove = { viewModel.setPreference(item.food.id, if (item.preference == "LOVE") "NEUTRAL" else "LOVE") },
                                onSetAvoid = { viewModel.setPreference(item.food.id, if (item.preference == "AVOID") "NEUTRAL" else "AVOID") }
                            )
                        }
                    }
                }
            }

            item {
                SectionCard {
                    Text(
                        "Claude will suggest meals based on your macro targets, remaining budget, dietary restrictions, and ingredient preferences.",
                        fontSize = 13.sp,
                        color = OnSurfaceMuted
                    )
                    Spacer(Modifier.height(12.dp))
                    LimeButton(
                        text = if (state.isLoading) "Generating..." else "✨ Get Suggestions",
                        onClick = { viewModel.getSuggestions() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !state.isLoading
                    )
                    if (state.isLoading) {
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Lime,
                            trackColor = Lime.copy(alpha = 0.15f)
                        )
                    }
                }
            }

            state.error?.let { error ->
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f))) {
                        Text(error, color = ErrorRed, modifier = Modifier.padding(16.dp), fontSize = 13.sp)
                    }
                }
            }

            itemsIndexed(state.suggestions) { index, suggestion ->
                SuggestionCard(
                    suggestion = suggestion,
                    isSaved = state.savedIndex == index,
                    onSave = { viewModel.saveAsRecipe(suggestion, index) }
                )
            }
        }
    }
}

@Composable
private fun PantryRow(
    name: String,
    preference: String,
    onSetLove: () -> Unit,
    onSetAvoid: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, modifier = Modifier.weight(1f), color = OnSurface, fontSize = 14.sp)
        IconButton(
            onClick = onSetLove,
            modifier = Modifier.size(36.dp)
        ) {
            Text(if (preference == "LOVE") "❤️" else "🤍", fontSize = 16.sp)
        }
        IconButton(
            onClick = onSetAvoid,
            modifier = Modifier.size(36.dp)
        ) {
            Text(if (preference == "AVOID") "🚫" else "⭕", fontSize = 16.sp)
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestion: MealSuggestion,
    isSaved: Boolean,
    onSave: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    suggestion.name,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                if (isSaved) {
                    Icon(Icons.Default.Check, null, tint = Lime, modifier = Modifier.size(18.dp))
                } else {
                    TextButton(
                        onClick = onSave,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Save recipe", color = Lime, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(suggestion.description, color = OnSurface, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MacroTag("${suggestion.estimatedCal}", "kcal", CalColor)
                MacroTag("${suggestion.estimatedPro}g", "protein", ProteinColor)
                MacroTag("${suggestion.estimatedCarb}g", "carbs", CarbColor)
                MacroTag("${suggestion.estimatedFat}g", "fat", FatColor)
            }

            if (suggestion.ingredients.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text("Ingredients:", fontSize = 12.sp, color = OnSurfaceMuted)
                Spacer(Modifier.height(4.dp))
                Text(
                    suggestion.ingredients.joinToString(" · "),
                    fontSize = 12.sp,
                    color = OnSurface
                )
            }
        }
    }
}

@Composable
private fun MacroTag(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
        Text(label, fontSize = 10.sp, color = OnSurfaceMuted)
    }
}
