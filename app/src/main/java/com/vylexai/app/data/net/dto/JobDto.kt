package com.vylexai.app.data.net.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JobCreateRequest(
    @SerialName("task_type") val taskType: String,
    @SerialName("model_ref") val modelRef: String,
    @SerialName("input_refs") val inputRefs: List<String>,
    val params: Map<String, String> = emptyMap(),
    val replication: Int = 3
)

@Serializable
data class JobResponse(
    val id: String,
    @SerialName("task_type") val taskType: String,
    @SerialName("model_ref") val modelRef: String,
    val state: String,
    val replication: Int,
    @SerialName("reward_bsai") val rewardBsai: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("completed_at") val completedAt: String? = null
)
