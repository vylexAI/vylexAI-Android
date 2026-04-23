package com.vylexai.app.data.auth

import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response

/** Attaches `Authorization: Bearer <jwt>` if a token is on file. */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: AuthTokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.get()
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
