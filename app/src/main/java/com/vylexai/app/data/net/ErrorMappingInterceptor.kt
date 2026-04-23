package com.vylexai.app.data.net

import com.vylexai.app.data.auth.AuthTokenStore
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Translates coordinator HTTP status codes into [VylexException]s.
 *
 * Runs as an application-level OkHttp interceptor so Retrofit suspend calls
 * receive typed exceptions instead of opaque HttpException objects.
 */
@Singleton
class ErrorMappingInterceptor @Inject constructor(
    private val tokenStore: AuthTokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = try {
            chain.proceed(chain.request())
        } catch (e: IOException) {
            throw VylexException.Unavailable(e)
        }

        if (response.isSuccessful) return response

        val bodyPreview = try {
            response.peekBody(MAX_ERROR_BODY_BYTES).string()
        } catch (_: IOException) {
            ""
        }

        when {
            response.code == HTTP_UNAUTHORIZED -> {
                tokenStore.clear()
                response.close()
                throw VylexException.AuthExpired()
            }
            response.code == HTTP_PAYMENT_REQUIRED -> {
                response.close()
                throw VylexException.InsufficientFunds()
            }
            response.code in CLIENT_ERRORS -> {
                response.close()
                throw VylexException.BadRequest(bodyPreview.ifBlank { "bad_request" })
            }
            response.code in SERVER_ERRORS -> {
                response.close()
                throw VylexException.Unavailable()
            }
        }
        return response
    }

    private companion object {
        const val MAX_ERROR_BODY_BYTES = 4_096L

        // HTTP status codes (RFC 9110) given domain-meaningful names here so the
        // `when` branches read as intent, not magic numbers.
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_PAYMENT_REQUIRED = 402
        val CLIENT_ERRORS = setOf(400, 403, 404, 409, 422)
        val SERVER_ERRORS = 500..599
    }
}
