package com.vylexai.app.data.net.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceProfileRequest(
    @SerialName("device_id") val deviceId: String,
    val model: String? = null,
    @SerialName("android_sdk") val androidSdk: Int? = null,
    val profile: Map<String, String> = emptyMap(),
    @SerialName("performance_score") val performanceScore: Int = 0
)

@Serializable
data class DeviceProfileResponse(
    val id: String,
    @SerialName("device_id") val deviceId: String,
    val model: String?,
    @SerialName("android_sdk") val androidSdk: Int?,
    @SerialName("performance_score") val performanceScore: Int,
    val profile: Map<String, String>,
    @SerialName("recommended_tasks") val recommendedTasks: List<String>,
    @SerialName("estimated_monthly_bsai") val estimatedMonthlyBsai: List<Double>
)
