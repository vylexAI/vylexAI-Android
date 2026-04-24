package com.vylexai.app.data.auth

import com.vylexai.app.data.net.VylexApi
import com.vylexai.app.data.net.dto.LoginRequest
import com.vylexai.app.data.net.dto.RegisterRequest
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    suspend fun register(email: String, password: String)
    suspend fun login(email: String, password: String)
    fun logout()
    fun isAuthenticated(): Boolean
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: VylexApi,
    private val tokenStore: AuthTokenStore
) : AuthRepository {

    override suspend fun register(email: String, password: String) {
        val response = api.register(RegisterRequest(email = email, password = password))
        tokenStore.save(response.accessToken)
    }

    override suspend fun login(email: String, password: String) {
        val response = api.login(LoginRequest(email = email, password = password))
        tokenStore.save(response.accessToken)
    }

    override fun logout() = tokenStore.clear()

    override fun isAuthenticated(): Boolean = tokenStore.isAuthenticated()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
