package com.thanhng224.androidcomposebase.startup

import com.thanhng224.androidcomposebase.core.ui.theme.AppTheme
import com.thanhng224.androidcomposebase.core.ui.theme.ThemeManager
import com.thanhng224.androidcomposebase.di.ApplicationScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies the persisted theme at process startup with a bounded timeout, so a slow or failing
 * settings read can never hold splash readiness indefinitely. [isReady] becomes `true` exactly
 * once [initialize] returns, whether theme application succeeded, failed, or timed out.
 */
@Singleton
class AppStartupCoordinator
    @Inject
    constructor(
        private val themeManager: ThemeManager,
        @param:ApplicationScope private val applicationScope: CoroutineScope,
    ) {
        private val mutableIsReady = MutableStateFlow(false)
        val isReady: StateFlow<Boolean> = mutableIsReady.asStateFlow()
        private val themeApplicationJob: Job by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            applicationScope.launch { loadAndApplyPersistedTheme() }
        }

        suspend fun initialize() {
            // Awaiting this app-scoped job with a timeout bounds the splash wait without cancelling
            // the underlying DataStore read. A slow read still applies the selected theme later.
            withTimeoutOrNull(STARTUP_TIMEOUT_MILLIS) { themeApplicationJob.join() }
            mutableIsReady.value = true
        }

        private suspend fun loadAndApplyPersistedTheme() {
            try {
                val theme = themeManager.currentTheme.first()
                // ThemeManager.applyTheme is @MainThread and can trigger a configuration change.
                withContext(Dispatchers.Main) { themeManager.applyTheme(theme) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: IOException) {
                Timber.w("Failed to load the persisted theme; applying the system default")
                withContext(Dispatchers.Main) { themeManager.applyTheme(AppTheme.SYSTEM) }
            }
        }

        private companion object {
            const val STARTUP_TIMEOUT_MILLIS = 2_000L
        }
    }
