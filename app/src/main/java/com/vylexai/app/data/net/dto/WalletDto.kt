package com.vylexai.app.data.net.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletResponse(
    @SerialName("balance_bsai") val balanceBsai: String,
    @SerialName("total_credited_bsai") val totalCreditedBsai: String,
    @SerialName("total_debited_bsai") val totalDebitedBsai: String
)

@Serializable
data class UserStatsResponse(
    @SerialName("tasks_completed") val tasksCompleted: Int,
    @SerialName("bsai_earned") val bsaiEarned: String,
    @SerialName("bsai_spent") val bsaiSpent: String,
    @SerialName("contribution_score") val contributionScore: Int
)
