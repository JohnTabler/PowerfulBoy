package com.powerfulboy.app.ui.screens.weeklyplan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.powerfulboy.app.data.dao.ShoppingItem
import com.powerfulboy.app.ui.components.EmptyState
import com.powerfulboy.app.ui.components.SectionCard
import com.powerfulboy.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    weekStart: String,
    onBack: () -> Unit,
    viewModel: WeeklyPlanViewModel = hiltViewModel()
) {
    var items by remember { mutableStateOf<List<ShoppingItem>>(emptyList()) }
    var checked by remember { mutableStateOf(setOf<Int>()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(weekStart) {
        viewModel.getShoppingList { result ->
            items = result
            loading = false
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Shopping List", color = OnBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Lime)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        when {
            loading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Lime)
            }

            items.isEmpty() -> EmptyState(
                "No ingredients found. Add recipes to your weekly plan.",
                Modifier.padding(padding)
            )

            else -> LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                item {
                    SectionCard {
                        Text(
                            "${items.size} item${if (items.size != 1) "s" else ""}",
                            fontSize = 13.sp,
                            color = OnSurfaceMuted
                        )
                        Spacer(Modifier.height(8.dp))
                        items.forEachIndexed { index, item ->
                            ShoppingRow(
                                item = item,
                                isChecked = index in checked,
                                onToggle = {
                                    checked = if (index in checked) checked - index else checked + index
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingRow(item: ShoppingItem, isChecked: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
        Text(
            item.foodName,
            modifier = Modifier.weight(1f),
            color = if (isChecked) OnSurfaceMuted else OnBackground,
            fontSize = 15.sp
        )
        Text(
            "${item.totalGrams.toInt()}g",
            color = OnSurfaceMuted,
            fontSize = 13.sp
        )
    }
}
