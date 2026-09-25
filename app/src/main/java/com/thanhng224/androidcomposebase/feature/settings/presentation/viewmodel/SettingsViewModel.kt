package com.thanhng224.androidcomposebase.feature.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import com.thanhng224.androidcomposebase.core.localization.SupportedLanguages
import com.thanhng224.androidcomposebase.core.ui.text.UiText
import com.thanhng224.androidcomposebase.core.ui.theme.AppTheme
import com.thanhng224.androidcomposebase.feature.settings.domain.usecase.GetCurrentLanguageUseCase
import com.thanhng224.androidcomposebase.feature.settings.domain.usecase.GetSupportedLanguagesUseCase
import com.thanhng224.androidcomposebase.feature.settings.domain.usecase.ObserveThemeUseCase
import com.thanhng224.androidcomposebase.feature.settings.domain.usecase.SetLanguageUseCase
import com.thanhng224.androidcomposebase.feature.settings.domain.usecase.SetThemeUseCase
import com.thanhng224.androidcomposebase.feature.settings.presentation.state.PendingSettingsMessage
import com.thanhng224.androidcomposebase.feature.settings.presentation.state.SettingsUiEvent
import com.thanhng224.androidcomposebase.feature.settings.presentation.state.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        observeTheme: ObserveThemeUseCase,
        supportedLanguages: SupportedLanguages,
        private val getCurrentLanguage: GetCurrentLanguageUseCase,
        getSupportedLanguages: GetSupportedLanguagesUseCase,
        private val setTheme: SetThemeUseCase,
        private val setLanguage: SetLanguageUseCase,
    ) : ViewModel() {
        private var isInitialLanguageLoaded = false
        private var latestRequestedLanguageTag: String? = null
        private var languageRequestVersion = 0L
        private val languageMutationMutex = Mutex()
        private var latestRequestedTheme: AppTheme? = null
        private var persistedTheme: AppTheme? = null
        private var themeRequestVersion = 0L
        private var completedThemeRequestVersion = 0L
        private val themeMutationMutex = Mutex()
        private val nextMessageId = AtomicLong(0)
        private val presentationLanguages = supportedLanguages.values
        private val mutableState =
            MutableStateFlow(
                SettingsUiState(
                    supportedLanguages = getSupportedLanguages().mapNotNull(::findPresentationLanguage),
                ),
            )
        val state: StateFlow<SettingsUiState> = mutableState.asStateFlow()

        init {
            viewModelScope.launch {
                observeTheme().collect { theme ->
                    persistedTheme = theme
                    if (themeRequestVersion == completedThemeRequestVersion) {
                        latestRequestedTheme = theme
                        mutableState.update { it.copy(theme = theme) }
                    }
                }
            }
            viewModelScope.launch {
                val language = getCurrentLanguage()?.let(::findPresentationLanguage)
                latestRequestedLanguageTag = language?.languageTag
                mutableState.update { it.copy(language = language) }
                isInitialLanguageLoaded = true
            }
        }

        fun onEvent(event: SettingsUiEvent) {
            when (event) {
                is SettingsUiEvent.ThemeSelected -> selectTheme(event)
                is SettingsUiEvent.LanguageSelected -> selectLanguage(event)
            }
        }

        fun onMessageHandled(id: Long) {
            removeHeadIfMatching(id)
        }

        /** Reconciles the selected language after returning from system per-app language settings. */
        fun refreshCurrentLanguage() {
            if (!isInitialLanguageLoaded) return
            val requestVersion = languageRequestVersion
            viewModelScope.launch {
                languageMutationMutex.withLock {
                    if (requestVersion != languageRequestVersion) return@withLock
                    val language = getCurrentLanguage()?.let(::findPresentationLanguage)
                    if (requestVersion != languageRequestVersion) return@withLock
                    latestRequestedLanguageTag = language?.languageTag
                    mutableState.update { it.copy(language = language) }
                }
            }
        }

        private fun selectTheme(event: SettingsUiEvent.ThemeSelected) {
            if (event.theme == (latestRequestedTheme ?: mutableState.value.theme)) return
            latestRequestedTheme = event.theme
            val requestVersion = ++themeRequestVersion
            viewModelScope.launch {
                themeMutationMutex.withLock {
                    if (requestVersion != themeRequestVersion) return@withLock
                    try {
                        setTheme(event.theme)
                        persistedTheme = event.theme
                        completedThemeRequestVersion = requestVersion
                        if (requestVersion == themeRequestVersion) {
                            mutableState.update { it.copy(theme = event.theme) }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: IOException) {
                        completedThemeRequestVersion = requestVersion
                        if (requestVersion == themeRequestVersion) {
                            latestRequestedTheme = persistedTheme ?: mutableState.value.theme
                            mutableState.update {
                                it.copy(
                                    theme = latestRequestedTheme ?: it.theme,
                                    pendingMessages =
                                        it.pendingMessages +
                                            PendingSettingsMessage(
                                                id = nextMessageId.incrementAndGet(),
                                                text = UiText.StringResource(R.string.settings_theme_update_failed),
                                            ),
                                )
                            }
                        }
                    }
                }
            }
        }

        private fun selectLanguage(event: SettingsUiEvent.LanguageSelected) {
            if (!isInitialLanguageLoaded) return
            val requestedLanguageTag = event.language?.languageTag
            if (requestedLanguageTag == latestRequestedLanguageTag) return
            latestRequestedLanguageTag = requestedLanguageTag
            val requestVersion = ++languageRequestVersion
            viewModelScope.launch {
                languageMutationMutex.withLock {
                    if (requestVersion != languageRequestVersion) return@withLock
                    try {
                        setLanguage(requestedLanguageTag)
                        if (requestVersion == languageRequestVersion) {
                            mutableState.update { it.copy(language = event.language) }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: IOException) {
                        if (requestVersion == languageRequestVersion) {
                            latestRequestedLanguageTag = mutableState.value.language?.languageTag
                            enqueueMessage(UiText.StringResource(R.string.settings_language_update_failed))
                        }
                    }
                }
            }
        }

        private fun findPresentationLanguage(languageTag: String): AppLanguage? =
            presentationLanguages.firstOrNull { it.languageTag == languageTag }
                ?: AppLanguage.findByLanguageTag(languageTag, presentationLanguages)

        private fun enqueueMessage(text: UiText) {
            val message = PendingSettingsMessage(id = nextMessageId.incrementAndGet(), text = text)
            mutableState.update { it.copy(pendingMessages = it.pendingMessages + message) }
        }

        /** Returns whether [id] matched the current head and was removed. */
        private fun removeHeadIfMatching(id: Long): Boolean {
            var removed = false
            mutableState.update { current ->
                val head = current.pendingMessages.firstOrNull()
                if (head?.id != id) {
                    current
                } else {
                    removed = true
                    current.copy(pendingMessages = current.pendingMessages.drop(1))
                }
            }
            return removed
        }
    }
