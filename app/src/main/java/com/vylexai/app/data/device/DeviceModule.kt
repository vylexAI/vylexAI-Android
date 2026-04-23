package com.vylexai.app.data.device

import com.vylexai.app.domain.device.DeviceScanner
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DeviceModule {
    @Binds
    abstract fun bindDeviceScanner(impl: AndroidDeviceScanner): DeviceScanner
}
