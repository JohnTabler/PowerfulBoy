package com.powerfulboy.app.ui.screens.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.powerfulboy.app.data.dao.RecipeIngredientDetail
import com.powerfulboy.app.data.entity.FoodEntity
import com.powerfulboy.app.data.entity.RecipeEntity
import com.powerfulboy.app.ui.components.*
import com.powerfulboy.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    onBack: () -> Unit,
    onEditRecipe: (Long) -> Unit,
    onNewRecipe: () -> Unit,
    viewModel: RecipesViewModel = hiltViewModel()
) {
    val recipes by viewModel.recipes.collectAsState()
    var deleteTarget by remember { mutableStateOf<RecipeEntity?>(null) }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Recipes", color = OnBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Lime) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewRecipe,
                containerColor = Lime,
                contentColor = Background
            ) {
                Icon(Icons.Default.Add, "New recipe")
            }
        }
    ) { padding ->
        if (recipes.isEmpty()) {
            EmptyState("No recipes yet. Tap + to create one.", Modifier.padding(padding))
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                itemsIndexed(recipes) { _, recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onEditRecipe(recipe.id) },
                        onDelete = { deleteTarget = recipe }
                    )
                }
            }
        }
    }

    deleteTarget?.let { recipe ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete recipe?", color = OnBackground) },
            text = { Text("\"${recipe.name}\" will be permanently deleted.", color = OnSurface) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecipe(recipe)
                    deleteTarget = null
                }) { Text("Delete", color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel", color = Lime) }
            },
            containerColor = Surface
        )
    }
}

@Composable
private fun RecipeCard(recipe: RecipeEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(recipe.name, fontWeight = FontWeight.SemiBold, color = OnBackground)
                Text("${recipe.servings} serving${if (recipe.servings != 1) "s" else ""}", fontSize = 12.sp, color = OnSurfaceMuted)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = OnSurfaceMuted, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ─── Recipe Edit Screen ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    onBack: () -> Unit,
    viewModel: RecipeEditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.saved) { if (state.saved) onBack() }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isNew) "New Recipe" else "Edit Recipe",
                        color = OnBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Lime) }
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
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                SectionCard {
                    Text("Details", fontWeight = FontWeight.Bold, color = OnBackground)
                    Spacer(Modifier.height(12.dp))
                    PbTextField(
                        value = state.name,
                        onValueChange = { viewModel.setName(it) },
                        label = "Recipe name",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    PbTextField(
                        value = state.servings,
                        onValueChange = { viewModel.setServings(it) },
                        label = "Servings",
                        modifier = Modifier.fillMaxWidth(0.4f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(8.dp))
                    PbTextField(
                        value = state.instructions,
                        onValueChange = { viewModel.setInstructions(it) },
                        label = "Instructions (optional)",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false
                    )
                }
            }

            item {
                SectionCard {
                    Text("Ingredients", fontWeight = FontWeight.Bold, color = OnBackground)

                    val totalMacros = state.ingredients.fold(com.powerfulboy.app.util.Quad.ZERO) { acc, ing ->
                        acc + com.powerfulboy.app.util.MacroCalc.calcFromPer100g(
                            ing.caloriesPer100g, ing.proteinPer100g, ing.carbsPer100g, ing.fatPer100g, ing.grams
                        )
                    }
                    if (state.ingredients.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Total: ${totalMacros.calories.toInt()} kcal · P:${totalMacros.protein.toInt()}g · C:${totalMacros.carbs.toInt()}g · F:${totalMacros.fat.toInt()}g",
                            fontSize = 12.sp,
                            color = OnSurfaceMuted
                        )
                    }

                    state.ingredients.forEachIndexed { index, ing ->
                        Spacer(Modifier.height(8.dp))
                        IngredientRow(ing, onRemove = { viewModel.removeIngredient(index) })
                    }
                }
            }

            // Add ingredient section
            item {
                SectionCard {
                    Text("Add Ingredient", fontWeight = FontWeight.Bold, color = OnBackground)
                    Spacer(Modifier.height(8.dp))

                    if (state.pendingFood != null) {
                        Text(state.pendingFood.name, color = OnBackground, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        PbTextField(
                            value = state.pendingGrams,
                            onValueChange = { viewModel.setPendingGrams(it) },
                            label = "Grams",
                            modifier = Modifier.fillMaxWidth(0.5f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedLimeButton("Cancel", onClick = { viewModel.cancelPendingFood() }, modifier = Modifier.weight(1f))
                            LimeButton("Add", onClick = { viewModel.addPendingIngredient() }, modifier = Modifier.weight(1f))
                        }
                    } else {
                        PbTextField(
                            value = state.foodQuery,
                            onValueChange = { viewModel.searchFoods(it) },
                            label = "Search food...",
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (state.isSearching) {
                            Spacer(Modifier.height(8.dp))
                            CircularProgressIndicator(color = Lime, modifier = Modifier.size(24.dp))
                        }
                        state.foodResults.forEach { food ->
                            Spacer(Modifier.height(4.dp))
                            FoodChip(food) { viewModel.selectFood(food) }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun IngredientRow(ing: RecipeIngredientDetail, onRemove: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(ing.foodName, color = OnSurface, fontSize = 14.sp)
            val macros = com.powerfulboy.app.util.MacroCalc.calcFromPer100g(
                ing.caloriesPer100g, ing.proteinPer100g, ing.carbsPer100g, ing.fatPer100g, ing.grams
            )
            Text(
                "${ing.grams.toInt()}g · ${macros.calories.toInt()} kcal · P:${macros.protein.toInt()}g",
                fontSize = 12.sp, color = OnSurfaceMuted
            )
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, null, tint = OnSurfaceMuted, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun FoodChip(food: FoodEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(food.name, color = OnBackground, fontSize = 14.sp)
                Text("${food.calories.toInt()} kcal/100g", fontSize = 11.sp, color = OnSurfaceMuted)
            }
            Icon(Icons.Default.Add, null, tint = Lime, modifier = Modifier.size(18.dp))
        }
    }
}
