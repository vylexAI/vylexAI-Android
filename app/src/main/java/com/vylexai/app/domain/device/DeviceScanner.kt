package com.vylexai.app.domain.device

/** Produces a [DeviceReport] for this phone. Implementation is injected. */
interface DeviceScanner {
    suspend fun scan(): DeviceReport
}
