package com.vylexai.app.data.inference

import com.vylexai.app.domain.inference.InferenceEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class InferenceModule {
    @Binds
    abstract fun bindInferenceEngine(impl: TfLiteInferenceEngine): InferenceEngine
}
