package com.vylexai.app.data.heartbeat

import com.vylexai.app.data.net.VylexApi
import com.vylexai.app.data.net.dto.HeartbeatRequest
import com.vylexai.app.data.net.dto.HeartbeatResponse
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface HeartbeatRepository {
    suspend fun send(
        deviceId: String,
        batteryPct: Int?,
        tempC: Double?,
        isCharging: Boolean?,
        networkType: String?,
        integrityToken: String?
    ): HeartbeatResponse
}

@Singleton
class HeartbeatRepositoryImpl @Inject constructor(
    private val api: VylexApi
) : HeartbeatRepository {

    override suspend fun send(
        deviceId: String,
        batteryPct: Int?,
        tempC: Double?,
        isCharging: Boolean?,
        networkType: String?,
        integrityToken: String?
    ): HeartbeatResponse = api.heartbeat(
        HeartbeatRequest(
            deviceId = deviceId,
            batteryPct = batteryPct,
            tempC = tempC,
            isCharging = isCharging,
            networkType = networkType,
            integrityToken = integrityToken
        )
    )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class HeartbeatRepositoryModule {
    @Binds
    abstract fun bindHeartbeatRepository(impl: HeartbeatRepositoryImpl): HeartbeatRepository
}
