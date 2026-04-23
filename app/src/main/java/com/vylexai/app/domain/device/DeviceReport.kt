package com.vylexai.app.domain.device

/** A snapshot of what this phone looks like to the VylexAI network. */
data class DeviceReport(
    val model: String,
    val androidSdk: Int,
    val cpu: CpuInfo,
    val gpu: String?,
    val ramGb: Int,
    val freeGb: Int,
    val battery: BatteryInfo,
    val network: NetworkInfo,
    val nnapi: Boolean,
    val vulkan: Boolean,
    val performanceScore: Int,
    val estimateMonthlyBsai: Pair<Int, Int>,
    val recommendedTasks: List<String>
)

data class CpuInfo(
    val cores: Int,
    val model: String?,
    val maxFrequencyMhz: Int?
)

data class BatteryInfo(
    val percent: Int,
    val temperatureC: Int,
    val isCharging: Boolean
)

data class NetworkInfo(
    val type: NetworkType,
    val downlinkMbps: Int?
)

enum class NetworkType { WIFI, CELLULAR, OTHER, NONE }
