package com.vylexai.app.data.net

import com.vylexai.app.data.net.dto.DeviceProfileRequest
import com.vylexai.app.data.net.dto.DeviceProfileResponse
import com.vylexai.app.data.net.dto.HeartbeatRequest
import com.vylexai.app.data.net.dto.HeartbeatResponse
import com.vylexai.app.data.net.dto.JobCreateRequest
import com.vylexai.app.data.net.dto.JobResponse
import com.vylexai.app.data.net.dto.LoginRequest
import com.vylexai.app.data.net.dto.RegisterRequest
import com.vylexai.app.data.net.dto.TaskResponse
import com.vylexai.app.data.net.dto.TaskResultAck
import com.vylexai.app.data.net.dto.TaskResultRequest
import com.vylexai.app.data.net.dto.TokenResponse
import com.vylexai.app.data.net.dto.UserStatsResponse
import com.vylexai.app.data.net.dto.WalletResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * Mirror of the 10 coordinator endpoints.
 * Keep 1:1 with the FastAPI routers under `backend/app/api/`.
 */
interface VylexApi {
    // auth
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): TokenResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    // device profile
    @PUT("device/profile")
    suspend fun upsertDeviceProfile(@Body body: DeviceProfileRequest): DeviceProfileResponse

    @GET("device/profile")
    suspend fun listDeviceProfiles(): List<DeviceProfileResponse>

    // task dispatch (provider side)
    @GET("tasks/next")
    suspend fun nextTask(@Query("device_id") deviceId: String): TaskResponse?

    @POST("tasks/result")
    suspend fun submitTaskResult(@Body body: TaskResultRequest): TaskResultAck

    // job submission (client side)
    @POST("jobs")
    suspend fun createJob(@Body body: JobCreateRequest): JobResponse

    @GET("jobs/status")
    suspend fun listJobs(): List<JobResponse>

    // user + wallet
    @GET("user/stats")
    suspend fun userStats(): UserStatsResponse

    @GET("wallet/balance")
    suspend fun walletBalance(): WalletResponse

    // heartbeat
    @POST("worker/heartbeat")
    suspend fun heartbeat(@Body body: HeartbeatRequest): HeartbeatResponse
}
