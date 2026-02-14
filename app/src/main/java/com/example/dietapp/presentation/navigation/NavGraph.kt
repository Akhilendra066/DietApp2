package com.example.dietapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dietapp.presentation.dietlog.DietLogScreen
import com.example.dietapp.presentation.foodsearch.FoodSearchScreen
import com.example.dietapp.presentation.home.HomeScreen
import com.example.dietapp.presentation.mealplan.MealPlanScreen
import com.example.dietapp.presentation.profile.ProfileScreen

/**
 * Navigation route definitions for the app.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object MealPlan : Screen("meal_plan")
    data object FoodSearch : Screen("food_search")
    data object DietLog : Screen("diet_log")
    data object Profile : Screen("profile")
}

@Composable
fun DietNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToMealPlan = {
                    navController.navigate(Screen.MealPlan.route)
                },
                onNavigateToFoodSearch = {
                    navController.navigate(Screen.FoodSearch.route)
                },
                onNavigateToLog = {
                    navController.navigate(Screen.DietLog.route)
                }
            )
        }

        composable(Screen.MealPlan.route) {
            MealPlanScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.FoodSearch.route) {
            FoodSearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onFoodSelected = { /* TODO: navigate to food detail or log */ }
            )
        }

        composable(Screen.DietLog.route) {
            DietLogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
