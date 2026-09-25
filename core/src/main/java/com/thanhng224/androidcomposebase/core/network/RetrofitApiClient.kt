package com.thanhng224.androidcomposebase.core.network

import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException

/** Error bodies are truncated to this many characters before being surfaced as [ApiFailure.Http.serverMessage]. */
private const val MAX_ERROR_BODY_CHARS = 2_048

/**
 * The error body's text when present and non-blank, falling back to the HTTP status message.
 * Reading the body can itself throw (e.g. the connection was reset mid-body); that failure must
 * not escape as [ApiFailure.Network] and hide a real HTTP failure, so it is caught here and
 * treated the same as a missing/blank body.
 */
private fun Response<*>.errorServerMessage(): String? =
    try {
        errorBody()?.use { it.string() }?.takeIf(String::isNotBlank)?.take(MAX_ERROR_BODY_CHARS)
    } catch (expected: IOException) {
        null
    } ?: message().takeIf(String::isNotBlank)

internal class RetrofitApiClient internal constructor() : ApiClient {
    override suspend fun <T> execute(call: suspend () -> Response<T>): ApiResult<T> =
        try {
            val response = call()
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it) }
                    ?: ApiResult.Failure(ApiFailure.EmptyBody)
            } else {
                ApiResult.Failure(ApiFailure.Http(response.code(), response.errorServerMessage()))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            ApiResult.Failure(ApiFailure.Network(e))
        } catch (e: Exception) {
            ApiResult.Failure(ApiFailure.Serialization(e))
        }
}
