package com.thanhng224.androidcomposebase.di

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Logs fixed request metadata without exposing URLs, headers, bodies, or exception messages. */
internal class MetadataLoggingInterceptor(
    private val enabled: Boolean,
    private val log: (String) -> Unit = { Log.d(TAG, it) },
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startedAt = System.nanoTime()

        return try {
            val response = chain.proceed(request)
            logIfEnabled(
                "method=${request.method} status=${response.code} duration_ms=${elapsedMillis(startedAt)}",
            )
            response
        } catch (failure: IOException) {
            logIfEnabled("method=${request.method} outcome=io_failure duration_ms=${elapsedMillis(startedAt)}")
            throw failure
        }
    }

    private fun logIfEnabled(message: String) {
        if (enabled) log(message)
    }

    private fun elapsedMillis(startedAt: Long): Long = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)

    private companion object {
        const val TAG = "NetworkMetadata"
    }
}
