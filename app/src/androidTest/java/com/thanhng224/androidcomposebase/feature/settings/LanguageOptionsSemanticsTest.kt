package com.thanhng224.androidcomposebase.feature.settings

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import com.thanhng224.androidcomposebase.feature.settings.presentation.ui.LanguageOptions
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LanguageOptionsSemanticsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun systemAndLanguageRowsExposeOneSelectionAndWholeRowActions() {
        val selectedLanguage = mutableStateOf<AppLanguage?>(AppLanguage.ENGLISH)
        val systemLabel = ApplicationProvider.getApplicationContext<Context>().getString(R.string.settings_language_system)
        composeRule.setContent {
            MaterialTheme {
                LanguageOptions(
                    languages = AppLanguage.BUILT_IN,
                    selectedLanguage = selectedLanguage.value,
                    onLanguageSelected = { selectedLanguage.value = it },
                )
            }
        }

        composeRule.onAllNodesWithText("English").assertCountEquals(1)
        composeRule.onNodeWithText(systemLabel).assertHasClickAction()
        composeRule
            .onNodeWithText("English")
            .assertIsSelected()
            .assertHasClickAction()
        composeRule.onNodeWithText(systemLabel).performClick().assertIsSelected()
        composeRule.onNodeWithText("English").assertIsNotSelected()
        composeRule.onNodeWithText(systemLabel).assertIsSelected()
        composeRule.runOnIdle { assertEquals(null, selectedLanguage.value) }
        composeRule.onNodeWithText("Tiếng Việt").performClick()
        composeRule.onNodeWithText("Tiếng Việt").assertIsSelected()
        composeRule.onNodeWithText(systemLabel).assertIsNotSelected()
        composeRule.runOnIdle { assertEquals(AppLanguage.VIETNAMESE, selectedLanguage.value) }
    }
}
