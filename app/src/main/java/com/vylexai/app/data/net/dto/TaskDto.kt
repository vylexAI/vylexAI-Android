package com.vylexai.app.data.net.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskResponse(
    @SerialName("task_id") val taskId: String,
    @SerialName("job_id") val jobId: String,
    @SerialName("task_type") val taskType: String,
    @SerialName("model_ref") val modelRef: String,
    @SerialName("input_ref") val inputRef: String,
    val deadline: String? = null,
    @SerialName("reward_bsai") val rewardBsai: String
)

@Serializable
data class TaskResultRequest(
    @SerialName("task_id") val taskId: String,
    @SerialName("output_ref") val outputRef: String? = null,
    @SerialName("inline_output") val inlineOutput: Map<String, String>? = null,
    @SerialName("result_hash") val resultHash: String,
    @SerialName("exec_time_ms") val execTimeMs: Long,
    @SerialName("integrity_token") val integrityToken: String? = null
)

@Serializable
data class TaskResultAck(val status: String)
