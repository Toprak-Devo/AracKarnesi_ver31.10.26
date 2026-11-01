package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.FuelPriceService
import com.example.data.VehicleCatalogService
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val db = AppDatabase.getDatabase(this)
        val fuelPriceService = FuelPriceService(applicationContext)
        val vehicleCatalogService = VehicleCatalogService(applicationContext)
        val repository = AppRepository(
            db.userDao(),
            db.vehicleDao(),
            db.recordDao(),
            db.reminderDao(),
            fuelPriceService,
            vehicleCatalogService
        )

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val alreadySeededV1 = prefs.getBoolean("seeded_toprak_baran_v1", false)
        val alreadySeededV2 = prefs.getBoolean("seeded_toprak_baran_v2", false)
        lifecycleScope.launch {
            repository.seedInitialProfileAndRecords(
                userId = "default_user",
                forceProfileUpdate = !alreadySeededV1,
                forceFuelTimelineUpgrade = !alreadySeededV2
            )
            if (!alreadySeededV1 || !alreadySeededV2) {
                prefs.edit()
                    .putBoolean("seeded_toprak_baran_v1", true)
                    .putBoolean("seeded_toprak_baran_v2", true)
                    .apply()
            }
        }
        
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                
                val authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory(repository))
                
                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashScreen(
                            onTimeout = {
                                navController.navigate("dashboard") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("dashboard") {
                        val dashboardViewModel: DashboardViewModel = viewModel(factory = ViewModelFactory(repository, "default_user"))
                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            onVehicleClick = { id -> navController.navigate("vehicle_detail/$id?tab=0") },
                            onVehicleClickWithTab = { id, tab -> navController.navigate("vehicle_detail/$id?tab=$tab") },
                            onQuickAddMaintenanceClick = { id -> navController.navigate("add_maintenance/$id") },
                            onAddVehicleClick = { navController.navigate("add_vehicle") },
                            onProfileClick = { navController.navigate("profile") },
                            onLogoutClick = { }
                        )
                    }
                    composable("profile") {
                        val profileViewModel: ProfileViewModel = viewModel(factory = ViewModelFactory(repository, "default_user"))
                        ProfileScreen(
                            viewModel = profileViewModel,
                            onBack = { navController.popBackStack() },
                            onVehicleClick = { id -> navController.navigate("vehicle_detail/$id?tab=0") },
                            onAddVehicleClick = { navController.navigate("add_vehicle") }
                        )
                    }
                    composable("add_vehicle") {
                        val dashboardViewModel: DashboardViewModel = viewModel(factory = ViewModelFactory(repository, "default_user"))
                        AddVehicleScreen(viewModel = dashboardViewModel, onBack = { navController.popBackStack() })
                    }
                    composable(
                        route = "vehicle_detail/{id}?tab={tab}",
                        arguments = listOf(
                            navArgument("id") { type = NavType.StringType },
                            navArgument("tab") {
                                type = NavType.IntType
                                defaultValue = 0
                            }
                        )
                    ) { backStackEntry ->
                        val idStr = backStackEntry.arguments?.getString("id") ?: "0"
                        val id = idStr.toIntOrNull() ?: 0
                        val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
                        val detailViewModel: VehicleDetailViewModel = viewModel(factory = ViewModelFactory(repository, "", id))
                        
                        VehicleDetailScreen(
                            viewModel = detailViewModel,
                            initialTab = initialTab,
                            onBack = { navController.popBackStack() },
                            onAddMaintenance = { navController.navigate("add_maintenance/$id") },
                            onAddReminder = { navController.navigate("add_reminder/$id") },
                            onAddExpense = { navController.navigate("add_expense/$id") }
                        )
                    }
                    composable("add_maintenance/{id}") { backStackEntry ->
                        val idStr = backStackEntry.arguments?.getString("id") ?: "0"
                        val id = idStr.toIntOrNull() ?: 0
                        val detailViewModel: VehicleDetailViewModel = viewModel(factory = ViewModelFactory(repository, "", id))
                        AddMaintenanceScreen(viewModel = detailViewModel, onBack = { navController.popBackStack() })
                    }
                    composable("add_reminder/{id}") { backStackEntry ->
                        val idStr = backStackEntry.arguments?.getString("id") ?: "0"
                        val id = idStr.toIntOrNull() ?: 0
                        val detailViewModel: VehicleDetailViewModel = viewModel(factory = ViewModelFactory(repository, "", id))
                        AddReminderScreen(viewModel = detailViewModel, onBack = { navController.popBackStack() })
                    }
                    composable("add_expense/{id}") { backStackEntry ->
                        val idStr = backStackEntry.arguments?.getString("id") ?: "0"
                        val id = idStr.toIntOrNull() ?: 0
                        val detailViewModel: VehicleDetailViewModel = viewModel(factory = ViewModelFactory(repository, "", id))
                        AddExpenseScreen(viewModel = detailViewModel, onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
