package com.thanhng224.androidcomposebase

import android.app.Instrumentation
import android.content.res.Configuration
import android.os.Build
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.thanhng224.androidcomposebase.core.foundation.SettingsKey
import com.thanhng224.androidcomposebase.core.foundation.SettingsStore
import com.thanhng224.androidcomposebase.core.ui.theme.AppTheme
import com.thanhng224.androidcomposebase.core.ui.theme.ThemeManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemeManagerPlatformTest {
    @Test
    fun api31ApplicationModesMatchUserSelectionAndSystemMode() {
        assumeTrue("UiModeManager application mode requires Android 12+", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val themeManager = ThemeManager.create(EmptySettingsStore(), context)
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)

        try {
            activityScenario.onActivity { activity ->
                assertEquals(true, activity.startupCoordinator.isReady.value)
            }
            applyTheme(themeManager, AppTheme.SYSTEM, activityScenario, instrumentation)
            val expectedSystemNightMode = currentNightMode(activityScenario)
            applyAndAssert(themeManager, AppTheme.LIGHT, Configuration.UI_MODE_NIGHT_NO, activityScenario, instrumentation)
            applyAndAssert(themeManager, AppTheme.DARK, Configuration.UI_MODE_NIGHT_YES, activityScenario, instrumentation)
            applyAndAssert(themeManager, AppTheme.SYSTEM, expectedSystemNightMode, activityScenario, instrumentation)
        } finally {
            instrumentation.runOnMainSync { themeManager.applyTheme(AppTheme.SYSTEM) }
            instrumentation.waitForIdleSync()
            activityScenario.close()
        }
    }

    private fun applyAndAssert(
        themeManager: ThemeManager,
        theme: AppTheme,
        expectedNightMode: Int,
        activityScenario: ActivityScenario<MainActivity>,
        instrumentation: Instrumentation,
    ) {
        applyTheme(themeManager, theme, activityScenario, instrumentation)
        val deadline = SystemClock.elapsedRealtime() + CONFIGURATION_TIMEOUT_MILLIS
        var actualNightMode = currentNightMode(activityScenario)
        while (actualNightMode != expectedNightMode && SystemClock.elapsedRealtime() < deadline) {
            instrumentation.waitForIdleSync()
            SystemClock.sleep(CONFIGURATION_POLL_INTERVAL_MILLIS)
            actualNightMode = currentNightMode(activityScenario)
        }
        assertEquals(expectedNightMode, actualNightMode)
    }

    private fun applyTheme(
        themeManager: ThemeManager,
        theme: AppTheme,
        activityScenario: ActivityScenario<MainActivity>,
        instrumentation: Instrumentation,
    ) {
        instrumentation.runOnMainSync { themeManager.applyTheme(theme) }
        instrumentation.waitForIdleSync()
        activityScenario.onActivity { }
    }

    private fun currentNightMode(activityScenario: ActivityScenario<MainActivity>): Int {
        var actualNightMode = Configuration.UI_MODE_NIGHT_UNDEFINED
        activityScenario.onActivity { activity ->
            actualNightMode = activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        }
        return actualNightMode
    }

    private class EmptySettingsStore : SettingsStore {
        override fun <T> observe(key: SettingsKey<T>): Flow<T> = flowOf(key.defaultValue)

        override suspend fun <T> get(key: SettingsKey<T>): T = key.defaultValue

        override suspend fun <T> set(
            key: SettingsKey<T>,
            value: T,
        ) = Unit

        override suspend fun <T> remove(key: SettingsKey<T>) = Unit
    }

    private companion object {
        const val CONFIGURATION_TIMEOUT_MILLIS = 5_000L
        const val CONFIGURATION_POLL_INTERVAL_MILLIS = 50L
    }
}
