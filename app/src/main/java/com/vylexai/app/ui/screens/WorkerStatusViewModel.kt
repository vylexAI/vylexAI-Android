package com.vylexai.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vylexai.app.data.worker.WorkerController
import com.vylexai.app.data.worker.WorkerStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Surfaces the live state of the real [com.vylexai.app.data.worker.VylexProviderWorker]
 * (cumulative, persisted via [WorkerStore]). The screen no longer runs its own inference
 * loop — what testers see here is the same source of truth as the Provider dashboard.
 */
@HiltViewModel
class WorkerStatusViewModel @Inject constructor(
    store: WorkerStore,
    private val controller: WorkerController
) : ViewModel() {

    val state: StateFlow<WorkerUiState> = store.state
        .map { s ->
            WorkerUiState(
                isRunning = s.enabled,
                latencyHistoryMs = s.latencyHistoryMs,
                lastLatencyMs = s.lastLatencyMs,
                completedTasks = s.tasksCompletedToday,
                rewardBsai = s.bsaiEarnedToday,
                error = s.lastError
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT_MS),
            initialValue = WorkerUiState()
        )

    fun start() {
        viewModelScope.launch { controller.start() }
    }

    fun pause() {
        viewModelScope.launch { controller.stop() }
    }

    private companion object {
        const val SUBSCRIBE_TIMEOUT_MS = 5_000L
    }
}

data class WorkerUiState(
    val isRunning: Boolean = false,
    val latencyHistoryMs: List<Int> = emptyList(),
    val lastLatencyMs: Int = 0,
    val completedTasks: Int = 0,
    val rewardBsai: Double = 0.0,
    val error: String? = null
)
