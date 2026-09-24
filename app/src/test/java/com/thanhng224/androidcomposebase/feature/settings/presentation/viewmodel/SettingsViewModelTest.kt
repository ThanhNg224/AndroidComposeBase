package com.thanhng224.androidcomposebase.feature.settings.presentation.viewmodel

import app.cash.turbine.test
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import com.thanhng224.androidcomposebase.core.localization.SupportedLanguages
import com.thanhng224.androidcomposebase.core.testing.MainDispatcherRule
import com.thanhng224.androidcomposebase.core.ui.theme.AppTheme
import com.thanhng224.androidcomposebase.feature.settings.domain.repository.SettingsRepository
import com.thanhng224.androidcomposebase.feature.settings.domain.usecase.GetCurrentLanguageUseCase
import com.thanhng224.androidcomposebase.feature.settings.domain.usecase.GetSupportedLanguagesUseCase
import com.thanhng224.androidcomposebase.feature.settings.domain.usecase.ObserveThemeUseCase
import com.thanhng224.androidcomposebase.feature.settings.domain.usecase.SetLanguageUseCase
import com.thanhng224.androidcomposebase.feature.settings.domain.usecase.SetThemeUseCase
import com.thanhng224.androidcomposebase.feature.settings.presentation.state.SettingsUiEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeSettingsRepository(
        languageTag: String? = AppLanguage.ENGLISH.languageTag,
        private val supportedLanguageTags: List<String> = AppLanguage.BUILT_IN.map(AppLanguage::languageTag),
        theme: AppTheme = AppTheme.SYSTEM,
        private val calls: MutableList<String>? = null,
        private val failLanguagePersistence: Boolean = false,
    ) : SettingsRepository {
        private val themeFlow = MutableStateFlow(theme)
        private var currentLanguageTag = languageTag
        var setThemeCalls = 0
            private set

        override fun observeTheme(): Flow<AppTheme> = themeFlow

        override suspend fun getCurrentLanguageTag(): String? = currentLanguageTag

        override fun getSupportedLanguageTags(): List<String> = supportedLanguageTags

        override suspend fun setLanguageTag(languageTag: String?) {
            val tag = languageTag ?: "system"
            calls?.add("persist:$tag")
            if (failLanguagePersistence) throw IOException("persist failed")
            calls?.add("apply:$tag")
            currentLanguageTag = languageTag
        }

        override suspend fun setTheme(theme: AppTheme) {
            setThemeCalls += 1
            themeFlow.value = theme
        }
    }

    @Test
    fun `initial state reflects the current language and observed theme`() =
        runTest {
            val viewModel = createViewModel(FakeSettingsRepository(AppLanguage.VIETNAMESE.languageTag, theme = AppTheme.DARK))

            advanceUntilIdle()

            assertEquals(AppLanguage.VIETNAMESE, viewModel.state.value.language)
            assertEquals(AppTheme.DARK, viewModel.state.value.theme)
        }

    @Test
    fun `theme selection persists and updates the shared screen state`() =
        runTest {
            val repository = FakeSettingsRepository(theme = AppTheme.SYSTEM)
            val viewModel = createViewModel(repository)

            advanceUntilIdle()
            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.DARK))
            advanceUntilIdle()

            assertEquals(AppTheme.DARK, viewModel.state.value.theme)
            assertEquals(1, repository.setThemeCalls)
        }

    @Test
    fun `selecting the current theme does not persist again`() =
        runTest {
            val repository = FakeSettingsRepository(theme = AppTheme.LIGHT)
            val viewModel = createViewModel(repository)

            advanceUntilIdle()
            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.LIGHT))
            advanceUntilIdle()

            assertEquals(0, repository.setThemeCalls)
        }

    @Test
    fun `selecting a language persists before applying it`() =
        runTest {
            val calls = mutableListOf<String>()
            val viewModel = createViewModel(FakeSettingsRepository(languageTag = AppLanguage.ENGLISH.languageTag, calls = calls))
            advanceUntilIdle()

            viewModel.onEvent(SettingsUiEvent.LanguageSelected(AppLanguage.VIETNAMESE))
            advanceUntilIdle()

            assertEquals(listOf("persist:vi-VN", "apply:vi-VN"), calls)
            assertEquals(AppLanguage.VIETNAMESE, viewModel.state.value.language)
        }

    @Test
    fun `a failed language persistence keeps the previous language and queues an error message`() =
        runTest {
            val calls = mutableListOf<String>()
            val viewModel =
                createViewModel(
                    FakeSettingsRepository(languageTag = AppLanguage.ENGLISH.languageTag, calls = calls, failLanguagePersistence = true),
                )
            advanceUntilIdle()

            viewModel.onEvent(SettingsUiEvent.LanguageSelected(AppLanguage.VIETNAMESE))
            advanceUntilIdle()

            assertEquals(listOf("persist:vi-VN"), calls)
            assertEquals(AppLanguage.ENGLISH, viewModel.state.value.language)
            assertEquals(1, viewModel.state.value.pendingMessages.size)
        }

    @Test
    fun `acknowledging the language error message removes it`() =
        runTest {
            val viewModel =
                createViewModel(FakeSettingsRepository(languageTag = AppLanguage.ENGLISH.languageTag, failLanguagePersistence = true))
            advanceUntilIdle()
            viewModel.onEvent(SettingsUiEvent.LanguageSelected(AppLanguage.VIETNAMESE))
            advanceUntilIdle()
            val message =
                viewModel.state.value.pendingMessages
                    .first()

            viewModel.onMessageHandled(message.id)

            assertEquals(0, viewModel.state.value.pendingMessages.size)
        }

    @Test
    fun `re-collecting state does not repeat the language mutation`() =
        runTest {
            val calls = mutableListOf<String>()
            val viewModel = createViewModel(FakeSettingsRepository(languageTag = AppLanguage.ENGLISH.languageTag, calls = calls))
            advanceUntilIdle()
            viewModel.onEvent(SettingsUiEvent.LanguageSelected(AppLanguage.VIETNAMESE))
            advanceUntilIdle()

            viewModel.state.test { awaitItem() }
            viewModel.state.test { awaitItem() }

            assertEquals(listOf("persist:vi-VN", "apply:vi-VN"), calls)
        }

    @Test
    fun `custom supported language tags map to their presentation labels`() =
        runTest {
            val customLanguage = AppLanguage("fr-CA", R.string.app_name)
            val repository =
                FakeSettingsRepository(
                    languageTag = customLanguage.languageTag,
                    supportedLanguageTags = listOf(customLanguage.languageTag),
                )
            val viewModel = createViewModel(repository, SupportedLanguages(listOf(customLanguage)))

            advanceUntilIdle()

            assertEquals(listOf(customLanguage), viewModel.state.value.supportedLanguages)
            assertEquals(customLanguage, viewModel.state.value.language)
        }

    private fun createViewModel(
        repository: SettingsRepository,
        supportedLanguages: SupportedLanguages = SupportedLanguages(AppLanguage.BUILT_IN),
    ): SettingsViewModel =
        SettingsViewModel(
            observeTheme = ObserveThemeUseCase(repository),
            supportedLanguages = supportedLanguages,
            getCurrentLanguage = GetCurrentLanguageUseCase(repository),
            getSupportedLanguages = GetSupportedLanguagesUseCase(repository),
            setTheme = SetThemeUseCase(repository),
            setLanguage = SetLanguageUseCase(repository),
        )
}
