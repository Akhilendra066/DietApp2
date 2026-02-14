package com.example.dietapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.dietapp.ui.screens.auth.LoginScreen
import com.example.dietapp.ui.screens.auth.SignupScreen
import com.example.dietapp.ui.screens.chat.AIChatScreen
import com.example.dietapp.ui.screens.home.HomeDashboardScreen
import com.example.dietapp.ui.screens.mealplan.GroceryListScreen
import com.example.dietapp.ui.screens.mealplan.MealDetailsScreen
import com.example.dietapp.ui.screens.mealplan.RecipeScreen
import com.example.dietapp.ui.screens.mealplan.WeeklyMealPlanScreen
import com.example.dietapp.ui.screens.onboarding.BudgetInputScreen
import com.example.dietapp.ui.screens.onboarding.DietPreferenceScreen
import com.example.dietapp.ui.screens.onboarding.GoalSelectionScreen
import com.example.dietapp.ui.screens.onboarding.ProfileSetupScreen
import com.example.dietapp.ui.screens.onboarding.RegionSelectionScreen
import com.example.dietapp.ui.screens.premium.ConsultationBookingScreen
import com.example.dietapp.ui.screens.premium.FamilyPlanScreen
import com.example.dietapp.ui.screens.premium.LabReportUploadScreen
import com.example.dietapp.ui.screens.profile.EditProfileScreen
import com.example.dietapp.ui.screens.profile.ProfileScreen
import com.example.dietapp.ui.screens.profile.SettingsScreen
import com.example.dietapp.ui.screens.profile.SubscriptionScreen
import com.example.dietapp.ui.screens.splash.SplashScreen
import com.example.dietapp.ui.screens.tracking.MealCompletionScreen
import com.example.dietapp.ui.screens.tracking.ProgressReportScreen
import com.example.dietapp.ui.screens.tracking.SymptomTrackerScreen
import com.example.dietapp.ui.screens.tracking.WaterTrackerScreen
import com.example.dietapp.ui.screens.tracking.WeightLogScreen

sealed class Route(val route: String) {
    data object Splash : Route("splash")
    data object Login : Route("login")
    data object Signup : Route("signup")

    data object ProfileSetup : Route("onboarding/profile_setup")
    data object GoalSelection : Route("onboarding/goal")
    data object DietPreference : Route("onboarding/diet_preference")
    data object BudgetInput : Route("onboarding/budget")
    data object RegionSelection : Route("onboarding/region")

    data object Home : Route("home")
    data object WeeklyMealPlan : Route("meals/weekly")
    data object MealDetails : Route("meals/details/{mealId}") {
        fun createRoute(mealId: String) = "meals/details/$mealId"
    }
    data object Recipe : Route("meals/recipe/{mealId}") {
        fun createRoute(mealId: String) = "meals/recipe/$mealId"
    }
    data object GroceryList : Route("meals/grocery")

    data object WeightLog : Route("tracking/weight")
    data object WaterTracker : Route("tracking/water")
    data object MealCompletion : Route("tracking/meal_completion")
    data object ProgressReport : Route("tracking/progress")
    data object SymptomTracker : Route("tracking/symptoms")

    data object AIChat : Route("chat")

    data object Profile : Route("profile")
    data object EditProfile : Route("profile/edit")
    data object Settings : Route("profile/settings")
    data object Subscription : Route("profile/subscription")

    data object ConsultationBooking : Route("premium/consultation")
    data object LabReportUpload : Route("premium/lab_report")
    data object FamilyPlan : Route("premium/family")
}

