package com.thanhng224.androidcomposebase.core.network

import com.thanhng224.androidcomposebase.core.foundation.SecureStoreKeys
import com.thanhng224.androidcomposebase.core.network.auth.AuthSession
import com.thanhng224.androidcomposebase.core.network.auth.AuthTokenRefresher
import com.thanhng224.androidcomposebase.core.testing.FakeSecureStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkClientFactoryTest {
    @Test
    fun `creates client with thirty second connect read and write timeouts`() {
        val client = NetworkClientFactory.createOkHttpClient(config = ApiConfig(baseUrl = "https://example.com/"))

        assertEquals(30_000, client.connectTimeoutMillis)
        assertEquals(30_000, client.readTimeoutMillis)
        assertEquals(30_000, client.writeTimeoutMillis)
    }

    @Test
    fun `applies per-timeout overrides from the supplied config`() {
        val client =
            NetworkClientFactory.createOkHttpClient(
                config =
                    ApiConfig(
                        baseUrl = "https://example.com/",
                        connectTimeoutSeconds = 5,
                        readTimeoutSeconds = 15,
                        writeTimeoutSeconds = 45,
                    ),
            )

        assertEquals(5_000, client.connectTimeoutMillis)
        assertEquals(15_000, client.readTimeoutMillis)
        assertEquals(45_000, client.writeTimeoutMillis)
    }

    @Test
    fun `retrofit base url comes from the supplied config`() {
        val config = ApiConfig(baseUrl = "https://api.example.com/")
        val client = NetworkClientFactory.createOkHttpClient(config = config)

        val retrofit = NetworkClientFactory.createRetrofit(config = config, okHttpClient = client)

        assertEquals("https://api.example.com/", retrofit.baseUrl().toString())
    }

    @Test
    fun `installs no interceptor when the caller supplies none`() {
        val client = NetworkClientFactory.createOkHttpClient(config = ApiConfig(baseUrl = "https://example.com/"))

        assertTrue(client.interceptors.isEmpty())
    }

    @Test
    fun `installs exactly the caller-supplied interceptors, in order`() {
        val first = Interceptor { chain -> chain.proceed(chain.request()) }
        val second = Interceptor { chain -> chain.proceed(chain.request()) }

        val client =
            NetworkClientFactory.createOkHttpClient(
                config = ApiConfig(baseUrl = "https://example.com/"),
                interceptors = listOf(first, second),
            )

        assertEquals(listOf(first, second), client.interceptors)
    }

    @Test
    fun `defaultJson ignores unknown keys and omits explicit nulls`() {
        assertTrue(NetworkClientFactory.defaultJson.configuration.ignoreUnknownKeys)
        assertFalse(NetworkClientFactory.defaultJson.configuration.explicitNulls)
    }

    @Test
    fun `createRetrofit installs a converter factory by default`() {
        val config = ApiConfig(baseUrl = "https://api.example.com/")
        val client = NetworkClientFactory.createOkHttpClient(config = config)

        val retrofit = NetworkClientFactory.createRetrofit(config = config, okHttpClient = client)

        assertTrue(retrofit.converterFactories().isNotEmpty())
    }

    @Test
    fun `createAuthenticator forwards the given scheme into the retry header`() =
        runBlocking {
            val store = FakeSecureStore()
            store.putString(SecureStoreKeys.AUTH_TOKEN, "old-token")
            val authSession = AuthSession(store)
            val refresher =
                object : AuthTokenRefresher {
                    override suspend fun refresh(refreshToken: String?): String? = "new"
                }
            val authenticator = NetworkClientFactory.createAuthenticator(authSession, tokenRefresher = { refresher }, scheme = "Token")
            val failedRequest =
                Request
                    .Builder()
                    .url("https://example.com/")
                    .header("Authorization", "Token old-token")
                    .build()
            val failedResponse =
                Response
                    .Builder()
                    .request(failedRequest)
                    .protocol(Protocol.HTTP_1_1)
                    .code(401)
                    .message("Unauthorized")
                    .build()

            val retryRequest = authenticator.authenticate(null, failedResponse)

            assertEquals("Token new", retryRequest?.header("Authorization"))
        }
}
