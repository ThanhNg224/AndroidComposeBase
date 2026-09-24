package com.thanhng224.androidcomposebase.feature.settings.data.repository

import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import com.thanhng224.androidcomposebase.core.localization.AppLocaleApplier
import com.thanhng224.androidcomposebase.core.localization.LocaleManager
import com.thanhng224.androidcomposebase.core.storage.settings.AppSettingsKeys
import com.thanhng224.androidcomposebase.core.testing.FakeSettingsStore
import com.thanhng224.androidcomposebase.core.ui.theme.ThemeManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SettingsRepositoryImplTest {
    private val customLanguage = AppLanguage(languageTag = "fr-CA", displayNameResId = R.string.app_name)

    private class FakeLocaleApplier : AppLocaleApplier {
        var appliedTags: String = ""
            private set

        override fun applyLocales(tag: String) {
            appliedTags = tag
        }

        override fun currentLocaleTags(): String = appliedTags
    }

    @Test
    fun `domain exposes configured custom language as a pure language tag`() =
        runTest {
            val settingsStore = FakeSettingsStore()
            val repository = createRepository(settingsStore)
            settingsStore.set(AppSettingsKeys.LANGUAGE_TAG, "fr-FR")

            assertEquals(listOf("fr-CA"), repository.getSupportedLanguageTags())
            assertEquals("fr-CA", repository.getCurrentLanguageTag())
        }

    @Test
    fun `setting a configured custom tag persists it then applies that locale`() =
        runTest {
            val settingsStore = FakeSettingsStore()
            val localeApplier = FakeLocaleApplier()
            val repository = createRepository(settingsStore, localeApplier)

            repository.setLanguageTag("fr-CA")

            assertEquals("fr-CA", settingsStore.get(AppSettingsKeys.LANGUAGE_TAG))
            assertEquals("fr-CA", localeApplier.appliedTags)
        }

    @Test
    fun `null language tag persists system selection and clears locale override`() =
        runTest {
            val settingsStore = FakeSettingsStore()
            val localeApplier = FakeLocaleApplier()
            localeApplier.applyLocales("fr-CA")
            val repository = createRepository(settingsStore, localeApplier)

            repository.setLanguageTag(null)

            assertEquals("", settingsStore.get(AppSettingsKeys.LANGUAGE_TAG))
            assertEquals("", localeApplier.appliedTags)
        }

    @Test
    fun `unsupported language tag is rejected before persistence`() =
        runTest {
            val settingsStore = FakeSettingsStore()
            val repository = createRepository(settingsStore)

            assertThrows(IllegalArgumentException::class.java) {
                kotlinx.coroutines.runBlocking { repository.setLanguageTag("de-DE") }
            }
            assertEquals("", settingsStore.get(AppSettingsKeys.LANGUAGE_TAG))
        }

    private fun createRepository(
        settingsStore: FakeSettingsStore,
        localeApplier: FakeLocaleApplier = FakeLocaleApplier(),
    ): SettingsRepositoryImpl =
        SettingsRepositoryImpl(
            themeManager = ThemeManager.create(settingsStore),
            localeManager = LocaleManager(localeApplier, listOf(customLanguage)),
            settingsStore = settingsStore,
        )
}
