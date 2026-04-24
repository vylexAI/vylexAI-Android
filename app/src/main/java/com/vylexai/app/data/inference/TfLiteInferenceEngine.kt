package com.vylexai.app.data.inference

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import com.vylexai.app.domain.inference.InferenceEngine
import com.vylexai.app.domain.inference.InferenceResult
import com.vylexai.app.domain.inference.Prediction
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter

/**
 * MobileNetV1 (quantized, 224x224, 1001 ImageNet classes) running on TFLite.
 *
 * The interpreter is expensive to build, so we hold one instance guarded by a
 * mutex for thread safety. Inference itself runs on `Dispatchers.Default`
 * so the UI thread is never blocked.
 */
@Singleton
class TfLiteInferenceEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : InferenceEngine {

    private val mutex = Mutex()
    private var interpreter: Interpreter? = null
    private val labels: List<String> by lazy { loadLabels() }
    private val inputBuffer: ByteBuffer = ByteBuffer
        .allocateDirect(INPUT_SIZE * INPUT_SIZE * CHANNELS)
        .order(ByteOrder.nativeOrder())
    private val outputBuffer: Array<ByteArray> =
        Array(1) { ByteArray(LABEL_COUNT) }

    override suspend fun classify(bitmap: Bitmap, k: Int): InferenceResult = withContext(
        Dispatchers.Default
    ) {
        mutex.withLock {
            val interp = interpreter ?: loadInterpreter().also { interpreter = it }

            val scaled = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
            writeBitmapToInputBuffer(scaled)

            val started = SystemClock.elapsedRealtime()
            interp.run(inputBuffer, outputBuffer)
            val latencyMs = SystemClock.elapsedRealtime() - started

            val predictions = topK(outputBuffer[0], k)
            InferenceResult(predictions = predictions, latencyMs = latencyMs)
        }
    }

    private fun loadInterpreter(): Interpreter {
        val assetFd = context.assets.openFd(MODEL_ASSET)
        val modelBuffer = assetFd.createInputStream().channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            assetFd.startOffset,
            assetFd.declaredLength
        )
        val options = Interpreter.Options().apply {
            setNumThreads(INFER_THREADS)
        }
        return Interpreter(modelBuffer, options)
    }

    private fun loadLabels(): List<String> =
        context.assets.open(LABELS_ASSET).bufferedReader().useLines { it.toList() }

    private fun writeBitmapToInputBuffer(bmp: Bitmap) {
        inputBuffer.rewind()
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bmp.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        for (pixel in pixels) {
            inputBuffer.put(((pixel shr SHIFT_R) and BYTE_MASK).toByte())
            inputBuffer.put(((pixel shr SHIFT_G) and BYTE_MASK).toByte())
            inputBuffer.put((pixel and BYTE_MASK).toByte())
        }
    }

    private fun topK(raw: ByteArray, k: Int): List<Prediction> {
        // Quantized UINT8 output — divide by 255 for a pseudo-confidence.
        val indexed = raw.mapIndexed { i, b -> i to (b.toInt() and BYTE_MASK) }
        return indexed
            .sortedByDescending { it.second }
            .take(k)
            .map { (idx, score) ->
                val safeIdx = idx.coerceIn(0, labels.size - 1)
                Prediction(label = labels[safeIdx], confidence = score / BYTE_MAX_F)
            }
    }

    private companion object {
        const val MODEL_ASSET = "models/mobilenet_v1_1.0_224_quant.tflite"
        const val LABELS_ASSET = "models/imagenet_labels.txt"

        const val INPUT_SIZE = 224
        const val CHANNELS = 3
        const val LABEL_COUNT = 1001
        const val INFER_THREADS = 2

        const val BYTE_MASK = 0xFF
        const val BYTE_MAX_F = 255f
        const val SHIFT_R = 16
        const val SHIFT_G = 8
    }
}
