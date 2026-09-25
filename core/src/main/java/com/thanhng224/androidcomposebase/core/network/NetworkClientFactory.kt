package com.thanhng224.androidcomposebase.core.network

import com.thanhng224.androidcomposebase.core.foundation.AppDispatchers
import com.thanhng224.androidcomposebase.core.network.auth.AuthSession
import com.thanhng224.androidcomposebase.core.network.auth.AuthTokenProvider
import com.thanhng224.androidcomposebase.core.network.auth.AuthTokenRefresher
import com.thanhng224.androidcomposebase.core.network.auth.DEFAULT_AUTH_SCHEME
import com.thanhng224.androidcomposebase.core.network.auth.SecureStoreAuthTokenProvider
import com.thanhng224.androidcomposebase.core.network.auth.TokenAuthenticator
import com.thanhng224.androidcomposebase.core.network.transfer.FileTransferClient
import com.thanhng224.androidcomposebase.core.network.transfer.OkHttpFileTransferClient
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

public object NetworkClientFactory {
    /** Default JSON configuration for [createRetrofit]: tolerant of unknown/absent fields. */
    public val defaultJson: Json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    public fun createOkHttpClient(
        config: ApiConfig,
        interceptors: List<Interceptor> = emptyList(),
        authenticator: Authenticator = Authenticator.NONE,
    ): OkHttpClient {
        val builder =
            OkHttpClient
                .Builder()
                .connectTimeout(config.connectTimeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(config.readTimeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(config.writeTimeoutSeconds, TimeUnit.SECONDS)
                .authenticator(authenticator)
        interceptors.forEach(builder::addInterceptor)
        return builder.build()
    }

    public fun createRetrofit(
        config: ApiConfig,
        okHttpClient: OkHttpClient,
        json: Json = defaultJson,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(config.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    public fun createApiClient(): ApiClient = RetrofitApiClient()

    public fun createFileTransferClient(
        okHttpClient: OkHttpClient,
        dispatchers: AppDispatchers,
    ): FileTransferClient = OkHttpFileTransferClient(okHttpClient, dispatchers)

    public fun createAuthTokenProvider(authSession: AuthSession): AuthTokenProvider = SecureStoreAuthTokenProvider(authSession)

    public fun createAuthenticator(
        authSession: AuthSession,
        tokenRefresher: (() -> AuthTokenRefresher)? = null,
        scheme: String = DEFAULT_AUTH_SCHEME,
    ): Authenticator = TokenAuthenticator(authSession, tokenRefresher, scheme)
}
