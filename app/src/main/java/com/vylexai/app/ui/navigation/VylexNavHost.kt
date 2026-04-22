package com.vylexai.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vylexai.app.ui.screens.ClientDashboardScreen
import com.vylexai.app.ui.screens.DeviceScanScreen
import com.vylexai.app.ui.screens.DeviceStateScreen
import com.vylexai.app.ui.screens.ModeSelectScreen
import com.vylexai.app.ui.screens.OnboardingScreen
import com.vylexai.app.ui.screens.ProviderDashboardScreen
import com.vylexai.app.ui.screens.SettingsScreen
import com.vylexai.app.ui.screens.SplashScreen
import com.vylexai.app.ui.screens.TaskCreateScreen
import com.vylexai.app.ui.screens.WalletScreen
import com.vylexai.app.ui.screens.WorkerStatusScreen

@Composable
fun VylexNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.Splash) {
        composable(Routes.Splash) {
            SplashScreen(onDone = {
                navController.navigate(Routes.Onboarding) {
                    popUpTo(Routes.Splash) { inclusive = true }
                }
            })
        }
        composable(Routes.Onboarding) {
            OnboardingScreen(onFinish = {
                navController.navigate(Routes.ModeSelect) {
                    popUpTo(Routes.Onboarding) { inclusive = true }
                }
            })
        }
        composable(Routes.ModeSelect) {
            ModeSelectScreen(onSelect = { mode ->
                val next = when (mode) {
                    Mode.Provider -> Routes.DeviceScan
                    Mode.Client -> Routes.ClientDashboard
                }
                navController.navigate(next)
            })
        }
        composable(Routes.DeviceScan) {
            DeviceScanScreen(onDone = {
                navController.navigate(Routes.ProviderDashboard) {
                    popUpTo(Routes.DeviceScan) { inclusive = true }
                }
            })
        }
        composable(Routes.ProviderDashboard) {
            ProviderDashboardScreen(
                onOpenWorker = { navController.navigate(Routes.WorkerStatus) },
                onOpenWallet = { navController.navigate(Routes.Wallet) },
                onOpenDevice = { navController.navigate(Routes.DeviceState) },
                onOpenSettings = { navController.navigate(Routes.Settings) }
            )
        }
        composable(Routes.ClientDashboard) {
            ClientDashboardScreen(
                onNewTask = { navController.navigate(Routes.TaskCreate) },
                onOpenWallet = { navController.navigate(Routes.Wallet) },
                onOpenSettings = { navController.navigate(Routes.Settings) }
            )
        }
        composable(Routes.TaskCreate) {
            TaskCreateScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.WorkerStatus) {
            WorkerStatusScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Wallet) {
            WalletScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Settings) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.DeviceState) {
            DeviceStateScreen(onBack = { navController.popBackStack() })
        }
    }
}
