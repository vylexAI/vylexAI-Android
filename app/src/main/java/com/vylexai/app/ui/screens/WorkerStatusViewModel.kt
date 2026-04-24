package com.vylexai.app.ui.screens

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vylexai.app.data.inference.SampleGallery
import com.vylexai.app.domain.inference.InferenceEngine
import com.vylexai.app.domain.inference.InferenceResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkerStatusViewModel @Inject constructor(
    private val engine: InferenceEngine,
    private val gallery: SampleGallery
) : ViewModel() {

    private val _state = MutableStateFlow(WorkerUiState())
    val state: StateFlow<WorkerUiState> = _state.asStateFlow()

    private var loop: Job? = null

    init {
        start()
    }

    fun start() {
        if (loop?.isActive == true) return
        _state.update { it.copy(isRunning = true, error = null) }
        loop = viewModelScope.launch {
            var idx = 0
            val samples = gallery.samples
            while (true) {
                val sample = samples[idx % samples.size]
                idx += 1
                runCatching {
                    val bitmap = gallery.bitmapFor(sample)
                    val result = engine.classify(bitmap)
                    emit(sample.id, bitmap, result)
                }.onFailure { e ->
                    _state.update {
                        it.copy(isRunning = false, error = e.message ?: "inference_failed")
                    }
                    return@launch
                }
                delay(INTER_INFERENCE_DELAY_MS)
            }
        }
    }

    fun pause() {
        loop?.cancel()
        _state.update { it.copy(isRunning = false) }
    }

    private fun emit(sampleId: String, bitmap: Bitmap, result: InferenceResult) {
        _state.update { prev ->
            val history = (listOf(result.latencyMs.toInt()) + prev.latencyHistoryMs).take(
                HISTORY_SIZE
            )
            prev.copy(
                currentSampleId = sampleId,
                currentBitmap = bitmap,
                lastResult = result,
                latencyHistoryMs = history,
                completedTasks = prev.completedTasks + 1,
                rewardBsai = prev.rewardBsai + REWARD_PER_TASK
            )
        }
    }

    override fun onCleared() {
        loop?.cancel()
        super.onCleared()
    }

    private companion object {
        const val INTER_INFERENCE_DELAY_MS = 650L
        const val HISTORY_SIZE = 20
        const val REWARD_PER_TASK = 0.002
    }
}

data class WorkerUiState(
    val isRunning: Boolean = false,
    val currentSampleId: String? = null,
    val currentBitmap: Bitmap? = null,
    val lastResult: InferenceResult? = null,
    val latencyHistoryMs: List<Int> = emptyList(),
    val completedTasks: Int = 0,
    val rewardBsai: Double = 0.0,
    val error: String? = null
)
