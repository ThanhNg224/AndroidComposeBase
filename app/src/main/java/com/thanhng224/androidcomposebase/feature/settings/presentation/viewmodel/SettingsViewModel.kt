package com.thanhng224.androidcomposebase.feature.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import com.thanhng224.androidcomposebase.core.localization.SupportedLanguages
import com.thanhng224.androidcomposebase.core.text.UiText
import com.thanhng224.androidcomposebase.core.theme.AppTheme
import com.thanhng224.androidcomposebase.feature.settings.domain.repository.SettingsRepository
import com.thanhng224.androidcomposebase.feature.settings.presentation.state.PendingSettingsMessage
import com.thanhng224.androidcomposebase.feature.settings.presentation.state.SettingsUiEvent
import com.thanhng224.androidcomposebase.feature.settings.presentation.state.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val repository: SettingsRepository,
        supportedLanguages: SupportedLanguages,
    ) : ViewModel() {
        private val languages = supportedLanguages.values
        private val selectedLanguage = MutableStateFlow(languageFor(repository.currentLanguageTag()))
        private val pendingMessages = MutableStateFlow(emptyList<PendingSettingsMessage>())
        private val themeMutationMutex = Mutex()
        private var requestedTheme: AppTheme? = null
        private var hasObservedTheme = false
        private var nextMessageId = 0L

        val state: StateFlow<SettingsUiState> =
            combine(
                repository.observeTheme().onEach { hasObservedTheme = true },
                selectedLanguage,
                pendingMessages,
            ) { theme, language, messages ->
                SettingsUiState(
                    theme = theme,
                    language = language,
                    supportedLanguages = languages,
                    pendingMessages = messages,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SettingsUiState(supportedLanguages = languages),
            )

        fun onEvent(event: SettingsUiEvent) {
            when (event) {
                is SettingsUiEvent.ThemeSelected -> selectTheme(event.theme)
                is SettingsUiEvent.LanguageSelected -> selectLanguage(event.language)
            }
        }

        fun refreshLanguage() {
            selectedLanguage.value = languageFor(repository.currentLanguageTag())
        }

        fun onMessageShown(id: Long) {
            pendingMessages.update { messages ->
                if (messages.firstOrNull()?.id == id) messages.drop(1) else messages
            }
        }

        private fun selectTheme(theme: AppTheme) {
            if (theme == requestedTheme) return
            val themeAlreadyObserved = hasObservedTheme && theme == state.value.theme
            if (requestedTheme == null && themeAlreadyObserved) return
            requestedTheme = theme
            viewModelScope.launch {
                themeMutationMutex.withLock {
                    try {
                        repository.setTheme(theme)
                        if (requestedTheme == theme) requestedTheme = null
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (_: IOException) {
                        if (requestedTheme == theme) {
                            requestedTheme = null
                            enqueue(R.string.settings_theme_update_failed)
                        }
                    }
                }
            }
        }

        private fun selectLanguage(language: AppLanguage?) {
            if (language?.languageTag == selectedLanguage.value?.languageTag) return
            try {
                repository.setLanguageTag(language?.languageTag)
            } catch (_: IllegalArgumentException) {
                enqueue(R.string.settings_language_update_failed)
            } catch (_: IOException) {
                enqueue(R.string.settings_language_update_failed)
            } finally {
                refreshLanguage()
            }
        }

        private fun languageFor(tag: String?): AppLanguage? = tag?.let { AppLanguage.findByLanguageTag(it, languages) }

        private fun enqueue(messageResId: Int) {
            pendingMessages.update { messages ->
                messages + PendingSettingsMessage(++nextMessageId, UiText.StringResource(messageResId))
            }
        }
    }
