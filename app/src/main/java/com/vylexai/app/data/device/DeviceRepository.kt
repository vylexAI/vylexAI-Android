package com.vylexai.app.data.device

import com.vylexai.app.data.net.VylexApi
import com.vylexai.app.data.net.dto.DeviceProfileRequest
import com.vylexai.app.data.net.dto.DeviceProfileResponse
import com.vylexai.app.domain.device.DeviceReport
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface DeviceRepository {
    /** Sync the local scan into the coordinator and return the server-enriched profile. */
    suspend fun upsert(deviceId: String, report: DeviceReport): DeviceProfileResponse
    suspend fun list(): List<DeviceProfileResponse>
}

@Singleton
class DeviceRepositoryImpl @Inject constructor(
    private val api: VylexApi
) : DeviceRepository {

    override suspend fun upsert(deviceId: String, report: DeviceReport): DeviceProfileResponse =
        api.upsertDeviceProfile(
            DeviceProfileRequest(
                deviceId = deviceId,
                model = report.model,
                androidSdk = report.androidSdk,
                profile = mapOf(
                    "cpu" to (report.cpu.model ?: ""),
                    "cores" to report.cpu.cores.toString(),
                    "ramGb" to report.ramGb.toString(),
                    "nnapi" to report.nnapi.toString(),
                    "vulkan" to report.vulkan.toString()
                ),
                performanceScore = report.performanceScore
            )
        )

    override suspend fun list(): List<DeviceProfileResponse> = api.listDeviceProfiles()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DeviceRepositoryModule {
    @Binds
    abstract fun bindDeviceRepository(impl: DeviceRepositoryImpl): DeviceRepository
}
