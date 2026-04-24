package com.vylexai.app.domain.inference

import android.graphics.Bitmap

/** Runtime-agnostic classifier. TFLite is the default impl; ONNX will plug in the same way. */
interface InferenceEngine {
    /** Classify a bitmap and return top-[k] labels with their confidences. */
    suspend fun classify(bitmap: Bitmap, k: Int = TOP_K): InferenceResult

    companion object {
        const val TOP_K = 3
    }
}

data class InferenceResult(
    val predictions: List<Prediction>,
    val latencyMs: Long
) {
    val top1: Prediction? get() = predictions.firstOrNull()
}

data class Prediction(
    val label: String,
    val confidence: Float
)
