package com.powerfulboy.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.powerfulboy.app.ui.screens.aisuggestions.AiSuggestionsScreen
import com.powerfulboy.app.ui.screens.dashboard.DashboardScreen
import com.powerfulboy.app.ui.screens.foodlog.FoodLogScreen
import com.powerfulboy.app.ui.screens.mealprep.MealPrepScreen
import com.powerfulboy.app.ui.screens.onboarding.OnboardingScreen
import com.powerfulboy.app.ui.screens.recipe.RecipeEditScreen
import com.powerfulboy.app.ui.screens.recipe.RecipesScreen
import com.powerfulboy.app.ui.screens.settings.SettingsScreen
import com.powerfulboy.app.ui.screens.weeklyplan.ShoppingListScreen
import com.powerfulboy.app.ui.screens.weeklyplan.WeeklyPlanScreen

@Composable
fun PowerfulBoyNavGraph(startDestination: String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Onboarding.route) {
            OnboardingScreen(onComplete = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onLogMeal = { date, mealType ->
                    navController.navigate(Screen.FoodLog.createRoute(date, mealType))
                },
                onNavigateWeeklyPlan = { navController.navigate(Screen.WeeklyPlan.route) },
                onNavigateRecipes = { navController.navigate(Screen.Recipes.route) },
                onNavigateAi = { navController.navigate(Screen.AiSuggestions.route) },
                onNavigateMealPrep = { navController.navigate(Screen.MealPrep.route) }
            )
        }

        composable(
            Screen.FoodLog.route,
            arguments = listOf(
                navArgument("date") { type = NavType.StringType },
                navArgument("mealType") { type = NavType.StringType }
            )
        ) {
            FoodLogScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Recipes.route) {
            RecipesScreen(
                onBack = { navController.popBackStack() },
                onEditRecipe = { id -> navController.navigate(Screen.RecipeEdit.createRoute(id)) },
                onNewRecipe = { navController.navigate(Screen.RecipeEdit.createRoute(Screen.RecipeEdit.NEW_ID)) }
            )
        }

        composable(
            Screen.RecipeEdit.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) {
            RecipeEditScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.WeeklyPlan.route) {
            WeeklyPlanScreen(
                onBack = { navController.popBackStack() },
                onShoppingList = { weekStart ->
                    navController.navigate(Screen.ShoppingList.createRoute(weekStart))
                }
            )
        }

        composable(
            Screen.ShoppingList.route,
            arguments = listOf(navArgument("weekStart") { type = NavType.StringType })
        ) { backStack ->
            val weekStart = backStack.arguments?.getString("weekStart") ?: ""
            ShoppingListScreen(
                weekStart = weekStart,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MealPrep.route) {
            MealPrepScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AiSuggestions.route) {
            AiSuggestionsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
