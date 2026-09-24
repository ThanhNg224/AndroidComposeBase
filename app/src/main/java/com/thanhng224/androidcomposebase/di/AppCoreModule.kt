package com.thanhng224.androidcomposebase.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.thanhng224.androidcomposebase.core.foundation.AppDispatchers
import com.thanhng224.androidcomposebase.core.foundation.SecureStore
import com.thanhng224.androidcomposebase.core.foundation.SettingsStore
import com.thanhng224.androidcomposebase.core.localization.AppCompatLocaleApplier
import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import com.thanhng224.androidcomposebase.core.localization.LocaleManager
import com.thanhng224.androidcomposebase.core.localization.SupportedLanguages
import com.thanhng224.androidcomposebase.core.storage.secure.SecureStoreFactory
import com.thanhng224.androidcomposebase.core.storage.settings.SettingsStoreFactory
import com.thanhng224.androidcomposebase.core.ui.theme.ThemeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")

/** Provides the core library's framework-independent contracts, now that :core ships no Hilt bindings of its own. */
@Module
@InstallIn(SingletonComponent::class)
object AppCoreModule {
    @Provides
    @Singleton
    fun provideAppDispatchers(): AppDispatchers = AppDispatchers.default()

    @Provides
    @Singleton
    fun provideSettingsStore(
        @ApplicationContext context: Context,
    ): SettingsStore = SettingsStoreFactory.create(context.appSettingsDataStore)

    @Provides
    @Singleton
    fun provideSecureStore(
        @ApplicationContext context: Context,
        dispatchers: AppDispatchers,
    ): SecureStore = SecureStoreFactory.encrypted(context, dispatchers)

    @Provides
    @Singleton
    fun provideThemeManager(
        @ApplicationContext context: Context,
        settingsStore: SettingsStore,
    ): ThemeManager = ThemeManager.create(settingsStore, context)

    @Provides
    @Singleton
    fun provideSupportedLanguages(): SupportedLanguages = SupportedLanguages(AppLanguage.BUILT_IN)

    @Provides
    @Singleton
    fun provideLocaleManager(
        @ApplicationContext context: Context,
        supportedLanguages: SupportedLanguages,
    ): LocaleManager = LocaleManager(localeApplier = AppCompatLocaleApplier(context), supportedLanguages = supportedLanguages.values)

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
