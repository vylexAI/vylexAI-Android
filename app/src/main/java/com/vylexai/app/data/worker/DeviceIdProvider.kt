package com.vylexai.app.data.worker

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.deviceIdDataStore by preferencesDataStore("vylex_device")
private val DEVICE_ID_KEY = stringPreferencesKey("device_id")

/** Stable, install-scoped device identifier. Regenerates on uninstall. */
@Singleton
class DeviceIdProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun get(): String {
        val existing = context.deviceIdDataStore.data
            .map { it[DEVICE_ID_KEY] }
            .firstOrNull()
        if (existing != null) return existing
        val fresh = UUID.randomUUID().toString()
        context.deviceIdDataStore.edit { it[DEVICE_ID_KEY] = fresh }
        return fresh
    }
}
