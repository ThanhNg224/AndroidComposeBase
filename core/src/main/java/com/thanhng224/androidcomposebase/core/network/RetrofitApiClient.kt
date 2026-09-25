package com.thanhng224.androidcomposebase.core.network

import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException

/** Error bodies are truncated to this many characters before being surfaced as [ApiFailure.Http.serverMessage]. */
private const val MAX_ERROR_BODY_CHARS = 2_048

internal class RetrofitApiClient internal constructor() : ApiClient {
    override suspend fun <T> execute(call: suspend () -> Response<T>): ApiResult<T> =
        try {
            val response = call()
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it) }
                    ?: ApiResult.Failure(ApiFailure.EmptyBody)
            } else {
                val serverMessage =
                    response
                        .errorBody()
                        ?.use { it.string() }
                        ?.takeIf(String::isNotBlank)
                        ?.take(MAX_ERROR_BODY_CHARS)
                        ?: response.message().takeIf(String::isNotBlank)
                ApiResult.Failure(ApiFailure.Http(response.code(), serverMessage))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            ApiResult.Failure(ApiFailure.Network(e))
        } catch (e: Exception) {
            ApiResult.Failure(ApiFailure.Serialization(e))
        }
}
