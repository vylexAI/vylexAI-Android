package com.vylexai.app.data.worker

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.workerDataStore by preferencesDataStore("vylex_worker")

/** Persistent telemetry + toggles shared between the background worker and the UI. */
@Singleton
class WorkerStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val state: Flow<WorkerSnapshot> = context.workerDataStore.data.map {
        WorkerSnapshot(
            enabled = it[KEY_ENABLED] ?: false,
            tasksCompletedToday = it[KEY_TASKS_TODAY] ?: 0,
            bsaiEarnedToday = it[KEY_BSAI_TODAY] ?: 0.0,
            lastStartedAt = it[KEY_LAST_STARTED] ?: 0L,
            lastHeartbeatAt = it[KEY_LAST_HEARTBEAT] ?: 0L,
            lastError = it[KEY_LAST_ERROR]
        )
    }

    suspend fun setEnabled(value: Boolean) {
        context.workerDataStore.edit {
            it[KEY_ENABLED] = value
            if (value) it[KEY_LAST_STARTED] = System.currentTimeMillis()
        }
    }

    suspend fun recordCompletedTask(rewardBsai: Double) {
        context.workerDataStore.edit {
            it[KEY_TASKS_TODAY] = (it[KEY_TASKS_TODAY] ?: 0) + 1
            it[KEY_BSAI_TODAY] = (it[KEY_BSAI_TODAY] ?: 0.0) + rewardBsai
            it[KEY_LAST_HEARTBEAT] = System.currentTimeMillis()
            it.remove(KEY_LAST_ERROR)
        }
    }

    suspend fun recordHeartbeat() {
        context.workerDataStore.edit {
            it[KEY_LAST_HEARTBEAT] = System.currentTimeMillis()
        }
    }

    suspend fun recordError(message: String) {
        context.workerDataStore.edit { it[KEY_LAST_ERROR] = message }
    }

    private companion object {
        val KEY_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("enabled")
        val KEY_TASKS_TODAY = intPreferencesKey("tasks_today")
        val KEY_BSAI_TODAY = doublePreferencesKey("bsai_today")
        val KEY_LAST_STARTED = longPreferencesKey("last_started")
        val KEY_LAST_HEARTBEAT = longPreferencesKey("last_heartbeat")
        val KEY_LAST_ERROR = stringPreferencesKey("last_error")
    }
}

data class WorkerSnapshot(
    val enabled: Boolean,
    val tasksCompletedToday: Int,
    val bsaiEarnedToday: Double,
    val lastStartedAt: Long,
    val lastHeartbeatAt: Long,
    val lastError: String?
)
