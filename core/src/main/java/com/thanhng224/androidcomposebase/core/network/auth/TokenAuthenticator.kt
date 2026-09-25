package com.thanhng224.androidcomposebase.core.network.auth

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

internal class TokenAuthenticator
    internal constructor(
        private val authSession: AuthSession,
        private val tokenRefresher: (() -> AuthTokenRefresher)? = null,
        private val scheme: String = DEFAULT_AUTH_SCHEME,
    ) : Authenticator {
        private val refreshMutex = Mutex()

        override fun authenticate(
            route: Route?,
            response: Response,
        ): Request? {
            // A non-null priorResponse means this failure is itself the result of a retry that
            // this authenticator already attempted, so at most one retry ever follows the
            // original 401.
            if (response.priorResponse != null) return null

            val failedAuthHeader = response.request.header("Authorization")

            val nextToken =
                runBlocking {
                    refreshMutex.withLock {
                        val cached = authSession.getAccessToken()
                        if (cached != null && authorizationValue(scheme, cached) != failedAuthHeader) {
                            cached
                        } else {
                            refreshAndPersist()
                        }
                    }
                }

            if (nextToken.isNullOrBlank()) return null

            return response.request
                .newBuilder()
                .header("Authorization", authorizationValue(scheme, nextToken))
                .build()
        }

        private suspend fun refreshAndPersist(): String? {
            val refresher = tokenRefresher?.invoke() ?: return null
            val newToken = refresher.refresh(authSession.getRefreshToken())
            if (newToken == null) {
                authSession.clear()
                authSession.notifySessionExpired()
                return null
            }
            authSession.setTokens(newToken)
            return newToken
        }
    }
