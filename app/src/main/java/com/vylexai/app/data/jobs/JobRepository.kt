package com.vylexai.app.data.jobs

import com.vylexai.app.data.net.VylexApi
import com.vylexai.app.data.net.dto.JobCreateRequest
import com.vylexai.app.data.net.dto.JobResponse
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface JobRepository {
    suspend fun create(
        taskType: String,
        modelRef: String,
        inputRefs: List<String>,
        params: Map<String, String> = emptyMap(),
        replication: Int = 3
    ): JobResponse

    suspend fun list(): List<JobResponse>
}

@Singleton
class JobRepositoryImpl @Inject constructor(
    private val api: VylexApi
) : JobRepository {

    override suspend fun create(
        taskType: String,
        modelRef: String,
        inputRefs: List<String>,
        params: Map<String, String>,
        replication: Int
    ): JobResponse = api.createJob(
        JobCreateRequest(
            taskType = taskType,
            modelRef = modelRef,
            inputRefs = inputRefs,
            params = params,
            replication = replication
        )
    )

    override suspend fun list(): List<JobResponse> = api.listJobs()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class JobRepositoryModule {
    @Binds
    abstract fun bindJobRepository(impl: JobRepositoryImpl): JobRepository
}
