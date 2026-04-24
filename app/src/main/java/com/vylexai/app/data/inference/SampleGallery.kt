package com.vylexai.app.data.inference

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Bundled 320x320 ImageNet-friendly sample photos used by the demo worker loop. */
@Singleton
class SampleGallery @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val samples: List<Sample> = listOf(
        Sample("dog", "samples/dog.jpg"),
        Sample("cat", "samples/cat.jpg"),
        Sample("banana", "samples/banana.jpg"),
        Sample("pizza", "samples/pizza.jpg"),
        Sample("orange", "samples/orange.jpg")
    )

    fun bitmapFor(sample: Sample): Bitmap =
        context.assets.open(sample.asset).use { BitmapFactory.decodeStream(it) }

    data class Sample(val id: String, val asset: String)
}
