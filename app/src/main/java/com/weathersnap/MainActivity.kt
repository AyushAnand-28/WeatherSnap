package com.weathersnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.weathersnap.ui.camera.CameraScreen
import com.weathersnap.ui.report.CreateReportScreen
import com.weathersnap.ui.report.CreateReportViewModel
import com.weathersnap.ui.reports.SavedReportsScreen
import com.weathersnap.ui.theme.WeatherSnapTheme
import com.weathersnap.ui.weather.WeatherScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherSnapTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "weather") {
                    composable("weather") {
                        WeatherScreen(
                            onNavigateToReports = { navController.navigate("saved_reports") },
                            onNavigateToCreateReport = { cityName, weatherJson ->
                                navController.navigate("create_report/$cityName/$weatherJson")
                            }
                        )
                    }
                    composable(
                        route = "create_report/{cityName}/{weatherJson}",
                        arguments = listOf(
                            navArgument("cityName") { type = NavType.StringType },
                            navArgument("weatherJson") { type = NavType.StringType }
                        )
                    ) { entry ->
                        // Pass same ViewModel instance to Camera if needed, or use savedStateHandle
                        val viewModel: CreateReportViewModel = hiltViewModel(entry)
                        
                        CreateReportScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToCamera = { navController.navigate("camera") },
                            onNavigateToReports = {
                                navController.navigate("saved_reports") {
                                    popUpTo("weather")
                                }
                            }
                        )
                    }
                    composable("camera") {
                        // Needs to retrieve the parent viewmodel or return result
                        // Since NavBackStackEntry is different, we get the previous back stack entry
                        val parentEntry = remember(it) {
                            navController.getBackStackEntry("create_report/{cityName}/{weatherJson}")
                        }
                        val parentViewModel: CreateReportViewModel = hiltViewModel(parentEntry)
                        
                        CameraScreen(
                            onPhotoCaptured = { path ->
                                parentViewModel.onImageCaptured(path)
                                navController.popBackStack()
                            },
                            onClose = { navController.popBackStack() }
                        )
                    }
                    composable("saved_reports") {
                        SavedReportsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
