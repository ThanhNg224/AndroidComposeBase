package com.thanhng224.androidcomposebase.localization

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.thanhng224.androidcomposebase.MainActivity
import com.thanhng224.androidcomposebase.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppLocaleActivityTest {
    @After
    fun resetAppLocale() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    @Test
    fun changingAppLocaleRecreatesComposeHostAndResolvesLocalizedResources() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertTrue(activity is AppCompatActivity)
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity { activity ->
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("vi-VN"))
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity { activity ->
                val currentLocale = activity.resources.configuration.locales[0]
                assertEquals("vi", currentLocale.language)
                assertEquals(
                    "Cài đặt",
                    activity.getString(R.string.navigation_settings),
                )
            }
        }
    }
}
