package com.vylexai.app.data.wallet

import com.vylexai.app.data.net.VylexApi
import com.vylexai.app.data.net.dto.UserStatsResponse
import com.vylexai.app.data.net.dto.WalletResponse
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface WalletRepository {
    suspend fun balance(): WalletResponse
    suspend fun stats(): UserStatsResponse
}

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val api: VylexApi
) : WalletRepository {

    override suspend fun balance(): WalletResponse = api.walletBalance()
    override suspend fun stats(): UserStatsResponse = api.userStats()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class WalletRepositoryModule {
    @Binds
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository
}
