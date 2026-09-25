package com.thanhng224.androidcomposebase.feature.onboarding.data.repository

import com.thanhng224.androidcomposebase.core.foundation.SettingsKey
import com.thanhng224.androidcomposebase.core.foundation.SettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingRepositoryImplTest {
    @Test
    fun `reads and persists onboarding completion`() =
        runBlocking {
            val store = RecordingSettingsStore()
            val repository = OnboardingRepositoryImpl(store)

            assertFalse(repository.isCompleted())
            repository.complete()

            assertTrue(repository.isCompleted())
            assertEquals(listOf("onboarding_completed"), store.writes)
        }

    private class RecordingSettingsStore : SettingsStore {
        private val values = mutableMapOf<String, Any>()
        val writes = mutableListOf<String>()

        @Suppress("UNCHECKED_CAST")
        override fun <T> observe(key: SettingsKey<T>): Flow<T> = flowOf(values[key.name] as? T ?: key.defaultValue)

        @Suppress("UNCHECKED_CAST")
        override suspend fun <T> get(key: SettingsKey<T>): T = values[key.name] as? T ?: key.defaultValue

        override suspend fun <T> set(
            key: SettingsKey<T>,
            value: T,
        ) {
            values[key.name] = value as Any
            writes += key.name
        }

        override suspend fun <T> remove(key: SettingsKey<T>) {
            values.remove(key.name)
        }
    }
}
