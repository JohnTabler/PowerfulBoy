package com.powerfulboy.app.ui

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object FoodLog : Screen("food_log/{date}/{mealType}") {
        fun createRoute(date: String, mealType: String) = "food_log/$date/$mealType"
    }
    object Recipes : Screen("recipes")
    object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun createRoute(id: Long) = "recipe_detail/$id"
    }
    object RecipeEdit : Screen("recipe_edit/{recipeId}") {
        fun createRoute(id: Long) = "recipe_edit/$id"
        const val NEW_ID = -1L
    }
    object WeeklyPlan : Screen("weekly_plan")
    object MealPrep : Screen("meal_prep")
    object AiSuggestions : Screen("ai_suggestions")
    object Settings : Screen("settings")
    object ShoppingList : Screen("shopping_list/{weekStart}") {
        fun createRoute(weekStart: String) = "shopping_list/$weekStart"
    }
}
