package com.localassistant.di

import android.content.Context
import com.localassistant.data.LlamaModelRepository
import com.localassistant.data.ModelDownloader
import com.localassistant.engine.InferenceEngine
import com.localassistant.inference.LlamaCppInferenceEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object InferenceModule {

    @Provides
    fun provideLlamaModelRepository(@ApplicationContext context: Context): LlamaModelRepository {
        return LlamaModelRepository(context, ModelDownloader())
    }

    @Provides
    fun provideInferenceEngine(llamaModelRepository: LlamaModelRepository): InferenceEngine {
        return LlamaCppInferenceEngine(llamaModelRepository.getModelPath())
    }
}
