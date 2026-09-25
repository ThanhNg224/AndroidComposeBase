package com.thanhng224.androidcomposebase.core.theme

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatDelegate
import com.thanhng224.androidcomposebase.core.foundation.SettingsStore
import com.thanhng224.androidcomposebase.core.storage.settings.AppSettingsKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

public interface ThemeManager {
    public val currentTheme: Flow<AppTheme>

    /** True once the persisted theme has been read and applied at least once this process. */
    public val isThemeApplied: StateFlow<Boolean>

    public suspend fun getTheme(): AppTheme

    /**
     * Persists [theme] and applies it. Applying recreates live Activities, so call this from a
     * coroutine on the main dispatcher -- `viewModelScope` already is one.
     */
    @MainThread
    public suspend fun setTheme(theme: AppTheme)

    /**
     * Applies [theme] without persisting it.
     *
     * On Android 12 and newer, sets the app-specific platform night mode so the system can apply
     * it during splash. Older Android versions use [AppCompatDelegate.setDefaultNightMode]. Both
     * paths can trigger an Activity configuration change and must be called on the main thread.
     */
    @MainThread
    public fun applyTheme(theme: AppTheme)

    public companion object {
        /** Creates a manager for callers that do not own an Android [Context]. */
        public fun create(settingsStore: SettingsStore): ThemeManager = AndroidThemeManager(settingsStore, null)

        /** Creates a manager that uses the platform app-specific night mode API when available. */
        public fun create(
            settingsStore: SettingsStore,
            context: Context,
        ): ThemeManager = AndroidThemeManager(settingsStore, context.applicationContext)
    }
}

internal class AndroidThemeManager
    internal constructor(
        private val settingsStore: SettingsStore,
        private val applicationContext: Context? = null,
    ) : ThemeManager {
        private val themeAppliedState = MutableStateFlow(false)
        override val isThemeApplied: StateFlow<Boolean> = themeAppliedState.asStateFlow()

        override val currentTheme: Flow<AppTheme> =
            settingsStore
                .observe(AppSettingsKeys.THEME_MODE)
                .map { AppTheme.fromKey(it) }

        override suspend fun getTheme(): AppTheme {
            val key = settingsStore.get(AppSettingsKeys.THEME_MODE)
            return AppTheme.fromKey(key)
        }

        override suspend fun setTheme(theme: AppTheme) {
            settingsStore.set(AppSettingsKeys.THEME_MODE, theme.key)
            applyTheme(theme)
        }

        override fun applyTheme(theme: AppTheme) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && applicationContext != null) {
                val uiModeManager =
                    checkNotNull(applicationContext.getSystemService(UiModeManager::class.java)) {
                        "UiModeManager is unavailable"
                    }
                // The platform setter is idempotent for the current package configuration and
                // also clears an external app-specific override when the user chooses System.
                uiModeManager.setApplicationNightMode(theme.toApplicationNightMode())
            } else {
                val nightMode =
                    when (theme) {
                        AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                        AppTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                        AppTheme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
                    AppCompatDelegate.setDefaultNightMode(nightMode)
                }
            }
            themeAppliedState.value = true
        }
    }

/** MODE_NIGHT_AUTO means no app-specific night override in setApplicationNightMode. */
internal fun AppTheme.toApplicationNightMode(): Int =
    when (this) {
        AppTheme.LIGHT -> UiModeManager.MODE_NIGHT_NO
        AppTheme.DARK -> UiModeManager.MODE_NIGHT_YES
        // AOSP maps AUTO to UI_MODE_NIGHT_UNDEFINED for this app-specific API, so the system
        // configuration selects the app's night qualifier. This differs from setNightMode(AUTO).
        AppTheme.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
    }
