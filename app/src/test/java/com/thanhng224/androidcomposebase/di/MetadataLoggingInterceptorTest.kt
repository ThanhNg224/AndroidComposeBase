package com.thanhng224.androidcomposebase.di

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MetadataLoggingInterceptorTest {
    @Test
    fun enabledLoggingEmitsMetadataWithoutRequestOrResponseSecrets() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(RESPONSE_SECRET))
        server.start()
        try {
            val messages = mutableListOf<String>()
            val client = OkHttpClient.Builder().addInterceptor(MetadataLoggingInterceptor(enabled = true, log = messages::add)).build()
            val request =
                Request
                    .Builder()
                    .url(server.url("/private/$PATH_SECRET?access_token=$QUERY_SECRET"))
                    .header("Authorization", "Bearer $HEADER_SECRET")
                    .post(REQUEST_SECRET.toRequestBody("text/plain".toMediaType()))
                    .build()

            val response = client.newCall(request).execute()

            assertEquals(RESPONSE_SECRET, response.body?.string())
            assertEquals(1, messages.size)
            assertTrue(messages.single().contains("method=POST status=200 duration_ms="))
            listOf(server.hostName, "/private/", PATH_SECRET, QUERY_SECRET, HEADER_SECRET, REQUEST_SECRET, RESPONSE_SECRET)
                .forEach { sensitiveValue -> assertFalse(messages.single().contains(sensitiveValue)) }
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun disabledLoggingEmitsNothing() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("ok"))
        server.start()
        try {
            val messages = mutableListOf<String>()
            val client = OkHttpClient.Builder().addInterceptor(MetadataLoggingInterceptor(enabled = false, log = messages::add)).build()
            val request = Request.Builder().url(server.url("/weather")).build()

            client.newCall(request).execute().use { assertEquals("ok", it.body?.string()) }

            assertTrue(messages.isEmpty())
        } finally {
            server.shutdown()
        }
    }

    private companion object {
        const val PATH_SECRET = "person-123"
        const val QUERY_SECRET = "query-secret"
        const val HEADER_SECRET = "header-secret"
        const val REQUEST_SECRET = "request-secret"
        const val RESPONSE_SECRET = "response-secret"
    }
}