// Screens with bottom nav
val bottomNavRoutes = listOf(
    Route.Home.route,
    Route.WeeklyMealPlan.route,
    Route.WeightLog.route,
    Route.Profile.route
)

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Splash.route,
        modifier = modifier
    ) {
        // ── Splash ─────────────────────────────────
        composable(Route.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Route.Login.route) {
                        popUpTo(Route.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Auth ───────────────────────────────────
        composable(Route.Login.route) {
            LoginScreen(
                onNavigateToSignup = { navController.navigate(Route.Signup.route) },
                onNavigateToHome = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.Signup.route) {
            SignupScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToOnboarding = {
                 navController.navigate(Route.ProfileSetup.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Onboarding ────────────────────────────
        composable(Route.ProfileSetup.route) {
            ProfileSetupScreen(
                onNext = { navController.navigate(Route.GoalSelection.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Route.GoalSelection.route) {
            GoalSelectionScreen(
                onNext = { navController.navigate(Route.DietPreference.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Route.DietPreference.route) {
            DietPreferenceScreen(
                onNext = { navController.navigate(Route.BudgetInput.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Route.BudgetInput.route) {
            BudgetInputScreen(
                onNext = { navController.navigate(Route.RegionSelection.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Route.RegionSelection.route) {
            RegionSelectionScreen(
                onFinish = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.ProfileSetup.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Home ──────────────────────────────────
        composable(Route.Home.route) {
            HomeDashboardScreen(
                onNavigateToMealPlan = { navController.navigate(Route.WeeklyMealPlan.route) },
                onNavigateToWeightLog = { navController.navigate(Route.WeightLog.route) },
                onNavigateToWaterTracker = { navController.navigate(Route.WaterTracker.route) },
                onNavigateToChat = { navController.navigate(Route.AIChat.route) },
                onNavigateToProgress = { navController.navigate(Route.ProgressReport.route) }
            )
        }

        // ── Meal Plan ─────────────────────────────
        composable(Route.WeeklyMealPlan.route) {
            WeeklyMealPlanScreen(
                onNavigateToMealDetails = { mealId ->
                    navController.navigate(Route.MealDetails.createRoute(mealId))
                },
                onNavigateToGroceryList = { navController.navigate(Route.GroceryList.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Route.MealDetails.route,
            arguments = listOf(navArgument("mealId") { type = NavType.StringType })
        ) {
            MealDetailsScreen(
                onNavigateToRecipe = { mealId ->
                    navController.navigate(Route.Recipe.createRoute(mealId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Route.Recipe.route,
            arguments = listOf(navArgument("mealId") { type = NavType.StringType })
        ) {
            RecipeScreen(onBack = { navController.popBackStack() })
        }
        composable(Route.GroceryList.route) {
            GroceryListScreen(onBack = { navController.popBackStack() })
        }

        // ── Tracking ──────────────────────────────
        composable(Route.WeightLog.route) {
            WeightLogScreen(onBack = { navController.popBackStack() })
        }
        composable(Route.WaterTracker.route) {
            WaterTrackerScreen(onBack = { navController.popBackStack() })
        }
        composable(Route.MealCompletion.route) {
            MealCompletionScreen(onBack = { navController.popBackStack() })
        }
        composable(Route.ProgressReport.route) {
            ProgressReportScreen(onBack = { navController.popBackStack() })
        }
        composable(Route.SymptomTracker.route) {
            SymptomTrackerScreen(onBack = { navController.popBackStack() })
        }

        // ── Chat ──────────────────────────────────
        composable(Route.AIChat.route) {
            AIChatScreen(onBack = { navController.popBackStack() })
        }

        // ── Profile ───────────────────────────────
        composable(Route.Profile.route) {
            ProfileScreen(
                onNavigateToEdit = { navController.navigate(Route.EditProfile.route) },
                onNavigateToSettings = { navController.navigate(Route.Settings.route) },
                onNavigateToSubscription = { navController.navigate(Route.Subscription.route) },
                onNavigateToConsultation = { navController.navigate(Route.ConsultationBooking.route) }
            )
        }
        composable(Route.EditProfile.route) {
            EditProfileScreen(onBack = { navController.popBackStack() })
        }
        composable(Route.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Route.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.Subscription.route) {
            SubscriptionScreen(onBack = { navController.popBackStack() })
        }

        // ── Premium ───────────────────────────────
        composable(Route.ConsultationBooking.route) {
            ConsultationBookingScreen(onBack = { navController.popBackStack() })
        }
        composable(Route.LabReportUpload.route) {
            LabReportUploadScreen(onBack = { navController.popBackStack() })
        }
        composable(Route.FamilyPlan.route) {
            FamilyPlanScreen(onBack = { navController.popBackStack() })
        }
    }
}
