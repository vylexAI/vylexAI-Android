package com.vylexai.app.data.tasks

import com.vylexai.app.data.net.VylexApi
import com.vylexai.app.data.net.dto.TaskResponse
import com.vylexai.app.data.net.dto.TaskResultRequest
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface TaskRepository {
    suspend fun nextTask(deviceId: String): TaskResponse?
    suspend fun submitResult(
        taskId: String,
        outputRef: String?,
        resultHash: String,
        execTimeMs: Long,
        integrityToken: String?
    )
}

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val api: VylexApi
) : TaskRepository {

    override suspend fun nextTask(deviceId: String): TaskResponse? = api.nextTask(deviceId)

    override suspend fun submitResult(
        taskId: String,
        outputRef: String?,
        resultHash: String,
        execTimeMs: Long,
        integrityToken: String?
    ) {
        api.submitTaskResult(
            TaskResultRequest(
                taskId = taskId,
                outputRef = outputRef,
                resultHash = resultHash,
                execTimeMs = execTimeMs,
                integrityToken = integrityToken
            )
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class TaskRepositoryModule {
    @Binds
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
}
