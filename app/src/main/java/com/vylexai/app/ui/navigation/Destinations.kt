package com.vylexai.app.ui.navigation

object Routes {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val Auth = "auth"
    const val ModeSelect = "mode_select"
    const val DeviceScan = "device_scan"
    const val ProviderDashboard = "provider_dashboard"
    const val ClientDashboard = "client_dashboard"
    const val TaskCreate = "task_create"
    const val WorkerStatus = "worker_status"
    const val Wallet = "wallet"
    const val Settings = "settings"
    const val DeviceState = "device_state"
}

enum class Mode { Provider, Client }
