package com.thanhng224.androidcomposebase.feature.settings.presentation.viewmodel

import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import com.thanhng224.androidcomposebase.core.localization.SupportedLanguages
import com.thanhng224.androidcomposebase.core.testing.MainDispatcherRule
import com.thanhng224.androidcomposebase.core.text.UiText
import com.thanhng224.androidcomposebase.core.theme.AppTheme
import com.thanhng224.androidcomposebase.feature.settings.domain.repository.SettingsRepository
import com.thanhng224.androidcomposebase.feature.settings.presentation.state.SettingsUiEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeSettingsRepository(
        languageTag: String? = AppLanguage.ENGLISH.languageTag,
        private val supportedLanguages: List<AppLanguage> = AppLanguage.BUILT_IN,
        theme: AppTheme = AppTheme.SYSTEM,
        private val delayInitialThemeObservation: Boolean = false,
    ) : SettingsRepository {
        private val themeFlow = MutableStateFlow(theme)
        private val initialTheme = theme
        val initialThemeObservationStarted = CompletableDeferred<Unit>()
        val releaseInitialThemeObservation = CompletableDeferred<Unit>()
        var currentLanguageTag: String? = languageTag
            private set
        var failThemePersistence = false
        var failLanguageChange = false
        var themeCalls = 0
        val themeWrites = mutableListOf<AppTheme>()
        val languageWrites = mutableListOf<String?>()
        var delayedTheme: AppTheme? = null
        val delayedThemeStarted = CompletableDeferred<Unit>()
        val finishDelayedTheme = CompletableDeferred<Unit>()

        override fun observeTheme(): Flow<AppTheme> =
            if (delayInitialThemeObservation) {
                flow {
                    initialThemeObservationStarted.complete(Unit)
                    releaseInitialThemeObservation.await()
                    emit(initialTheme)
                    emitAll(themeFlow)
                }
            } else {
                themeFlow
            }

        override suspend fun setTheme(theme: AppTheme) {
            themeCalls += 1
            themeWrites += theme
            if (failThemePersistence) {
                failThemePersistence = false
                throw IOException("theme write failed")
            }
            if (theme == delayedTheme) {
                delayedThemeStarted.complete(Unit)
                finishDelayedTheme.await()
            }
            themeFlow.value = theme
        }

        override fun currentLanguageTag(): String? = currentLanguageTag

        override fun setLanguageTag(languageTag: String?) {
            languageWrites += languageTag
            require(
                languageTag == null || AppLanguage.findByLanguageTag(languageTag, supportedLanguages) != null,
            ) { "Unsupported language tag: $languageTag" }
            if (failLanguageChange) throw IOException("locale apply failed")
            currentLanguageTag = languageTag
        }

        fun setExternalLanguage(languageTag: String?) {
            currentLanguageTag = languageTag
        }

        fun setExternalTheme(theme: AppTheme) {
            themeFlow.value = theme
        }
    }

    @Test
    fun `initial state reflects repository theme and language`() =
        runTest {
            val repository =
                FakeSettingsRepository(
                    languageTag = AppLanguage.VIETNAMESE.languageTag,
                    theme = AppTheme.DARK,
                )
            val viewModel = createViewModel(repository)
            collectState(viewModel)
            advanceUntilIdle()

            assertEquals(AppTheme.DARK, viewModel.state.value.theme)
            assertEquals(AppLanguage.VIETNAMESE, viewModel.state.value.language)
            assertEquals(AppLanguage.BUILT_IN, viewModel.state.value.supportedLanguages)
        }

    @Test
    fun `selecting dark persists it and state follows the observed theme`() =
        runTest {
            val repository = FakeSettingsRepository()
            val viewModel = createViewModel(repository)
            collectState(viewModel)

            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.DARK))
            advanceUntilIdle()

            assertEquals(listOf(AppTheme.DARK), repository.themeWrites)
            assertEquals(AppTheme.DARK, viewModel.state.value.theme)
        }

    @Test
    fun `system theme tap before first persisted theme emission is persisted`() =
        runTest {
            val repository =
                FakeSettingsRepository(
                    theme = AppTheme.DARK,
                    delayInitialThemeObservation = true,
                )
            val viewModel = createViewModel(repository)
            collectState(viewModel)
            runCurrent()
            repository.initialThemeObservationStarted.await()

            assertEquals(AppTheme.SYSTEM, viewModel.state.value.theme)
            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.SYSTEM))
            runCurrent()

            assertEquals(listOf(AppTheme.SYSTEM), repository.themeWrites)
            repository.releaseInitialThemeObservation.complete(Unit)
            advanceUntilIdle()

            assertEquals(AppTheme.SYSTEM, viewModel.state.value.theme)
        }

    @Test
    fun `reselecting a theme after persisted theme changes externally writes the selection`() =
        runTest {
            val repository = FakeSettingsRepository()
            val viewModel = createViewModel(repository)
            collectState(viewModel)

            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.DARK))
            advanceUntilIdle()
            repository.setExternalTheme(AppTheme.LIGHT)
            advanceUntilIdle()
            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.DARK))
            advanceUntilIdle()

            assertEquals(listOf(AppTheme.DARK, AppTheme.DARK), repository.themeWrites)
            assertEquals(AppTheme.DARK, viewModel.state.value.theme)
        }

    @Test
    fun `theme write failure leaves persisted state and queues a message`() =
        runTest {
            val repository = FakeSettingsRepository().apply { failThemePersistence = true }
            val viewModel = createViewModel(repository)
            collectState(viewModel)

            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.DARK))
            advanceUntilIdle()

            assertEquals(AppTheme.SYSTEM, viewModel.state.value.theme)
            val text =
                viewModel.state.value.pendingMessages
                    .single()
                    .text as UiText.StringResource
            assertEquals(R.string.settings_theme_update_failed, text.resId)

            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.DARK))
            advanceUntilIdle()

            assertEquals(AppTheme.DARK, viewModel.state.value.theme)
            assertEquals(2, repository.themeCalls)
        }

    @Test
    fun `selecting the persisted theme does not write it again`() =
        runTest {
            val repository = FakeSettingsRepository(theme = AppTheme.LIGHT)
            val viewModel = createViewModel(repository)
            collectState(viewModel)

            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.LIGHT))
            advanceUntilIdle()

            assertEquals(0, repository.themeCalls)
        }

    @Test
    fun `rapid theme selections persist the latest selection after an in flight write`() =
        runTest {
            val repository = FakeSettingsRepository().apply { delayedTheme = AppTheme.DARK }
            val viewModel = createViewModel(repository)
            collectState(viewModel)

            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.DARK))
            runCurrent()
            repository.delayedThemeStarted.await()
            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.SYSTEM))
            runCurrent()
            repository.finishDelayedTheme.complete(Unit)
            advanceUntilIdle()

            assertEquals(listOf(AppTheme.DARK, AppTheme.SYSTEM), repository.themeWrites)
            assertEquals(AppTheme.SYSTEM, viewModel.state.value.theme)
        }

    @Test
    fun `language selection writes the locale and updates state`() =
        runTest {
            val repository = FakeSettingsRepository(languageTag = AppLanguage.ENGLISH.languageTag)
            val viewModel = createViewModel(repository)
            collectState(viewModel)

            viewModel.onEvent(SettingsUiEvent.LanguageSelected(AppLanguage.VIETNAMESE))
            advanceUntilIdle()

            assertEquals(listOf(AppLanguage.VIETNAMESE.languageTag), repository.languageWrites)
            assertEquals(AppLanguage.VIETNAMESE.languageTag, repository.currentLanguageTag)
            assertEquals(AppLanguage.VIETNAMESE, viewModel.state.value.language)
        }

    @Test
    fun `selecting system language clears the override`() =
        runTest {
            val repository = FakeSettingsRepository(languageTag = AppLanguage.ENGLISH.languageTag)
            val viewModel = createViewModel(repository)
            collectState(viewModel)

            viewModel.onEvent(SettingsUiEvent.LanguageSelected(null))

            assertEquals(listOf(null), repository.languageWrites)
            assertEquals(null, repository.currentLanguageTag)
            assertEquals(null, viewModel.state.value.language)
        }

    @Test
    fun `same language selection is ignored`() =
        runTest {
            val repository = FakeSettingsRepository(languageTag = AppLanguage.ENGLISH.languageTag)
            val viewModel = createViewModel(repository)
            collectState(viewModel)

            viewModel.onEvent(SettingsUiEvent.LanguageSelected(AppLanguage.ENGLISH))

            assertTrue(repository.languageWrites.isEmpty())
        }

    @Test
    fun `unsupported language queues an error and refreshes persisted language`() =
        runTest {
            val repository = FakeSettingsRepository(languageTag = AppLanguage.ENGLISH.languageTag)
            val viewModel = createViewModel(repository)
            collectState(viewModel)

            viewModel.onEvent(SettingsUiEvent.LanguageSelected(AppLanguage("de-DE", R.string.app_name)))

            assertEquals(AppLanguage.ENGLISH, viewModel.state.value.language)
            val text =
                viewModel.state.value.pendingMessages
                    .single()
                    .text as UiText.StringResource
            assertEquals(R.string.settings_language_update_failed, text.resId)
        }

    @Test
    fun `language persistence failure queues an error and preserves the selected language`() =
        runTest {
            val repository =
                FakeSettingsRepository(languageTag = AppLanguage.ENGLISH.languageTag).apply {
                    failLanguageChange = true
                }
            val viewModel = createViewModel(repository)
            collectState(viewModel)

            viewModel.onEvent(SettingsUiEvent.LanguageSelected(AppLanguage.VIETNAMESE))

            assertEquals(AppLanguage.ENGLISH, viewModel.state.value.language)
            assertEquals(1, viewModel.state.value.pendingMessages.size)
        }

    @Test
    fun `refresh updates language after a platform locale change`() =
        runTest {
            val repository = FakeSettingsRepository(languageTag = AppLanguage.ENGLISH.languageTag)
            val viewModel = createViewModel(repository)
            collectState(viewModel)
            repository.setExternalLanguage(null)

            viewModel.refreshLanguage()

            assertEquals(null, viewModel.state.value.language)
        }

    @Test
    fun `message acknowledgement removes only the matching head`() =
        runTest {
            val repository = FakeSettingsRepository().apply { failThemePersistence = true }
            val viewModel = createViewModel(repository)
            collectState(viewModel)
            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.DARK))
            advanceUntilIdle()
            viewModel.onEvent(SettingsUiEvent.LanguageSelected(AppLanguage("de-DE", R.string.app_name)))

            val messages = viewModel.state.value.pendingMessages
            assertEquals(2, messages.size)
            viewModel.onMessageShown(messages[1].id)
            assertEquals(messages, viewModel.state.value.pendingMessages)
            viewModel.onMessageShown(messages[0].id)
            assertEquals(listOf(messages[1]), viewModel.state.value.pendingMessages)
        }

    private fun TestScope.collectState(viewModel: SettingsViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect()
        }
    }

    private fun createViewModel(repository: SettingsRepository): SettingsViewModel =
        SettingsViewModel(repository, SupportedLanguages(AppLanguage.BUILT_IN))
}
