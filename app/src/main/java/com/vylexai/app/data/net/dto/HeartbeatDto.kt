package com.vylexai.app.data.net.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HeartbeatRequest(
    @SerialName("device_id") val deviceId: String,
    @SerialName("battery_pct") val batteryPct: Int? = null,
    @SerialName("temp_c") val tempC: Double? = null,
    @SerialName("is_charging") val isCharging: Boolean? = null,
    @SerialName("network_type") val networkType: String? = null,
    @SerialName("integrity_token") val integrityToken: String? = null
)

@Serializable
data class HeartbeatResponse(
    val accepted: Boolean,
    @SerialName("integrity_ok") val integrityOk: Boolean,
    @SerialName("next_poll_ms") val nextPollMs: Long
)
