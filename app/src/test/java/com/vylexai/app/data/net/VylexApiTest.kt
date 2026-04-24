package com.vylexai.app.data.net

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.vylexai.app.data.auth.AuthInterceptor
import com.vylexai.app.data.auth.AuthTokenStore
import com.vylexai.app.data.net.dto.LoginRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class VylexApiTest {
    private val server = MockWebServer()
    private lateinit var tokenStore: AuthTokenStore
    private lateinit var api: VylexApi

    @Before
    fun setUp() {
        server.start()

        tokenStore = mockk(relaxed = true)
        every { tokenStore.get() } returns null

        val authInterceptor = AuthInterceptor(tokenStore)
        val errorInterceptor = ErrorMappingInterceptor(tokenStore)

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(errorInterceptor)
            .build()

        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(VylexApi::class.java)
    }

    @After fun tearDown() = server.shutdown()

    @Test fun `login success parses TokenResponse`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"access_token":"abc.def.ghi","token_type":"bearer","expires_in":86400}"""
            )
        )
        val result = api.login(LoginRequest("user@vylexai.com", "password12345"))
        assertEquals("abc.def.ghi", result.accessToken)
        assertEquals(86400L, result.expiresIn)
    }

    @Test fun `401 clears token and throws AuthExpired`() = runTest {
        every { tokenStore.get() } returns "expired.jwt"
        server.enqueue(
            MockResponse().setResponseCode(401).setBody("""{"detail":"invalid_token"}""")
        )
        try {
            api.walletBalance()
            error("expected AuthExpired")
        } catch (_: VylexException.AuthExpired) {
            // expected
        }
        verify { tokenStore.clear() }
    }

    @Test fun `402 throws InsufficientFunds`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(402).setBody("""{"detail":"insufficient_bsai"}""")
        )
        try {
            api.createJob(
                com.vylexai.app.data.net.dto.JobCreateRequest(
                    taskType = "image_classification",
                    modelRef = "mobilenet.tflite",
                    inputRefs = listOf("blob://1")
                )
            )
            error("expected InsufficientFunds")
        } catch (_: VylexException.InsufficientFunds) {
            // expected
        }
    }

    @Test fun `authenticated requests carry Bearer header`() = runTest {
        every { tokenStore.get() } returns "real.jwt.here"
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"balance_bsai":"10","total_credited_bsai":"10","total_debited_bsai":"0"}"""
            )
        )
        api.walletBalance()
        val recorded = server.takeRequest()
        assertEquals("Bearer real.jwt.here", recorded.getHeader("Authorization"))
    }

    @Test fun `unauthenticated requests omit Authorization header`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"access_token":"t","token_type":"bearer","expires_in":60}"""
            )
        )
        api.login(LoginRequest("e@vylexai.com", "password12345"))
        val recorded = server.takeRequest()
        assertEquals(null, recorded.getHeader("Authorization"))
    }
}
