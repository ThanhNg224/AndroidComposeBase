package com.thanhng224.androidcomposebase.core.network.auth

import app.cash.turbine.test
import com.thanhng224.androidcomposebase.core.foundation.SecureStoreKeys
import com.thanhng224.androidcomposebase.core.testing.FakeSecureStore
import kotlinx.coroutines.runBlocking
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TokenAuthenticatorTest {
    private class FakeAuthTokenRefresher(
        private val newToken: String?,
    ) : AuthTokenRefresher {
        var callCount = 0

        override suspend fun refresh(refreshToken: String?): String? {
            callCount++
            return newToken
        }
    }

    private fun response(
        authorizationHeader: String? = null,
        priorResponse: Response? = null,
    ): Response {
        val request =
            Request
                .Builder()
                .url("https://example.com/")
                .apply { authorizationHeader?.let { header("Authorization", it) } }
                .build()
        return Response
            .Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .apply { priorResponse?.let { priorResponse(it) } }
            .build()
    }

    private fun authenticator(
        authSession: AuthSession,
        refresher: AuthTokenRefresher? = null,
    ): TokenAuthenticator = TokenAuthenticator(authSession, refresher?.let { { it } })

    @Test
    fun `authenticate returns cached token formatted with the scheme when it differs from the token that just failed`() =
        runBlocking {
            val store = FakeSecureStore()
            store.putString(SecureStoreKeys.AUTH_TOKEN, "already-newer-token")
            val refresher = FakeAuthTokenRefresher("should-not-be-used")
            val sut = authenticator(AuthSession(store), refresher)

            val result = sut.authenticate(null, response(authorizationHeader = "Bearer stale-token"))

            assertEquals("Bearer already-newer-token", result?.header("Authorization"))
            assertEquals(0, refresher.callCount)
        }

    @Test
    fun `authenticate refreshes and persists the new token, formatted with the scheme, when the cached token matches the failed one`() =
        runBlocking {
            val store = FakeSecureStore()
            store.putString(SecureStoreKeys.AUTH_TOKEN, "expired-token")
            val refresher = FakeAuthTokenRefresher("fresh-token")
            val sut = authenticator(AuthSession(store), refresher)

            val result = sut.authenticate(null, response(authorizationHeader = "Bearer expired-token"))

            assertEquals("Bearer fresh-token", result?.header("Authorization"))
            assertEquals(1, refresher.callCount)
            assertEquals("fresh-token", store.getString(SecureStoreKeys.AUTH_TOKEN))
        }

    @Test
    fun `authenticate returns null and keeps tokens when no refresher is bound`() =
        runBlocking {
            val store = FakeSecureStore()
            store.putString(SecureStoreKeys.AUTH_TOKEN, "expired-token")
            val sut = authenticator(AuthSession(store), refresher = null)

            val result = sut.authenticate(null, response(authorizationHeader = "Bearer expired-token"))

            assertNull(result)
            assertEquals("expired-token", store.getString(SecureStoreKeys.AUTH_TOKEN))
        }

    @Test
    fun `authenticate clears the session and emits sessionExpired once when the refresher fails`() =
        runBlocking {
            val store = FakeSecureStore()
            store.putString(SecureStoreKeys.AUTH_TOKEN, "expired-token")
            store.putString(SecureStoreKeys.REFRESH_TOKEN, "refresh-token")
            val authSession = AuthSession(store)
            val refresher = FakeAuthTokenRefresher(newToken = null)
            val sut = authenticator(authSession, refresher)

            authSession.sessionExpired.test {
                val result = sut.authenticate(null, response(authorizationHeader = "Bearer expired-token"))

                assertNull(result)
                assertEquals(1, refresher.callCount)
                assertEquals(Unit, awaitItem())
                assertNull(authSession.getAccessToken())
                assertNull(authSession.getRefreshToken())
            }
        }

    @Test
    fun `authenticate gives up after too many retries without touching the refresher`() =
        runBlocking {
            val store = FakeSecureStore()
            val refresher = FakeAuthTokenRefresher("fresh-token")
            val sut = authenticator(AuthSession(store), refresher)

            val first = response(authorizationHeader = "t")
            val second = response(authorizationHeader = "t", priorResponse = first)
            val third = response(authorizationHeader = "t", priorResponse = second)

            val result = sut.authenticate(null, third)

            assertNull(result)
            assertEquals(0, refresher.callCount)
        }

    @Test
    fun `authenticate allows at most one retry, giving up immediately after it also fails`() =
        runBlocking {
            val store = FakeSecureStore()
            store.putString(SecureStoreKeys.AUTH_TOKEN, "expired-token")
            val refresher = FakeAuthTokenRefresher("fresh-token")
            val sut = authenticator(AuthSession(store), refresher)

            val originalFailure = response(authorizationHeader = "Bearer expired-token")
            val retryFailure = response(authorizationHeader = "Bearer expired-token", priorResponse = originalFailure)

            val result = sut.authenticate(null, retryFailure)

            assertNull(result)
            assertEquals(0, refresher.callCount)
        }
}
