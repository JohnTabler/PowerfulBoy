package com.powerfulboy.app.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerfulboy.app.ui.components.*
import com.powerfulboy.app.ui.theme.*

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showKey by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        Text(
            "💪 Powerful Boy",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Lime
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Your personal meal planner\nand macro tracker",
            fontSize = 16.sp,
            color = OnSurfaceMuted,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        SectionCard {
            Text("Macro Targets", fontWeight = FontWeight.Bold, color = OnBackground, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PbTextField(
                    value = state.calories,
                    onValueChange = { viewModel.updateCalories(it) },
                    label = "Calories",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                PbTextField(
                    value = state.protein,
                    onValueChange = { viewModel.updateProtein(it) },
                    label = "Protein (g)",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PbTextField(
                    value = state.carbs,
                    onValueChange = { viewModel.updateCarbs(it) },
                    label = "Carbs (g)",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                PbTextField(
                    value = state.fat,
                    onValueChange = { viewModel.updateFat(it) },
                    label = "Fat (g)",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        SectionCard {
            Text("Dietary Restrictions", fontWeight = FontWeight.Bold, color = OnBackground, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text("Pre-filled with your preferences. Edit as needed.", fontSize = 12.sp, color = OnSurfaceMuted)
            Spacer(Modifier.height(12.dp))
            PbTextField(
                value = state.restrictions,
                onValueChange = { viewModel.updateRestrictions(it) },
                label = "Restrictions",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )
        }

        Spacer(Modifier.height(16.dp))

        SectionCard {
            Text("Anthropic API Key", fontWeight = FontWeight.Bold, color = OnBackground, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                "Required for AI meal suggestions. Stored only on your device. Get yours at console.anthropic.com",
                fontSize = 12.sp,
                color = OnSurfaceMuted
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.apiKey,
                onValueChange = { viewModel.updateApiKey(it) },
                label = { Text("sk-ant-...", color = OnSurfaceMuted) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showKey = !showKey }) {
                        Text(if (showKey) "Hide" else "Show", color = Lime, fontSize = 12.sp)
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

        Spacer(Modifier.height(32.dp))

        LimeButton(
            text = "Let's Go",
            onClick = { viewModel.complete(onComplete) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        )

        Spacer(Modifier.height(24.dp))
    }
}
