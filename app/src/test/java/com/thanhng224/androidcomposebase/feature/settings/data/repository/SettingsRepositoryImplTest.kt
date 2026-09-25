package com.thanhng224.androidcomposebase.feature.settings.data.repository

import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import com.thanhng224.androidcomposebase.core.localization.AppLocaleApplier
import com.thanhng224.androidcomposebase.core.localization.LocaleManager
import com.thanhng224.androidcomposebase.core.testing.FakeSettingsStore
import com.thanhng224.androidcomposebase.core.theme.ThemeManager
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
    fun `repository reads external locale changes from the platform locale source`() =
        runTest {
            val settingsStore = FakeSettingsStore()
            val localeApplier = FakeLocaleApplier()
            val repository = createRepository(settingsStore, localeApplier)
            localeApplier.applyLocales("fr-FR")

            assertEquals("fr-CA", repository.currentLanguageTag())
        }

    @Test
    fun `setting a configured language updates the platform app locale source`() =
        runTest {
            val settingsStore = FakeSettingsStore()
            val localeApplier = FakeLocaleApplier()
            val repository = createRepository(settingsStore, localeApplier)

            repository.setLanguageTag("fr-CA")

            assertEquals("fr-CA", localeApplier.appliedTags)
            assertEquals("fr-CA", repository.currentLanguageTag())
        }

    @Test
    fun `null language tag clears locale override and reports system selection`() =
        runTest {
            val settingsStore = FakeSettingsStore()
            val localeApplier = FakeLocaleApplier()
            localeApplier.applyLocales("fr-CA")
            val repository = createRepository(settingsStore, localeApplier)

            repository.setLanguageTag(null)

            assertEquals("", localeApplier.appliedTags)
            assertEquals(null, repository.currentLanguageTag())
        }

    @Test
    fun `external system locale reset is reflected without repository writes`() =
        runTest {
            val settingsStore = FakeSettingsStore()
            val localeApplier = FakeLocaleApplier()
            val repository = createRepository(settingsStore, localeApplier)
            localeApplier.applyLocales("fr-CA")

            assertEquals("fr-CA", repository.currentLanguageTag())

            localeApplier.applyLocales("")

            assertEquals(null, repository.currentLanguageTag())
        }

    @Test
    fun `unsupported language tag is rejected before applying a platform locale`() =
        runTest {
            val settingsStore = FakeSettingsStore()
            val repository = createRepository(settingsStore)

            assertThrows(IllegalArgumentException::class.java) { repository.setLanguageTag("de-DE") }
            assertEquals(null, repository.currentLanguageTag())
        }

    private fun createRepository(
        settingsStore: FakeSettingsStore,
        localeApplier: FakeLocaleApplier = FakeLocaleApplier(),
    ): SettingsRepositoryImpl =
        SettingsRepositoryImpl(
            themeManager = ThemeManager.create(settingsStore),
            localeManager = LocaleManager(localeApplier, listOf(customLanguage)),
        )
}
