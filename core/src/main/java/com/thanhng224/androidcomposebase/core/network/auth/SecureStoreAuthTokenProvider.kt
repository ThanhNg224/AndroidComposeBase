package com.thanhng224.androidcomposebase.core.network.auth

internal class SecureStoreAuthTokenProvider
    internal constructor(
        private val authSession: AuthSession,
    ) : AuthTokenProvider {
        override fun peekToken(): String? = authSession.peekAccessToken()

        override suspend fun getToken(): String? = authSession.getAccessToken()
    }
