package com.thanhng224.androidcomposebase.feature.settings.presentation.viewmodel

import app.cash.turbine.test
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import com.thanhng224.androidcomposebase.core.localization.SupportedLanguages
import com.thanhng224.androidcomposebase.core.testing.MainDispatcherRule
import com.thanhng224.androidcomposebase.core.ui.text.UiText
import com.thanhng224.androidcomposebase.core.ui.theme.AppTheme
import com.thanhng224.androidcomposebase.feature.settings.domain.repository.SettingsRepository
import com.thanhng224.androidcomposebase.feature.settings.domain.usecase.GetCurrentLanguageUseCase
import com.thanhng224.androidcomposebase.feature.settings.domain.usecase.GetSupportedLanguagesUseCase
import com.thanhng224.androidcomposebase.feature.settings.domain.usecase.ObserveThemeUseCase
import com.thanhng224.androidcomposebase.feature.settings.domain.usecase.SetLanguageUseCase
import com.thanhng224.androidcomposebase.feature.settings.domain.usecase.SetThemeUseCase
import com.thanhng224.androidcomposebase.feature.settings.presentation.state.SettingsUiEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
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
        private val supportedLanguageTags: List<String> = AppLanguage.BUILT_IN.map(AppLanguage::languageTag),
        theme: AppTheme = AppTheme.SYSTEM,
        private val localeChanges: MutableList<String?>? = null,
        private val failLocaleChange: Boolean = false,
        failThemePersistence: Boolean = false,
    ) : SettingsRepository {
        private val themeFlow = MutableStateFlow(theme)
        private var currentLanguageTag = languageTag
        var failNextThemePersistence = failThemePersistence
        var setThemeCalls = 0
            private set

        override fun observeTheme(): Flow<AppTheme> = themeFlow

        override suspend fun getCurrentLanguageTag(): String? = currentLanguageTag

        fun setExternalLanguage(languageTag: String?) {
            currentLanguageTag = languageTag
        }

        override fun getSupportedLanguageTags(): List<String> = supportedLanguageTags

        override suspend fun setLanguageTag(languageTag: String?) {
            localeChanges?.add(languageTag)
            if (failLocaleChange) throw IOException("locale apply failed")
            currentLanguageTag = languageTag
        }

        override suspend fun setTheme(theme: AppTheme) {
            setThemeCalls += 1
            if (failNextThemePersistence) {
                failNextThemePersistence = false
                throw IOException("persist failed")
            }
            themeFlow.value = theme
        }
    }

    private class DelayedLanguageSettingsRepository(
        private val failEnglishChange: Boolean = false,
    ) : SettingsRepository {
        private val themeFlow = MutableStateFlow(AppTheme.SYSTEM)
        val englishChangeStarted = CompletableDeferred<Unit>()
        val finishEnglishChange = CompletableDeferred<Unit>()
        val localeChanges = mutableListOf<String?>()
        var currentLanguageTag: String? = AppLanguage.VIETNAMESE.languageTag
            private set

        override fun observeTheme(): Flow<AppTheme> = themeFlow

        override suspend fun getCurrentLanguageTag(): String? = currentLanguageTag

        override fun getSupportedLanguageTags(): List<String> = AppLanguage.BUILT_IN.map(AppLanguage::languageTag)

        override suspend fun setLanguageTag(languageTag: String?) {
            localeChanges += languageTag
            if (languageTag == AppLanguage.ENGLISH.languageTag) {
                englishChangeStarted.complete(Unit)
                finishEnglishChange.await()
                if (failEnglishChange) throw IOException("locale apply failed")
            }
            currentLanguageTag = languageTag
        }

        override suspend fun setTheme(theme: AppTheme) {
            themeFlow.value = theme
        }
    }

    private class LocaleRefreshRaceRepository : SettingsRepository {
        private val themeFlow = MutableStateFlow(AppTheme.SYSTEM)
        val refreshStarted = CompletableDeferred<Unit>()
        val finishRefresh = CompletableDeferred<Unit>()
        private var reads = 0
        var currentLanguageTag: String? = AppLanguage.VIETNAMESE.languageTag
            private set

        override fun observeTheme(): Flow<AppTheme> = themeFlow

        override suspend fun getCurrentLanguageTag(): String? {
            reads += 1
            if (reads > 1) {
                refreshStarted.complete(Unit)
                finishRefresh.await()
            }
            return currentLanguageTag
        }

        override fun getSupportedLanguageTags(): List<String> = AppLanguage.BUILT_IN.map(AppLanguage::languageTag)

        override suspend fun setLanguageTag(languageTag: String?) {
            currentLanguageTag = languageTag
        }

        override suspend fun setTheme(theme: AppTheme) {
            themeFlow.value = theme
        }
    }

    private class DelayedThemeSettingsRepository : SettingsRepository {
        private val themeFlow = MutableStateFlow(AppTheme.SYSTEM)
        val darkMutationStarted = CompletableDeferred<Unit>()
        val finishDarkMutation = CompletableDeferred<Unit>()
        val mutationThemes = mutableListOf<AppTheme>()

        override fun observeTheme(): Flow<AppTheme> = themeFlow

        override suspend fun getCurrentLanguageTag(): String? = AppLanguage.ENGLISH.languageTag

        override fun getSupportedLanguageTags(): List<String> = AppLanguage.BUILT_IN.map(AppLanguage::languageTag)

        override suspend fun setLanguageTag(languageTag: String?) = Unit

        override suspend fun setTheme(theme: AppTheme) {
            mutationThemes += theme
            if (theme == AppTheme.DARK) {
                darkMutationStarted.complete(Unit)
                finishDarkMutation.await()
            }
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
    fun `refresh reflects external language changes and clearing the system override`() =
        runTest {
            val repository = FakeSettingsRepository(languageTag = AppLanguage.ENGLISH.languageTag)
            val viewModel = createViewModel(repository)
            advanceUntilIdle()

            repository.setExternalLanguage(AppLanguage.VIETNAMESE.languageTag)
            viewModel.refreshCurrentLanguage()
            advanceUntilIdle()
            assertEquals(AppLanguage.VIETNAMESE, viewModel.state.value.language)

            repository.setExternalLanguage(null)
            viewModel.refreshCurrentLanguage()
            advanceUntilIdle()
            assertEquals(null, viewModel.state.value.language)
        }

    @Test
    fun `language selection wins over an in flight stale resume refresh`() =
        runTest {
            val repository = LocaleRefreshRaceRepository()
            val viewModel = createViewModel(repository)
            advanceUntilIdle()

            viewModel.refreshCurrentLanguage()
            repository.refreshStarted.await()
            viewModel.onEvent(SettingsUiEvent.LanguageSelected(AppLanguage.ENGLISH))
            repository.finishRefresh.complete(Unit)
            advanceUntilIdle()

            assertEquals(AppLanguage.ENGLISH.languageTag, repository.currentLanguageTag)
            assertEquals(AppLanguage.ENGLISH, viewModel.state.value.language)
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
    fun `theme persistence failure reports an error and a later selection retries`() =
        runTest {
            val repository = FakeSettingsRepository(failThemePersistence = true)
            val viewModel = createViewModel(repository)
            advanceUntilIdle()

            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.DARK))
            advanceUntilIdle()

            assertEquals(AppTheme.SYSTEM, viewModel.state.value.theme)
            val message =
                viewModel.state.value.pendingMessages
                    .single()
                    .text as UiText.StringResource
            assertEquals(R.string.settings_theme_update_failed, message.resId)

            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.DARK))
            advanceUntilIdle()

            assertEquals(AppTheme.DARK, viewModel.state.value.theme)
            assertEquals(2, repository.setThemeCalls)
        }

    @Test
    fun `rapid theme selections coalesce duplicates and retain the latest intent`() =
        runTest {
            val repository = DelayedThemeSettingsRepository()
            val viewModel = createViewModel(repository)
            advanceUntilIdle()

            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.DARK))
            repository.darkMutationStarted.await()
            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.DARK))
            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.LIGHT))
            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.LIGHT))
            advanceUntilIdle()

            assertEquals(listOf(AppTheme.DARK), repository.mutationThemes)
            repository.finishDarkMutation.complete(Unit)
            advanceUntilIdle()

            assertEquals(listOf(AppTheme.DARK, AppTheme.LIGHT), repository.mutationThemes)
            assertEquals(AppTheme.LIGHT, viewModel.state.value.theme)
        }

    @Test
    fun `selecting a language applies the platform locale once`() =
        runTest {
            val localeChanges = mutableListOf<String?>()
            val viewModel =
                createViewModel(
                    FakeSettingsRepository(languageTag = AppLanguage.ENGLISH.languageTag, localeChanges = localeChanges),
                )
            advanceUntilIdle()

            viewModel.onEvent(SettingsUiEvent.LanguageSelected(AppLanguage.VIETNAMESE))
            advanceUntilIdle()

            assertEquals(listOf(AppLanguage.VIETNAMESE.languageTag), localeChanges)
            assertEquals(AppLanguage.VIETNAMESE, viewModel.state.value.language)
        }

    @Test
    fun `system language selection clears the app locale override`() =
        runTest {
            val localeChanges = mutableListOf<String?>()
            val viewModel =
                createViewModel(
                    FakeSettingsRepository(languageTag = AppLanguage.ENGLISH.languageTag, localeChanges = localeChanges),
                )
            advanceUntilIdle()

            viewModel.onEvent(SettingsUiEvent.LanguageSelected(null))
            advanceUntilIdle()

            assertEquals(listOf(null), localeChanges)
            assertEquals(null, viewModel.state.value.language)
        }

    @Test
    fun `a failed platform locale change keeps the previous language and queues an error message`() =
        runTest {
            val localeChanges = mutableListOf<String?>()
            val viewModel =
                createViewModel(
                    FakeSettingsRepository(
                        languageTag = AppLanguage.ENGLISH.languageTag,
                        localeChanges = localeChanges,
                        failLocaleChange = true,
                    ),
                )
            advanceUntilIdle()

            viewModel.onEvent(SettingsUiEvent.LanguageSelected(AppLanguage.VIETNAMESE))
            advanceUntilIdle()

            assertEquals(listOf(AppLanguage.VIETNAMESE.languageTag), localeChanges)
            assertEquals(AppLanguage.ENGLISH, viewModel.state.value.language)
            assertEquals(1, viewModel.state.value.pendingMessages.size)
        }

    @Test
    fun `acknowledging the language error message removes it`() =
        runTest {
            val viewModel =
                createViewModel(FakeSettingsRepository(languageTag = AppLanguage.ENGLISH.languageTag, failLocaleChange = true))
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
    fun `re-collecting state does not repeat the platform locale change`() =
        runTest {
            val localeChanges = mutableListOf<String?>()
            val viewModel =
                createViewModel(
                    FakeSettingsRepository(languageTag = AppLanguage.ENGLISH.languageTag, localeChanges = localeChanges),
                )
            advanceUntilIdle()
            viewModel.onEvent(SettingsUiEvent.LanguageSelected(AppLanguage.VIETNAMESE))
            advanceUntilIdle()

            viewModel.state.test { awaitItem() }
            viewModel.state.test { awaitItem() }

            assertEquals(listOf(AppLanguage.VIETNAMESE.languageTag), localeChanges)
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

    @Test
    fun `platform locale changes are serialized so the latest selection wins across inverse completion order`() =
        runTest {
            val repository = DelayedLanguageSettingsRepository()
            val viewModel = createViewModel(repository)
            advanceUntilIdle()

            viewModel.onEvent(SettingsUiEvent.LanguageSelected(AppLanguage.ENGLISH))
            repository.englishChangeStarted.await()
            viewModel.onEvent(SettingsUiEvent.LanguageSelected(AppLanguage.VIETNAMESE))
            advanceUntilIdle()

            assertEquals(listOf(AppLanguage.ENGLISH.languageTag), repository.localeChanges)
            assertEquals(
                AppLanguage.VIETNAMESE.languageTag,
                viewModel.state.value.language
                    ?.languageTag,
            )

            repository.finishEnglishChange.complete(Unit)
            advanceUntilIdle()

            assertTrue(repository.localeChanges.contains(AppLanguage.VIETNAMESE.languageTag))
            assertEquals(AppLanguage.VIETNAMESE.languageTag, repository.currentLanguageTag)
            assertEquals(AppLanguage.VIETNAMESE, viewModel.state.value.language)
        }

    @Test
    fun `superseded language failure does not enqueue a stale error after latest selection succeeds`() =
        runTest {
            val repository = DelayedLanguageSettingsRepository(failEnglishChange = true)
            val viewModel = createViewModel(repository)
            advanceUntilIdle()

            viewModel.onEvent(SettingsUiEvent.LanguageSelected(AppLanguage.ENGLISH))
            repository.englishChangeStarted.await()
            viewModel.onEvent(SettingsUiEvent.LanguageSelected(AppLanguage.VIETNAMESE))
            advanceUntilIdle()

            repository.finishEnglishChange.complete(Unit)
            advanceUntilIdle()

            assertEquals(AppLanguage.VIETNAMESE.languageTag, repository.currentLanguageTag)
            assertEquals(AppLanguage.VIETNAMESE, viewModel.state.value.language)
            assertTrue(
                viewModel.state.value.pendingMessages
                    .isEmpty(),
            )
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
